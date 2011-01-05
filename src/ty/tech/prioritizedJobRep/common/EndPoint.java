package ty.tech.prioritizedJobRep.common;

import java.io.Serializable;


public class EndPoint implements Serializable
{
	private static final long serialVersionUID = -1733048462463558997L;
	private static final int MAX_PORT = (int)(Math.pow(2, 16)-1);

	private String _hostName;
	private int _port;
	
	public EndPoint(String hostName, int port)
	{
		if (hostName == null || hostName.isEmpty())
			throw new IllegalArgumentException("host name cannot be null nor empty");
		if (port < 0 || port > MAX_PORT)
			throw new IllegalArgumentException(port + ": Illegal port number. Port number must be in the domain: [0.." + MAX_PORT + "]");
		_hostName = hostName;
		_port = port;
	}

	
	public String getHostName()
	{
		return _hostName;
	}

	
	public int getPort()
	{
		return _port;
	}
	
	
	@Override
	public String toString()
	{
		return _hostName + ":" + _port;
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof EndPoint)
		{
			EndPoint ep = (EndPoint)obj;
			return _hostName.equals(ep.getHostName()) && _port == ep.getPort();  
		}
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
		return (int)(((long)_hostName.hashCode() * (long)_port) % Integer.MAX_VALUE);
	}
}
