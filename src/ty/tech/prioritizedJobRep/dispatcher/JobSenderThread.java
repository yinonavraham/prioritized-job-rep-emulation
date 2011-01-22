package ty.tech.prioritizedJobRep.dispatcher;

import java.util.ArrayList;
import java.util.Random;

import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.Priority;
import ty.tech.prioritizedJobRep.server.Server;

public class JobSenderThread extends Thread 
{
	public JobSenderThread()
	{
		
	}
	
	public void run() 
	{
		Job hpJob = null;
		Job lpJob = null;
		
		while(true)
		{
			Job job = null;
			job = DispatcherImpl.getIncomingJobsQueue().pop();
			if (null != hpJob)
			{
				//copy the job
				hpJob = job;
				lpJob = job;
				
				//set matching priorities
				hpJob.setPriority(Priority.High);
				lpJob.setPriority(Priority.Low);
				
				// get all available servers
				ArrayList<Server> servers = DispatcherImpl.getActiveServers();
				
				// get random 2 servers out of these
				int idx1 = -1, idx2 = -1;
				boolean con1 = false, con2 = false;
			    Random randomGenerator = new Random();
			    while (idx1 == idx2 && con1 && con2)
			    {
			    	idx1 = randomGenerator.nextInt(servers.size());
			    	con1 = servers.get(idx1).ping();
			    	if (con1)
			    	{
				    	idx2 = randomGenerator.nextInt(servers.size());
				    	con2 = servers.get(idx2).ping();			    		
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
					System.err.println("Error occured in JobSenderThread run: " + e.getMessage());
				} 

			}
			try 
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e) 
			{
				System.err.println("Error occured in JobsGeneratorThread sleep: " + e.getMessage());
			}
		}
	}
}
