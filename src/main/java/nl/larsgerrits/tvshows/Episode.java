package nl.larsgerrits.tvshows;

public class Episode
{
    private int episode;
    private String title = "";
    private String file_name = "";
    
    public int getEpisode()
    {
        return episode;
    }
    
    public Episode setEpisode(int episode)
    {
        this.episode = episode;
        return this;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public Episode setTitle(String title)
    {
        if (title == null) title = "";
        this.title = title;
        return this;
    }
    
    public String getFileName()
    {
        return file_name;
    }
    
    public Episode setFileName(String fileName)
    {
        this.file_name = fileName;
        return this;
    }
}
