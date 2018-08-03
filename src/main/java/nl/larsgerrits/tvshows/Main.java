package nl.larsgerrits.tvshows;

import nl.larsgerrits.tvshows.command.CommandParser;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Main
{
    public static void main(String[] args)
    {
        disableWarning();
        // System.setErr(new PrintStream(new OutputStream()
        // {
        //     public void write(int b)
        //     {
        //     }
        // }));
        CommandParser.parse(args);
        System.exit(0);
    }
    
    private static void disableWarning()
    {
        try
        {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);
            
            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        }
        catch (Exception e)
        {
            // ignore
        }
    }
}