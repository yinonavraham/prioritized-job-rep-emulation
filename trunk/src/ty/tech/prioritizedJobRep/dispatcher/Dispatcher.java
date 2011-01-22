package ty.tech.prioritizedJobRep.dispatcher;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.Job;

public interface Dispatcher extends Remote
{
	
	/**
	 * Stop the dispatcher.
	 */
	void stop() throws RemoteException;
	
	/**
	 * Adds a server to the active servers list
	 * @param endPoint
	 */
	void registerServer(EndPoint endPoint) throws RemoteException, NotBoundException;
	
	/**
	 * Removes the server from the active servers list
	 * @param endPoint
	 */
	void removeServer(EndPoint endPoint) throws RemoteException;
	
	/**
	 * Adds a new job to the dispatcher
	 * @param job
	 */
	void addJob(Job job) throws RemoteException;	

}
