package ty.tech.prioritizedJobRep.api;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ty.tech.prioritizedJobRep.client.Client;
import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.Entities;
import ty.tech.prioritizedJobRep.dispatcher.Dispatcher;
import ty.tech.prioritizedJobRep.server.Server;


public class ProxyFactory
{
	
	/**
	 * Create a proxy for a server 
	 * @param host - the host where the server is located
	 * @param port - the listening port of the server
	 * @return a proxy to the server
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static Server createServerProxy(String host, int port) throws AccessException, RemoteException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(host, port);
		return (Server)registry.lookup(Entities.SERVER);
	}
	

	/**
	 * Create a proxy for a server 
	 * @param ep - the end point of the server
	 * @return a proxy to the server
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static Server createServerProxy(EndPoint ep) throws AccessException, RemoteException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(ep.getHostAddress(), ep.getPort());
		return (Server)registry.lookup(Entities.SERVER);
	}

	
	/**
	 * Create a proxy for a dispatcher 
	 * @param host - the host where the dispatcher is located
	 * @param port - the listening port of the dispatcher
	 * @return a proxy to the dispatcher
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static Dispatcher createDispatcherProxy(String host, int port) throws AccessException, RemoteException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(host, port);
		return (Dispatcher)registry.lookup(Entities.DISPATCHER);
	}
	

	/**
	 * Create a proxy for a dispatcher 
	 * @param ep - the end point of the dispatcher
	 * @return a proxy to the dispatcher
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static Dispatcher createDispatcherProxy(EndPoint ep) throws AccessException, RemoteException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(ep.getHostAddress(), ep.getPort());
		return (Dispatcher)registry.lookup(Entities.DISPATCHER);
	}
	

	/**
	 * Create a proxy for a client 
	 * @param ep - the end point of the client
	 * @return a proxy to the client
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static Client createClientProxy(EndPoint ep) throws AccessException, RemoteException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(ep.getHostAddress(), ep.getPort());
		return (Client)registry.lookup(Entities.CLIENT);
	}
}
