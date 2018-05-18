package nl.larsgerrits.tvshows.command;

public class CommandInfo extends Command
{
    @Override
    public void execute(String[] args)
    {
        System.out.println("This is the info command");
    }
}
