package nl.larsgerrits.tvshows.apifetchv2.models;

import java.util.List;

public class EpisodeModel
{
    private int tvdb_id;
    private int season;
    private int episode;
    private String title;
    private String overview;
    private boolean date_based;
    private int first_aired;
    private List<Torrent> torrents;
    
    public EpisodeModel(int tvdb_id, int season, int episode, String title, String overview, boolean date_based, int first_aired, List<Torrent> torrents)
    {
        this.tvdb_id = tvdb_id;
        this.season = season;
        this.episode = episode;
        this.title = title;
        this.overview = overview;
        this.date_based = date_based;
        this.first_aired = first_aired;
        this.torrents = torrents;
    }
    
    public int getTvdb_id()
    {
        return tvdb_id;
    }
    
    public int getSeason()
    {
        return season;
    }
    
    public int getEpisode()
    {
        return episode;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public String getOverview()
    {
        return overview;
    }
    
    public boolean isDate_based()
    {
        return date_based;
    }
    
    public int getFirst_aired()
    {
        return first_aired;
    }
    
    public List<Torrent> getTorrents()
    {
        return torrents;
    }
}
