package nl.larsgerrits.tvshows.command;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbTV;
import info.movito.themoviedbapi.model.tv.TvSeries;
import nl.larsgerrits.tvshows.util.FileUtils;

import java.util.List;
import java.util.Scanner;

public class CommandCreate extends Command
{
    private final TmdbApi api = new TmdbApi("83fed95ccc330d5b194e5039d40387d6");
    
    @Override
    public void execute(String[] args)
    {
        String searchQuery = args[0];
        List<TvSeries> results = api.getSearch().searchTv(searchQuery, "en", 1).getResults();
        for (TvSeries result : results)
        {
            TvSeries serie = api.getTvSeries().getSeries(result.getId(), "en", TmdbTV.TvMethod.external_ids);
            System.out.print("Did you mean: \"" + serie.getName() + "\" (" + serie.getFirstAirDate().split("-")[0] + ")? (Y/n): ");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();
            if (answer.toLowerCase().equals("y"))
            {
                String imdbId = serie.getExternalIds().getImdbId();
                int season = 0;
                if (serie.getNumberOfSeasons() == 1) {season = 1;}
                else
                {
                    while (season <= 0)
                    {
                        System.out.print("Which season? (1-" + serie.getNumberOfSeasons() + "): ");
                        String seasonAnswer = scanner.nextLine();
                        if (seasonAnswer.matches("\\d+"))
                        {
                            season = Integer.parseInt(seasonAnswer);
                            if (season > serie.getNumberOfSeasons()) season = 0;
                        }
                    }
                }
                FileUtils.writeNewSeasonInfo(imdbId, season, serie.getName());
            }
        }
    }
}
