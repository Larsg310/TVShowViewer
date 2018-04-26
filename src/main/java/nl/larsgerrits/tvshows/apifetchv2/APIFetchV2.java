package nl.larsgerrits.tvshows.apifetchv2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.tvshows.apifetchv2.deserializers.EpisodeDeserializer;
import nl.larsgerrits.tvshows.apifetchv2.models.EpisodeModel;
import nl.larsgerrits.tvshows.apifetchv2.models.ShowInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIFetchV2
{
    private static Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(EpisodeModel.class, new EpisodeDeserializer()).create();
    
    public static ShowInfo getShowInfo(String imdbId)
    {
        return gson.fromJson(GET("https://tv-v2.api-fetch.website/show/" + imdbId), ShowInfo.class);
    }
    
    private static String GET(String url)
    {
        try
        {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            // System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK)
            { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();
                
                // print result
                // System.out.println(response.toString());
                return response.toString();
            }
            else System.out.println("GET request not worked");
            
        }
        catch (IOException e) {e.printStackTrace();}
        return "";
    }
}
