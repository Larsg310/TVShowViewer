package nl.larsgerrits.tvshows;

import java.util.List;

public class Season
{
    private List<Episode> episodes;
    private int seasonNumber;
    
    public Season(int seasonNumber, List<Episode> episodes)
    {
        this.seasonNumber = seasonNumber;
        this.episodes = episodes;
    }
    
    public int getSeasonNumber()
    {
        return seasonNumber;
    }
}
