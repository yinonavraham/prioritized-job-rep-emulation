package ty.tech.prioritizedJobRep.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ty.tech.prioritizedJobRep.common.Job;


public interface Server extends Remote
{
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
}
