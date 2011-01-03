package ty.tech.prioritizedJobRep.logging;

import java.util.logging.Level;


public class Location
{
	private java.util.logging.Logger _logger = null;
	private String _cat;
	
	protected Location(Class<?> cls)
	{
		if (cls == null) throw new IllegalArgumentException("cls cannot be null");
		_cat = cls.getName();
		_logger = java.util.logging.Logger.getLogger(_cat);
	}
	
	public void log(Level level, String msg)
	{
		_logger.log(level, msg);
	}
	
	public void log(Level level, String msg, Object... params)
	{
		_logger.log(level, msg, params);
	}
	
	public void log(Level level, String msg, Throwable thrown)
	{
		_logger.log(level, msg, thrown);
	}
	
	public void logp(Level level, String methodName, String msg)
	{
		_logger.logp(level, _cat, methodName, msg);
	}
	
	public void logp(Level level, String methodName, String msg, Object... params)
	{
		_logger.logp(level, _cat, methodName, msg, params);
	}
	
	public void logp(Level level, String methodName, String msg, Throwable thrown)
	{
		_logger.logp(level, _cat, methodName, msg, thrown);
	}
	
	public void entering(String methodName, Object... params)
	{
		if (params == null || params.length == 0) _logger.entering(_cat, methodName);
		else _logger.entering(_cat, methodName, params);
	}
	
	public void exiting(String methodName)
	{
		_logger.exiting(_cat, methodName);
	}
	
	public void exiting(String methodName, Object returnValue)
	{
		_logger.exiting(_cat, methodName, returnValue);
	}
	
	public void debug(String msg)
	{
		_logger.finer(msg);
	}
	
	public void info(String msg)
	{
		_logger.info(msg);
	}
	
	public void throwing(String methodName, Throwable thrown)
	{
		_logger.throwing(_cat, methodName, thrown);
	}
	
	public void severe(String msg)
	{
		_logger.severe(msg);
	}
	
	public void warning(String msg)
	{
		_logger.warning(msg);
	}
}
