package nl.larsgerrits.tvshows.command;

import nl.larsgerrits.tvshows.SeasonInfo;
import nl.larsgerrits.tvshows.util.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandWatch extends Command
{
    
    @Override
    public void execute(String[] args)
    {
        String showName = "";
        int season = 1;
        int episode = 1;
        if (args.length >= 1) showName = args[0];
        if (args.length >= 2) season = Integer.parseInt(args[1]);
        if (args.length >= 2) episode = Integer.parseInt(args[2]);
        
        Pattern search = Pattern.compile(showName.replace(" ", ".*").toLowerCase());
        
        List<Pair<File, SeasonInfo>> seasonInfos = FileUtils.getAllSeasonInfo();
        for (int i = 0; i < seasonInfos.size(); i++)
        {
            Pair<File, SeasonInfo> pair = seasonInfos.get(i);
            Matcher matcher = search.matcher(pair.getRight().getTitle().toLowerCase());
            if (matcher.matches() && pair.getRight().getSeason() == season)
            {
                // System.out.println(pair.getRight().getEpisodes().get(episode - 1).getFileName());
                // System.out.println(pair.getLeft().getPath() + "\\" + pair.getRight().getEpisodes().get(episode - 1).getFileName());
                try
                {
                    Desktop.getDesktop().open(new File(pair.getLeft().getPath() + "\\" + pair.getRight().getEpisodes().get(episode - 1).getFileName()));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    // }
                }
            }
            
        }
    }
}
