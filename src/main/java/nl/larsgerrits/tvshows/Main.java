package nl.larsgerrits.tvshows;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.runtime.BtClient;
import bt.runtime.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.enums.Extended;
import nl.larsgerrits.tvshows.apifetchv2.APIFetchV2;
import nl.larsgerrits.tvshows.apifetchv2.models.EpisodeModel;
import nl.larsgerrits.tvshows.apifetchv2.models.ShowInfo;
import retrofit2.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
    private static final Map<String, TVShow> TV_SHOWS = new HashMap<>();
    
    public static final TraktV2 TRAKT_TV = new TraktV2("d37bc084cad26a17a8a4ae8bf01eb73262e9ae15823351257f87c335c69f466d", "ba0b07289702f71b73c3427503c5d62598bcd2d83e62d8e0d9b4496a4699c328", "http://www.larsgerrits.nl");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public static final File DIRECTORY = new File("G:\\TV Series");
    public static final File DOWNLOAD_DIR = new File("G:\\TV Series\\_downloads");
    public static final String SEASON_INFO_NAME = "season_info.json";
    
    public static final Function<String, String> SEASON_INFO_PATH = (t) -> DIRECTORY + "\\" + t + "\\" + SEASON_INFO_NAME;
    
    public static final Pattern SHOW_PATTERN = Pattern.compile("([^\\s]+)_season_(0?[0-9]|[0-9][1-9])");
    
    public static void main(String[] args) throws IOException, Throwable
    {
        File[] directories = DIRECTORY.listFiles(File::isDirectory);
        
        for (File dir : Objects.requireNonNull(directories))
        {
            Matcher matcher = SHOW_PATTERN.matcher(dir.getName());
            if (matcher.matches())
            {
                if (Files.exists(Paths.get(SEASON_INFO_PATH.apply(dir.getName()))))
                {
                    String content = new String(Files.readAllBytes(Paths.get(SEASON_INFO_PATH.apply(dir.getName()))));
                    if (!content.isEmpty())
                    {
                        SeasonInfo seasonInfo = GSON.fromJson(content, SeasonInfo.class);
                        if (!seasonInfo.isComplete()) fixSeasonMetadata(seasonInfo, dir);
                        addSeasonToShow(seasonInfo);
                        
                        // File[] episodeFiles = dir.listFiles(f -> f.getName().startsWith("episode_"));
                        //
                        // List<EpisodeModel> episodes = new ArrayList<>();
                        //
                        // Response<List<com.uwetrottmann.trakt5.entities.EpisodeModel>> episodeList = TRAKT_TV.seasons().season(info.getImdbId(), info.getSeason(), Extended.EPISODES).execute();
                        //
                        // //                    System.out.println(episodeList.body());
                        //
                        // for (com.uwetrottmann.trakt5.entities.EpisodeModel file : episodeList.body())
                        // {
                        //     int episode = file.number;
                        //
                        //     assert episodeList.body() != null;
                        //
                        //     EpisodeModel episode1 = new EpisodeModel().setEpisode(episode).setTitle(episodeList.body().get(episode - 1).title);
                        //     assert episodeFiles != null;
                        //     if (episode <= episodeFiles.length) {episode1.setFileName(episodeFiles[episode - 1].getName());}
                        //     episodes.add(episode1);
                        //
                        // }
                        //
                        // info.setEpisodes(episodes);
                        //
                        // String json = GSON.toJson(info);
                        //
                        // try (FileWriter writer = new FileWriter(Paths.get(SEASON_INFO_PATH.apply(dir.getName())).toFile()))
                        // {
                        //     writer.write(json);
                        // }
                        
                        ShowInfo showInfo = APIFetchV2.getShowInfo(seasonInfo.getImdbId());
                        for (Episode episode : seasonInfo.getEpisodes())
                        {
                            if (episode.getFileName().isEmpty())
                            {
                                Optional<EpisodeModel> optionalEpisode = showInfo.getEpisodes().stream().filter(e -> e.getSeason() == seasonInfo.getSeason()).filter(e -> e.getEpisode() == episode.getEpisode()).findFirst();
                                
                                if (optionalEpisode.isPresent())
                                {
                                    EpisodeModel model = optionalEpisode.get();
                                    
                                    String magnetURL = model.getTorrents().get(model.getTorrents().size() - 1).getUrl();
                                    System.out.println(showInfo.getTitle() + " " + model.getSeason() + "x" + episode.getEpisode() + ": " + magnetURL);
                                    
                                    Storage storage = new FileSystemStorage(dir.toPath());
                                    Config config = new Config()
                                    {
                                        @Override
                                        public int getNumOfHashingThreads()
                                        {
                                            return Runtime.getRuntime().availableProcessors() * 2;
                                        }
                                    };
                                    BtClient client = Bt.client().magnet(magnetURL).storage(storage).autoLoadModules().stopWhenDownloaded().build();
                                    
                                    Object obj = client.startAsync(state -> {
                                        System.out.println("Progress: " + (state.getPiecesComplete() * 100D / state.getPiecesTotal()));
                                        if(state.getPiecesRemaining() == 0) client.stop();
                                    }, 1000).join();
                                    System.out.println(obj);
                                }
                                
                            }
                        }
                        // Response<Show> response = traktShows.summary(showName.replace('_', '-'), Extended.FULL).execute();
                        //
                        // if (response.isSuccessful())
                        // {
                        //     assert response.body() != null;
                        //     //                    System.out.println(showName + ": " + response.body().ids.imdb);
                        //
                        //     SeasonInfo info = new SeasonInfo();
                        //
                        //     info.setImdbId(response.body().ids.imdb);
                        //     info.setSeason(seasonNumber);
                        //     info.setTitle(response.body().title);
                        //
                        //     Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        //     String json = gson.toJson(info);
                        //     System.out.println(json);
                        //
                        //     try (FileWriter writer = new FileWriter(DIRECTORY + "\\" + dir.getName() + "\\" + "season_info.json"))
                        //     {
                        //         writer.write(json);
                        //     }
                        //     catch (IOException e)
                        //     {
                        //         e.printStackTrace();
                        //     }
                        // }
                    }
                }
                // File[] episodes = dir.listFiles(f -> !f.getName().endsWith(".json") && !f.getName().equals("desktopini"));
                // for (File episode : episodes)
                // {
                //     String filename = episode.getName().substring(0, episode.getName().lastIndexOf('.'));
                //     String newFileName = fixFileName(filename);
                //     String extension = episode.getName().substring(episode.getName().lastIndexOf('.'), episode.getName().length());
                //     if (!extension.equals(".mkv") || !filename.equals(newFileName))
                //     {
                //         System.out.println(filename + extension + ": " + newFileName + ".mkv");
                //         System.out.println();
                //         File newFile = new File(dir.getPath() + "\\" + newFileName + ".mkv");
                //         episode.renameTo(newFile);
                //     }
                // }
            }
        }
        System.out.println("Shows added");
    }
    
    private static void fixSeasonMetadata(SeasonInfo info, File dir) throws IOException
    {
        Response<List<com.uwetrottmann.trakt5.entities.Episode>> response = TRAKT_TV.seasons().season(info.getImdbId(), info.getSeason(), Extended.EPISODES).execute();
        
        if (response.isSuccessful())
        {
            Map<Integer, String> map = new HashMap<>();
            
            File[] episodeFiles = dir.listFiles(f -> f.getName().startsWith("episode_"));
            assert episodeFiles != null;
            for (File f : episodeFiles)
            {
                int number = Integer.parseInt(f.getName().split("_")[1]);
                map.put(number, f.getName());
            }
            
            List<Episode> episodes = new ArrayList<>();
            assert response.body() != null;
            for (com.uwetrottmann.trakt5.entities.Episode episode : response.body())
            {
                Episode ep = new Episode();
                ep.setEpisode(episode.number);
                ep.setTitle(episode.title);
                if (map.containsKey(episode.number)) ep.setFileName(map.get(episode.number));
                episodes.add(ep);
            }
            info.setEpisodes(episodes);
        }
        String json = GSON.toJson(info);
        
        try (FileWriter writer = new FileWriter(Paths.get(SEASON_INFO_PATH.apply(dir.getName())).toFile()))
        {
            writer.write(json);
        }
        
    }
    
    private static String fixFileName(String filename)
    {
        return filename.toLowerCase()//
                       .replace('-', '_')//
                       .replaceAll("[^a-zA-Z_\\d]+", "")//
                       .replace("__", "_");
    }
    
    private static void addSeasonToShow(SeasonInfo info)
    {
        if (!TV_SHOWS.containsKey(info.getImdbId())) TV_SHOWS.put(info.getImdbId(), new TVShow(info.getTitle()));
        TV_SHOWS.get(info.getImdbId()).addSeason(new Season(info.getSeason(), info.getEpisodes()));
    }
}