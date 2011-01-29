package ty.tech.prioritizedJobRep.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.ServerStatistics;


public interface Server extends Remote
{
	/**
	 * Get the end point identifier of the server
	 * @return the end point where the server is running
	 * @throws RemoteException
	 */
	EndPoint getEndPoint() throws RemoteException;
	
	/**
	 * Add a new job to the server's queue, according to the job's priority
	 * @param job - job to add
	 */
	void putJob(Job job) throws RemoteException;
	
	/**
	 * Abort a job, whether it's still in the queue or in process (according to the policy)
	 * @param job - job to be aborted
	 */
	void abortJob(Job job) throws RemoteException;
	
	/**
	 * Reset the server. 
	 * Use this method between load iterations in order to reset the server's statistics, 
	 * clear the queues, etc.  
	 */
	void reset() throws RemoteException;

	/**
	 * Stop the server.
	 */
	void stop() throws RemoteException;
	
	/**
	 * Register the server in a dispatcher
	 * @param host - host of the dispatcher
	 * @param port - listening port of the dispatcher on the host
	 */
	void register(String host, int port) throws RemoteException;
	
	/**
	 * Set the policy of the server
	 * @param policy
	 */
	void setPolicy(ServerPolicy policy) throws RemoteException;
	
	
	/**
	 * @return The statistics collected in the server
	 * @throws RemoteException
	 */
	ServerStatistics getStatistics() throws RemoteException;
	
	/**
	 * Indicates that the server is still running
	 */
	boolean ping() throws RemoteException;
	
	/**
	 * Process a notification from a job
	 * @param notification - The notification to process
	 * @throws RemoteException
	 */
	void processSiblingNotification(JobNotification notification) throws RemoteException;
}
