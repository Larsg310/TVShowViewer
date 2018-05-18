package nl.larsgerrits.tvshows.command;

import bt.Bt;
import bt.data.Storage;
import bt.runtime.BtClient;
import bt.runtime.Config;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.util.Pair;
import nl.larsgerrits.tvshows.Episode;
import nl.larsgerrits.tvshows.SeasonInfo;
import nl.larsgerrits.tvshows.TVShowFileSystemStorage;
import nl.larsgerrits.tvshows.apifetchv2.APIFetchV2;
import nl.larsgerrits.tvshows.apifetchv2.models.EpisodeModel;
import nl.larsgerrits.tvshows.apifetchv2.models.ShowInfo;
import nl.larsgerrits.tvshows.util.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandUpdate extends Command
{
    private boolean updatedAny = false;
    
    @Override
    public void execute(String[] args)
    {
        if (args == null || args[0].equals("all")) updateAllShows();
        else updateShow(args[0]);
    }
    
    private void updateAllShows()
    {
        List<Pair<File, SeasonInfo>> seasonInfos = FileUtils.getAllSeasonInfo();
        for (Pair<File, SeasonInfo> pair : seasonInfos)
        {
            System.out.println("Checking show \"" + pair.getValue().getTitle() + "\", season " + pair.getValue().getSeason());
            updateShow(pair.getKey(), pair.getValue());
        }
        if (!updatedAny) System.out.println("All shows are up-to-date!");
    }
    
    private void updateShow(String showName)
    {
    }
    
    private void updateShow(File dir, SeasonInfo seasonInfo)
    {
        ShowInfo showInfo = APIFetchV2.getShowInfo(seasonInfo.getImdbId());
        for (Episode episode : seasonInfo.getEpisodes())
        {
            AtomicBoolean downloaded = new AtomicBoolean(false);
            if (episode.getFileName().isEmpty() && !episode.getTitle().isEmpty())
            {
                System.out.println("\tEpisode " + String.format("%02d", episode.getEpisode()) + " not found, checking for download...");
                Optional<EpisodeModel> optionalEpisode = showInfo.getEpisodes().stream().filter(e -> e.getSeason() == seasonInfo.getSeason()).filter(e -> e.getEpisode() == episode.getEpisode()).findFirst();
                
                if (optionalEpisode.isPresent())
                {
                    EpisodeModel model = optionalEpisode.get();
                    int index = model.getTorrents().size() - 1;
                    while (!downloaded.get() && index >= 0)
                    {
                        String magnetURL = model.getTorrents().get(index).getUrl();
                        
                        System.out.println("\tDownload found, starting download...");
                        
                        String fileName = "episode_" + String.format("%02d", model.getEpisode()) + "_" + FileUtils.fixFileName(model.getTitle()) + ".mkv";
                        Storage storage = new TVShowFileSystemStorage(dir.toPath(), fileName);
                        Config config = new Config()
                        {
                            @Override
                            public int getNumOfHashingThreads()
                            {
                                return Runtime.getRuntime().availableProcessors() * 2;
                            }
                            
                        };
                        BtClient client = Bt.client().magnet(magnetURL).storage(storage).autoLoadModules().config(config).stopWhenDownloaded().build();
                        
                        AtomicInteger counter = new AtomicInteger();
                        AtomicDouble prevCompleted = new AtomicDouble(-1);
                        client.startAsync(state -> {
                            if (prevCompleted.get() != state.getPiecesComplete())
                            {
                                System.out.println(String.format("\t\tProgress: %.2f", state.getPiecesComplete() * 100D / state.getPiecesTotal()).replace(',', '.') + "% (" + state.getPiecesComplete() + "/" + state.getPiecesTotal() + ")");
                                counter.set(0);
                                prevCompleted.set(state.getPiecesComplete());
                            }
                            if (counter.get() > 60) client.stop();
                            counter.getAndIncrement();
                            if (state.getPiecesRemaining() == 0) {
                                downloaded.set(true);
                                client.stop();
                            }
                        }, 1000).join();
                        index--;
                    }
                }
                if (downloaded.get())
                {
                    FileUtils.fixSeasonMetadata(seasonInfo, dir);
                    updatedAny = true;
                }
            }
        }
        
    }
}
