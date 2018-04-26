package nl.larsgerrits.tvshows.apifetchv2.models;

public class Torrent
{
    private String provider;
    private int peers;
    private int seeds;
    private String url;
    private String resolution;
    
    public void setResolution(String resolution)
    {
        this.resolution = resolution;
    }
    
    public String getProvider()
    {
        return provider;
    }
    
    public int getPeers()
    {
        return peers;
    }
    
    public int getSeeds()
    {
        return seeds;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public String getResolution()
    {
        return resolution;
    }
}

