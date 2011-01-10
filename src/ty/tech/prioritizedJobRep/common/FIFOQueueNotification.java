package ty.tech.prioritizedJobRep.common;


public class FIFOQueueNotification
{
	public enum Type { NotEmpty }
	
	private Type _type;
	private String _msg;
	private FIFOQueue _source;
	
	public FIFOQueueNotification(FIFOQueue source, Type type, String message)
	{
		_source = source;
		_type = type;
		_msg = message;
	}
	
	
	public FIFOQueue getSource()
	{
		return _source;
	}
	
	public Type getType()
	{
		return _type;
	}
	
	
	public String getMessage()
	{
		return _msg;
	}
	
	@Override
	public String toString()
	{
		return _type + " (" + _msg + ")";
	}
}
