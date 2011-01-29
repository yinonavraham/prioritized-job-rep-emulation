package ty.tech.prioritizedJobRep.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import ty.tech.prioritizedJobRep.util.NetUtils;


public class EndPoint implements Serializable
{
	private static final long serialVersionUID = -1733048462463558997L;
	private static final int MAX_PORT = (int)(Math.pow(2, 16)-1);

	private InetAddress _address;
	private String _hostName;
	private int _port;
	
	public EndPoint(InetAddress address, int port)
	{
		if (address == null)
			throw new IllegalArgumentException("address cannot be null nor empty");
		if (port < 0 || port > MAX_PORT)
			throw new IllegalArgumentException(port + ": Illegal port number. Port number must be in the domain: [0.." + MAX_PORT + "]");
		_address = address;
		_hostName = address.getHostName();
		_port = port;
	}
	
	public EndPoint(String hostAddress, int port) throws UnknownHostException
	{
		_hostName = hostAddress;
		_address = InetAddress.getByName(_hostName);
		_port = port;
	}	
	
	
	public EndPoint(int port) throws SocketException, UnknownHostException
	{
		this(NetUtils.detectLocalActiveIP(), port);
		_hostName = InetAddress.getLocalHost().getHostName();
	}

	
	public String getHostName()
	{
		return _hostName;
	}

	
	public String getHostAddress()
	{
		return _address.getHostAddress();
	}
	
	
	public InetAddress getInetAddress()
	{
		return _address;
	}
	
	
	public byte[] getAddress()
	{
		return _address.getAddress();
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
			return _address.equals(ep.getAddress()) && _port == ep.getPort();  
		}
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
		return (int)(((long)_address.hashCode() * (long)_port) % Integer.MAX_VALUE);
	}
}
