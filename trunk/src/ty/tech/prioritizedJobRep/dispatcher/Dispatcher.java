package ty.tech.prioritizedJobRep.dispatcher;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.JobResult;
import ty.tech.prioritizedJobRep.common.ServerStatistics;

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
	
	/**
	 * Gets a job's result from a server and keeps it
	 * @param result
	 */
	void keepJobResults(JobResult result) throws RemoteException;
	
	/**
	 * Returns a map with the result of all jobs in the iteration
	 */
	ArrayList<JobResult> getJobsResults() throws RemoteException;
	
	/**
	 * Get a list of all servers statistics in an iteration
	 */
	ArrayList<ServerStatistics> getServersStatistics() throws RemoteException;
	
	/**
	 * Resets all servers from their current iteration's jobs processing
	 * @throws RemoteException
	 */
	void resetAllServers() throws RemoteException;
	
	/**
	 * Stops all servers
	 * @throws RemoteException
	 */
	void stopAllServers() throws RemoteException;

}
