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
	
	public JobSenderThread()
	{
		_isDispatcherRunning = true;
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
				hpJob = job.clone(Priority.High);
				lpJob = job.clone(Priority.Low);
				
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

			    // send the jobs and move the original one to the inProcessQueue
			    try 
			    {
					servers.get(idx1).putJob(hpJob);
					servers.get(idx2).putJob(lpJob);
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
	
	public void setIsDispatcherRunning(boolean status)
	{
		_isDispatcherRunning = false;
	}
}
