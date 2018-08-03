package nl.larsgerrits.tvshows.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.Episode;
import com.uwetrottmann.trakt5.entities.Show;
import com.uwetrottmann.trakt5.enums.Extended;
import nl.larsgerrits.tvshows.SeasonInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.threeten.bp.ZoneOffset;
import retrofit2.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils
{
    public static final TraktV2 TRAKT_TV = new TraktV2("d37bc084cad26a17a8a4ae8bf01eb73262e9ae15823351257f87c335c69f466d", "ba0b07289702f71b73c3427503c5d62598bcd2d83e62d8e0d9b4496a4699c328", "http://www.larsgerrits.nl");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public static final File DIRECTORY = new File("G:" + File.separator + "TV Series");
    public static final String SEASON_INFO_NAME = "season_info.json";
    
    public static final Function<String, String> SEASON_INFO_PATH = (t) -> DIRECTORY + File.separator + t + File.separator + SEASON_INFO_NAME;
    public static final Pattern SHOW_PATTERN = Pattern.compile("([^\\s]+)_season_(0?[0-9]|[0-9][1-9])");
    public static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    
    public static List<Pair<File, SeasonInfo>> getAllSeasonInfo()
    {
        List<Pair<File, SeasonInfo>> seasonInfoList = new ArrayList<>();
        
        File[] directories = DIRECTORY.listFiles(File::isDirectory);
        
        for (File dir : Objects.requireNonNull(directories))
        {
            Matcher matcher = SHOW_PATTERN.matcher(dir.getName());
            if (matcher.matches())
            {
                if (Files.exists(Paths.get(SEASON_INFO_PATH.apply(dir.getName()))))
                {
                    try
                    {
                        String content = new String(Files.readAllBytes(Paths.get(SEASON_INFO_PATH.apply(dir.getName()))));
                        if (!content.isEmpty())
                        {
                            SeasonInfo seasonInfo = GSON.fromJson(content, SeasonInfo.class);
                            fixSeasonMetadata(seasonInfo, dir);
                            seasonInfoList.add(Pair.of(dir, seasonInfo));
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return seasonInfoList;
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void fixSeasonMetadata(SeasonInfo info, File dir)
    {
        try
        {
            Map<Integer, File> toRename = new HashMap<>();
            
            Response<List<Episode>> response = TRAKT_TV.seasons().season(info.getImdbId(), info.getSeason(), Extended.FULLEPISODES).execute();
            
            if (response.isSuccessful())
            {
                Map<Integer, String> map = new HashMap<>();
                
                File[] episodeFiles = dir.listFiles(f -> f.getName().startsWith("episode_"));
                assert episodeFiles != null;
                for (File f : episodeFiles)
                {
                    String[] details = f.getName().replace(".mkv", "").split("_");
                    int number = Integer.parseInt(details[1]);
                    // System.out.println(Arrays.toString(details));
                    if (details.length == 2)
                    {
                        // System.out.println(f.getPath().replace(".mkv", ""));
                        toRename.put(number, f);
                    }
                    map.put(number, f.getName());
                }
                
                List<nl.larsgerrits.tvshows.Episode> episodes = new ArrayList<>();
                assert response.body() != null;
                for (com.uwetrottmann.trakt5.entities.Episode episode : response.body())
                {
                    if (episode.title != null && !episode.title.isEmpty())
                    {
                        nl.larsgerrits.tvshows.Episode ep = new nl.larsgerrits.tvshows.Episode();
                        ep.setEpisode(episode.number);
                        ep.setTitle(episode.title);
                        ep.setReleaseDate(episode.first_aired.withOffsetSameInstant(ZoneOffset.of(OffsetDateTime.now().getOffset().toString())));
                        if (map.containsKey(episode.number)) ep.setFileName(map.get(episode.number));
                        if (toRename.keySet().contains(episode.number))
                        {
                            String fileName = "episode_" + String.format("%02d", episode.number) + "_" + FileUtils.fixFileName(episode.title) + ".mkv";
                            ep.setFileName(fileName);
                            File f = toRename.get(episode.number);
                            String newPath = f.getParent() + "\\" + fileName;
                            f.renameTo(new File(newPath));
                        }
                        episodes.add(ep);
                    }
                }
                info.setEpisodes(episodes);
            }
            
            Response<Show> showResponse = TRAKT_TV.shows().summary(info.getImdbId(), null).execute();
            if (showResponse.isSuccessful())
            {
                assert showResponse.body() != null;
                info.setTitle(showResponse.body().title);
            }
            
            String json = GSON.toJson(info);
            
            try (FileWriter writer = new FileWriter(Paths.get(SEASON_INFO_PATH.apply(dir.getName())).toFile()))
            {
                writer.write(json);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static String fixFileName(String filename)
    {
        return stripDiacritics(filename.toLowerCase())//
                                                      .replace(' ', '_')//
                                                      .replace('-', '_')//
                                                      .replaceAll("[^a-zA-Z_\\d]+", "")//
                                                      .replaceAll("_{2,}", "_");
    }
    
    private static String stripDiacritics(String str)
    {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
        return str;
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeNewSeasonInfo(String imdbId, int season, String title)
    {
        try
        {
            SeasonInfo info = new SeasonInfo(imdbId, season);
            String json = GSON.toJson(info);
            File dir = new File(DIRECTORY + File.separator + fixFileName(title) + "_season_" + season);
            dir.mkdir();
            Files.write(Paths.get(SEASON_INFO_PATH.apply(fixFileName(title) + "_season_" + season)), json.getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
