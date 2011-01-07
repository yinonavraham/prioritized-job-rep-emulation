package ty.tech.prioritizedJobRep.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;


public class NetUtils
{
	public static final String SELF_LOOP_IP = "127.0.0.1";
	

	public static InetAddress detectLocalActiveIP() throws SocketException, UnknownHostException
	{
		Location location = Logger.getLocation(NetUtils.class);
		InetAddress serverActiveIP = null;
		// each server prints all his IP.
		// This is used so we can collect data about the participating servers
		// and choose a not-self-loop IP
		location.entering("detectLocalActiveIP()");//log("Started on (printing all local addresses) ", LM_Logger.SERVER);
		Enumeration<NetworkInterface> allLocalInterfaces = NetworkInterface.getNetworkInterfaces();
		location.debug("Started on (printing all local addresses) ");
		while (allLocalInterfaces.hasMoreElements())
		{
			NetworkInterface currInterface = (NetworkInterface)allLocalInterfaces.nextElement();
			Enumeration<InetAddress> currInterfaceAddresses = currInterface.getInetAddresses();
			while (currInterfaceAddresses.hasMoreElements())
			{
				InetAddress currAddress = (InetAddress)currInterfaceAddresses.nextElement();
				if ((serverActiveIP == null)
						&& (!currAddress.equals(InetAddress.getByName(SELF_LOOP_IP)))
						&& (currAddress.getAddress().length == 4))
				{
					serverActiveIP = currAddress;
				}
				location.debug(currAddress.toString());
			}
		}
		location.exiting("detectLocalActiveIP()", serverActiveIP);
		return serverActiveIP;
	}
}
