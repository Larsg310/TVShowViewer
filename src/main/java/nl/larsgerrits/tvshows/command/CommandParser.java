package nl.larsgerrits.tvshows.command;

import java.util.*;

public final class CommandParser
{
    private static Map<List<String>, Command> commandMap = new HashMap<>();
    
    public static final Command INFO = new CommandInfo();
    public static final Command UPDATE = new CommandUpdate();
    public static final Command CREATE = new CommandCreate();
    
    static
    {
        addCommand(INFO, "info");
        addCommand(UPDATE, "update");
        addCommand(CREATE, "create");
    }
    
    @SuppressWarnings("unchecked")
    private static void addCommand(Command command, String name, String... aliases)
    {
        commandMap.put(new ArrayList(Arrays.asList(name, aliases)), command);
    }
    
    public static void parse(String[] args)
    {
        if (args == null || args.length == 0)
        {
            INFO.execute(null);
            return;
        }
        
        for (Map.Entry<List<String>, Command> entry : commandMap.entrySet())
        {
            if (entry.getKey().contains(args[0]))
            {
                entry.getValue().execute(args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : null);
                return;
            }
        }
        INFO.execute(null);
    }
    
}
