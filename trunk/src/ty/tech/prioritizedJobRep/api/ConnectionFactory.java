package ty.tech.prioritizedJobRep.api;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ty.tech.prioritizedJobRep.common.Entities;
import ty.tech.prioritizedJobRep.server.Server;


public class ConnectionFactory
{
	public static Server createServerConnection(String host, int port) throws AccessException, RemoteException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(host, port);
		return (Server)registry.lookup(Entities.SERVER);
	}
}
