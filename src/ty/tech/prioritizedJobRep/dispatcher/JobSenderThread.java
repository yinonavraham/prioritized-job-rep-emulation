package ty.tech.prioritizedJobRep.dispatcher;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.Priority;
import ty.tech.prioritizedJobRep.logging.Logger;
import ty.tech.prioritizedJobRep.server.Server;

public class JobSenderThread extends Thread 
{
	private boolean _isDispatcherRunning;
	private DispatcherPolicy _policy;
	private Object _policyLock = new Object();
	
	public JobSenderThread(DispatcherPolicy policy)
	{
		_isDispatcherRunning = true;
		_policy = policy;
	}
	
	public void run() 
	{
		Job hpJob = null;
		Job lpJob = null;
		
		while(_isDispatcherRunning)
		{
			Job job = null;
			job = DispatcherImpl.getIncomingJobsQueue().pop();
			if (null != job)
			{
				//copy the job and set matching priorities
				if (getJobReplicationsNum(Priority.High) == 1)
				{
					System.out.println("Create HP replication");
					hpJob = job.clone(Priority.High);
					hpJob.setDispatcher(job.getDispatcher());
					hpJob.getStatistics().setStartTime(job.getStatistics().getStartTime());
				}
				if (getJobReplicationsNum(Priority.Low) == 1)
				{
					System.out.println("Create LP replication");
					lpJob = job.clone(Priority.Low);
					lpJob.setDispatcher(job.getDispatcher());
					lpJob.getStatistics().setStartTime(job.getStatistics().getStartTime());
				}
				
				// get all available servers
				ArrayList<Server> servers = DispatcherImpl.getActiveServers();
				
				// get random 2 servers out of these
				int idx1 = -1, idx2 = -1;
			    Random randomGenerator = new Random();
			    
			    // get first server index
			    while(idx1 == -1)
			    {
			    	try
			    	{
					    idx1 = randomGenerator.nextInt(servers.size());
					    servers.get(idx1).ping(); // will throw if there is no answer
			    	}
			    	catch(RemoteException e)
			    	{
			    		System.err.println("Cannot ping registered server");
			    		Logger.getLocation(this.getClass()).throwing("JobServerThread.run()", e);
			    	}			    	
			    }
			    
			    // If need to replicate
			    if (hpJob != null && lpJob != null)
			    {
				    // get the second server index until it is different than the first one
				    while(idx2 == -1 || idx1 == idx2)
				    {
				    	try
				    	{
						    idx2 = randomGenerator.nextInt(servers.size());
						    servers.get(idx2).ping(); // will throw if there is no answer
				    	}
				    	catch(RemoteException e)
				    	{
				    		System.err.println("Cannot ping registered server");
				    		Logger.getLocation(this.getClass()).throwing("JobServerThread.run()", e);
				    	}			    	
				    }
			    }
			    else // Only one instance of the job will be sent to a single server
			    {
			    	idx2 = idx1;
			    }

			    // send the jobs and move the original one to the inProcessQueue
			    try 
			    {
			    	if (hpJob != null)
			    	{
				    	if (idx1 != idx2) hpJob.addSiblingLocation(servers.get(idx2).getEndPoint());
						servers.get(idx1).putJob(hpJob);
			    	}
			    	if (lpJob != null)
			    	{
			    		if (idx1 != idx2) lpJob.addSiblingLocation(servers.get(idx1).getEndPoint());
						servers.get(idx2).putJob(lpJob);
			    	}
					DispatcherImpl.getInProgressJobsQueue().add(job);
			    }
			    catch(Exception e) 
				{
			    	e.printStackTrace();
					System.err.println("Error occured in JobSenderThread run: " + e.getMessage());
				} 
			}
			try 
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e) 
			{
				System.err.println("Dispatcher.JobSenderThread stopped");
			}
		}
		System.out.println("Dispatcher.JobSenderThread stopped");
		Logger.getLocation(this.getClass()).debug("Dispatcher.JobSenderThread stopped");
	}
	
	private int getJobReplicationsNum(Priority priority)
	{
		synchronized (_policyLock)
		{
			int num = _policy.getJobReplicationsNumber(priority);
			// Currently support only [0..1] replications of each priority
			num = Math.min(num, 1);
			System.out.println("RepNum(" + priority + ") = " + num);
			return num;
		}
	}
	
	public synchronized void setIsDispatcherRunning(boolean status)
	{
		_isDispatcherRunning = false;
	}
	
	public void setPolicy(DispatcherPolicy policy)
	{
		synchronized (_policyLock)
		{
			_policy = policy;
			System.out.println("JobSenderThread.setPolicy: " + _policy);
		}
	}
}
