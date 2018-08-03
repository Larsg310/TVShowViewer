package nl.larsgerrits.tvshows;

import org.threeten.bp.OffsetDateTime;

public class Episode
{
    private int episode;
    private String title = "";
    private String file_name = "";
    private OffsetDateTime releaseDate = OffsetDateTime.MIN;
    
    public int getEpisode()
    {
        return episode;
    }
    
    public void setEpisode(int episode)
    {
        this.episode = episode;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setTitle(String title)
    {
        if (title == null) title = "";
        this.title = title;
    }
    
    public String getFileName()
    {
        return file_name;
    }
    
    public void setFileName(String fileName)
    {
        this.file_name = fileName;
    }
    
    public void setReleaseDate(OffsetDateTime releaseDate)
    {
        this.releaseDate = releaseDate;
    }
    
    public OffsetDateTime getReleaseDate()
    {
        return releaseDate;
    }
}
