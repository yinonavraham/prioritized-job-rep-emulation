package ty.tech.prioritizedJobRep.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;


public class Logger
{
//	private static Logger __instance = null;
	
	private Logger() {}
	
	static
	{
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger("ty.tech.prioritizedJobRep");
		Handler handler;
		try
		{
			// Log file name, e.g: prijobrep.0.log , prijobrep.1.log 
			String filenamePattern = "prijobrep.%g.log";
			// Rotation over 10 files of at most 256KB, appending to the current file
			handler = new FileHandler(filenamePattern, 256*1024, 10, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			handler = new ConsoleHandler();
		}
		SimpleFormatter formatter = new SimpleFormatter();
		handler.setFormatter(formatter);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		logger.finer(
			"\n\n#####################################################################\n" +
			"### Started: prioritizedJobRep                                    ###\n" +
			"#####################################################################\n");
	}
	
//	public static Logger getLogger()
//	{
//		if (__instance == null)
//		{
//			__instance = new Logger();
//		}
//		return __instance;
//	}
	
	public static Location getLocation(Class<?> cls)
	{
		return new Location(cls);
	}
	
	public static void main(String[] args)
	{
		Location location = Logger.getLocation(Logger.class);
		location.entering("main");
		location.debug("debug");
		location.info("info");
		location.warning("warning");
		location.severe("severe");
		location.throwing("main", new Exception("error"));
		location.exiting("main");
	}
}
