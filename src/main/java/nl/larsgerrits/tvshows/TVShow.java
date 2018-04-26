package nl.larsgerrits.tvshows;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TVShow
{
    private String title;
    
    private List<Season> seasons = new ArrayList<>();
    
    public TVShow(String displayName)
    {
        this.title = displayName;
    }
    
    public void addSeason(Season season)
    {
        seasons.add(season);
        seasons.sort(Comparator.comparing(Season::getSeasonNumber));
    }
}
