package ty.tech.prioritizedJobRep.dispatcher;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import ty.tech.prioritizedJobRep.api.ProxyFactory;
import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.FIFOQueue;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.JobResult;
import ty.tech.prioritizedJobRep.common.ServerStatistics;
import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;
import ty.tech.prioritizedJobRep.server.Server;

public class DispatcherImpl implements Dispatcher
{
	private EndPoint _endPoint;
	private static ArrayList<Server> _activeServers;
	private static ArrayList<Job> _inProgressJobsQueue;
	private static ArrayList<JobResult> _jobsResults;
	private Location _location = Logger.getLocation(this.getClass());
	private boolean _finished;
	private static FIFOQueue _incomingJobsQueue;
	
	private JobSenderThread _jobSenderThread = null;
	
	public DispatcherImpl(int port) throws SocketException, UnknownHostException
	{
		_endPoint = new EndPoint(port);
		_activeServers = new ArrayList<Server>();
		_jobsResults = new ArrayList<JobResult>();
		_finished = false; 
		_incomingJobsQueue = new FIFOQueue("Dispatcher's Incoming Jobs Queue");
		_inProgressJobsQueue = new ArrayList<Job>();
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
		_jobSenderThread.setIsDispatcherRunning(false);
		_location.exiting("stop()");	
	}
	
	@Override
	public synchronized void registerServer(EndPoint endPoint) throws RemoteException, NotBoundException
	{
		_location.entering("registerServer(EndPoint endPoint)", endPoint);
		
		// locate server's registry and keep it in active server list
        Server server = ProxyFactory.createServerProxy(endPoint);
        _activeServers.add(server);
        
        String msg = "Server " + endPoint.getHostName()+ ":" + endPoint.getPort()+ " was registered";
        System.out.println(msg);
       _location.debug(msg);

		_location.exiting("registerServer(EndPoint endPoint)");
	}
	
	@Override
	public synchronized void removeServer(EndPoint endPoint)
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
	public synchronized void addJob(Job job)
	{
		job.getStatistics().setStartTime();
		job.setDispatcher(_endPoint);
		_incomingJobsQueue.put(job);
		System.out.println("Dispatcher got job " + job.getID());
	}
	
	@Override
	public synchronized void keepJobResults(JobResult result)
	{
		result.setTotalTimeinSys(System.currentTimeMillis() - result.getStartTime());
		_jobsResults.add(result);
	}
	
	@Override
	public ArrayList<JobResult> getJobsResults()
	{
		return _jobsResults;
	}
	
	@Override
	public ArrayList<ServerStatistics> getServersStatistics() throws RemoteException
	{
		ArrayList<ServerStatistics> serversStatistics = new ArrayList<ServerStatistics>();
		
		for (int i = 0; i < getActiveServers().size(); ++i)
		{
			ServerStatistics serverStatistics = getActiveServers().get(i).getStatistics();
			serversStatistics.add(serverStatistics);
		}
		
		return serversStatistics;
	}
	
	@Override
	public void resetAllServers() throws RemoteException
	{
		for (int i = 0; i < getActiveServers().size() ; ++i)
		{
			Server server = getActiveServers().get(i);
			server.reset();
	        String msg = "Server" + server.getEndPoint().getHostName()+ ":" + server.getEndPoint().getPort()+ " was reset";
	        System.out.println(msg);
	       _location.debug(msg);			
		}
		
		// reset job results container as well
		_jobsResults.clear();
	}
	
	@Override
	public void stopAllServers() throws RemoteException
	{
		for (int i = 0; i < getActiveServers().size() ; ++i)
		{
			getActiveServers().get(i).stop();
		}
	}	
	
	public synchronized static FIFOQueue getIncomingJobsQueue()
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
