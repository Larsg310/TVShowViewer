package nl.larsgerrits.tvshows;

import java.util.List;

@SuppressWarnings("unused")
public class SeasonInfo
{
    private String title = "";
    private String imdbId = "";
    private int season;
    private List<Episode> episodes;
    
    public String getImdbId()
    {
        return imdbId;
    }
    
    public int getSeason()
    {
        return season;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public List<Episode> getEpisodes()
    {
        return episodes;
    }
    
    @Override
    public String toString()
    {
        return "SeasonInfo{" + "title='" + title + '\'' + ", imdbId='" + imdbId + '\'' + ", season=" + season + ", episodes=" + episodes + '}';
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public void setEpisodes(List<Episode> episodes)
    {
        this.episodes = episodes;
    }
    
    public boolean isComplete()
    {
        if (episodes == null) return false;
        for (Episode episode : episodes) if (episode.getFileName().isEmpty()) return false;
        return !title.isEmpty() && !imdbId.isEmpty() && season > 0;
    }
    
    public SeasonInfo()
    {
    }
    
    public SeasonInfo(String imdbId, int season)
    {
        this.imdbId = imdbId;
        this.season = season;
    }
}
