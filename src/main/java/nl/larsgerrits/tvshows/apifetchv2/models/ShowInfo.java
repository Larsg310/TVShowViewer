package nl.larsgerrits.tvshows.apifetchv2.models;

import java.util.List;

public class ShowInfo
{
    private String _id;
    private String imdb_id;
    private String title;
    private int year;
    private String slug;
    private String synopsis;
    private int runtime;
    private Rating rating;
    private Images images;
    private List<String> genres = null;
    private String type;
    private String tvdb_id;
    private String country;
    private String network;
    private String air_day;
    private String air_time;
    private String status;
    private int num_seasons;
    private long last_updated;
    private int latest_episode;
    private List<EpisodeModel> episodes;
    
    public String get_id()
    {
        return _id;
    }
    
    public String getImdb_id()
    {
        return imdb_id;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public int getYear()
    {
        return year;
    }
    
    public String getSlug()
    {
        return slug;
    }
    
    public String getSynopsis()
    {
        return synopsis;
    }
    
    public int getRuntime()
    {
        return runtime;
    }
    
    public Rating getRating()
    {
        return rating;
    }
    
    public Images getImages()
    {
        return images;
    }
    
    public List<String> getGenres()
    {
        return genres;
    }
    
    public String getType()
    {
        return type;
    }
    
    public String getTvdb_id()
    {
        return tvdb_id;
    }
    
    public String getCountry()
    {
        return country;
    }
    
    public String getNetwork()
    {
        return network;
    }
    
    public String getAir_day()
    {
        return air_day;
    }
    
    public String getAir_time()
    {
        return air_time;
    }
    
    public String getStatus()
    {
        return status;
    }
    
    public int getNum_seasons()
    {
        return num_seasons;
    }
    
    public long getLast_updated()
    {
        return last_updated;
    }
    
    public int getLatest_episode()
    {
        return latest_episode;
    }
    
    public List<EpisodeModel> getEpisodes()
    {
        return episodes;
    }
}
