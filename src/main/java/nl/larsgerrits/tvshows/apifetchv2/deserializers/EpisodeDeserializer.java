package nl.larsgerrits.tvshows.apifetchv2.deserializers;

import com.google.gson.*;
import nl.larsgerrits.tvshows.apifetchv2.models.EpisodeModel;
import nl.larsgerrits.tvshows.apifetchv2.models.Torrent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EpisodeDeserializer implements JsonDeserializer<EpisodeModel>
{
    private static List<String> RESOLUTIONS = Arrays.asList("0", "480p", "720p", "1080p");
    
    private Gson gson = new Gson();
    
    @Override
    public EpisodeModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject obj = json.getAsJsonObject();
        
        int tvdb_id = obj.get("tvdb_id").getAsInt();
        int season = obj.get("season").getAsInt();
        int episode = obj.get("episode").getAsInt();
        String title = obj.get("title").getAsString();
        String overview = obj.get("overview").isJsonNull() ? "" : obj.get("overview").getAsString();
        boolean date_based = obj.get("date_based").getAsBoolean();
        int first_aired = obj.get("first_aired").getAsInt();
        List<Torrent> torrents = new ArrayList<>();
        
        JsonObject torrentsObj = obj.getAsJsonObject("torrents");
        
        for (String resolution : RESOLUTIONS)
        {
            if (torrentsObj.has(resolution))
            {
                Torrent torrent = gson.fromJson(torrentsObj.get(resolution), Torrent.class);
                if (torrent != null)
                {
                    torrent.setResolution(resolution);
                    torrents.add(torrent);
                }
            }
        }
        return new EpisodeModel(tvdb_id, season, episode, title, overview, date_based, first_aired, torrents);
    }
}