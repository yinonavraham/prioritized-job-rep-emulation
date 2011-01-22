package ty.tech.prioritizedJobRep.dispatcher;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.Entities;
import ty.tech.prioritizedJobRep.common.FIFOQueue;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;
import ty.tech.prioritizedJobRep.server.Server;

public class DispatcherImpl implements Dispatcher
{
	private static ArrayList<Server> _activeServers;
	private static ArrayList<Job> _inProgressJobsQueue;
	private Location _location = Logger.getLocation(this.getClass());
	private boolean _finished;
	private static FIFOQueue _incomingJobsQueue;
	
	JobSenderThread _jobSenderThread = null;
	
	//1. Incoming jobs queue
	//2. Jobs in progress queue - fifo queue
	//3. Jobs results queue
	//4. Jobs replicator & sender

	
	public DispatcherImpl(int port) throws SocketException, UnknownHostException
	{
		_activeServers = new ArrayList<Server>();
		_finished = false; 
		_incomingJobsQueue = new FIFOQueue("Dispatcher's Incoming Jobs Queue");
	}
	
	public void start()
	{
		_location.entering("start()");
		
		// start all listener threads
		_jobSenderThread = new JobSenderThread();
		_jobSenderThread.start();
		
		while (!isFinished())
		{
			try { Thread.sleep(2000); }
			catch (InterruptedException e) { }
		}
		_location.exiting("start()");
	}
	
	@Override
	public void stop()
	{
		_location.entering("stop()");
		_finished = true;
		//stop all running threads
		Thread.currentThread().interrupt();
		_jobSenderThread.interrupt();
		_location.exiting("stop()");
	}
	
	@Override
	public synchronized void registerServer(EndPoint endPoint) throws RemoteException, NotBoundException
	{
		_location.entering("registerServer(EndPoint endPoint)", endPoint);
		
		// locate server's registry and keep it in active server list
        Registry registry = LocateRegistry.getRegistry(endPoint.getHostAddress());
        Server server = (Server) registry.lookup(Entities.SERVER);
        _activeServers.add(server);

		_location.exiting("registerServer(EndPoint endPoint)");
	}
	
	@Override
	public void removeServer(EndPoint endPoint)
	{
		_location.entering("removeServer(EndPoint endPoint)", endPoint);
		
		for (int i = 0; i < _activeServers.size(); ++i)
		{
			if (_activeServers.get(i).equals(endPoint))
			{
				_activeServers.remove(i);
			}
		}
		
		_location.exiting("removeServer(EndPoint endPoint)");
	}	
	
	@Override
	public void addJob(Job job)
	{
		_incomingJobsQueue.put(job);
		System.out.println("Dispatcher got job " + job.getID());
	}
	
	public static FIFOQueue getIncomingJobsQueue()
	{
		return _incomingJobsQueue;
	}
	
	public synchronized static ArrayList<Server> getActiveServers()
	{
		return _activeServers;
	}
	
	public synchronized static ArrayList<Job> getInProgressJobsQueue()
	{
		return _inProgressJobsQueue;
	}	
	
	private boolean isFinished()
	{
		return _finished;
	}	

}
