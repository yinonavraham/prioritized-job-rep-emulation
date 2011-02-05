package ty.tech.prioritizedJobRep.server;

import ty.tech.prioritizedJobRep.api.ProxyFactory;
import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.FIFOQueue;
import ty.tech.prioritizedJobRep.common.FIFOQueueListener;
import ty.tech.prioritizedJobRep.common.FIFOQueueNotification;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.Priority;
import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;
import ty.tech.prioritizedJobRep.server.JobNotification.Type;


public class Executor extends Thread implements FIFOQueueListener
{
	private Location _location = Logger.getLocation(this.getClass());
	private Job _currJob;
	private boolean _finished = false;
	private boolean _abortJob = false;
	private ServerImpl _server;
	private Object _lockJob = new Object();
	
	
	public Executor(ServerImpl server)
	{
		_server = server;
		setDaemon(true);
		setName("Executor");
		for (Priority p : Priority.values())
		{
			server.getQueue(p).addListener(this);
		}
	}
	
	
	public synchronized void abortJob()
	{
		_location.entering("abortJob()");
		Job currJob = _currJob;
		_abortJob = true;
		this.interrupt();
		System.out.println("Current job aborted: " + currJob);
		_location.exiting("abortJob()");
	}
	
	
	public synchronized boolean abortJob(Job job)
	{
		_location.entering("abortJob(job)",job);
		boolean aborted = false;
		synchronized (_lockJob)
		{
			if (isCurrentJob(job))
			{
				abortJob();
				aborted = true;
			}
			
		}
		_location.exiting("abortJob()",aborted);
		return aborted;
	}
	
	
	public boolean isCurrentJob(Job job)
	{
		synchronized (_lockJob)
		{
			return _currJob != null && _currJob.equals(job);
		}
	}
	
	
	public Job getCurrentJob()
	{
		synchronized (_lockJob)
		{
			return _currJob;
		}
	}
	
	
	private void setCurrentJob(Job job)
	{
		synchronized (_lockJob)
		{
			_currJob = job;
		}
	}
	
	
	public synchronized void stopExecutor()
	{
		_location.entering("processCurrentJob()");
		_finished = true;
		abortJob();
		this.interrupt();
		this.notifyAll();
		_location.exiting("stopExecutor()");
	}
	
	
	@Override
	public void run()
	{
		_location.entering("run()");
		
		FIFOQueue hpQueue = _server.getQueue(Priority.High);
		FIFOQueue lpQueue = _server.getQueue(Priority.Low);
		while (!_finished)
		{
			if (!hpQueue.isEmpty())
			{
				Job job = hpQueue.pop();
				if (!_server.isJobMarkedToAbort(job))
				{
					setCurrentJob(job);
					_abortJob = false;
					processCurrentJob();
				}
				else System.out.println("Job was marked to be aborted: " + job);
			}
			else if (!lpQueue.isEmpty())
			{
				Job job = lpQueue.pop();
				if (!_server.isJobMarkedToAbort(job))
				{
					setCurrentJob(job);
					_abortJob = false;
					processCurrentJob();
				}
				else System.out.println("Job was marked to be aborted: " + job);
			}
			try { sleep(1); } catch (InterruptedException e) {}
		}
		
		_location.exiting("run()");
	}
	

	private void processCurrentJob()
	{
		_location.entering("processCurrentJob()", _currJob);
		System.out.println("Processing job: " + _currJob);
		notifySiblings(new JobNotification(_currJob,Type.Started));
		_server.getStatisticsInternal().jobExecutionStarted(_currJob.getPriority());
		long time = 0;
		long execTime = _currJob.getExecutionTime();
		while (!_abortJob && time < execTime)
		{
			try
			{
				sleep(10);
			}
			catch (InterruptedException e)
			{
				_location.throwing("processCurrentJob()", e);
			}
			time += 10;
		}
		if (!_abortJob) 
		{
			System.out.println("Finished job: " + _currJob);
			_server.sendJobBack(_currJob);
			notifySiblings(new JobNotification(_currJob,Type.Finished));
			_server.getStatisticsInternal().jobExecutionFinished(_currJob.getPriority());
		}
		else 
		{
			System.out.println("Job execution aborted: " + _currJob);
			notifySiblings(new JobNotification(_currJob,Type.Aborted));
			_server.getStatisticsInternal().jobExecutionAborted(_currJob.getPriority());
		}
		_location.exiting("processCurrentJob()");
	}

	
	private void notifySiblings(final JobNotification notification)
	{
		_location.entering("notifySiblings(notification)", notification);
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for (EndPoint ep : notification.getJob().getSiblingsLocations())
				{
					try
					{
						Server server = ProxyFactory.createServerProxy(ep);
						server.processSiblingNotification(notification);
						server = null;
					}
					catch (Exception e)
					{
						System.err.println(
							"Failed to notify to job's sibling on: " + ep 
							+ ". Reason: " + e.getMessage());
						_location.throwing("notifySiblings(JobNotification)", e);
					}
				}
			}
		});
		t.setDaemon(true);
		t.setName("SiblingsNotificationDispatcher");
		t.start();
		_location.exiting("notifySiblings(notification)");
	}
	

	@Override
	public void processFIFOQueueNotification(FIFOQueueNotification notification)
	{
		_location.entering("processFIFOQueueNotification(FIFOQueueNotification)",notification);
		FIFOQueue queue = notification.getSource();
		/* If there is a job in progress which is not with high priority and the job in the
		 * queue is with high priority - abort the current job.  
		 */
		synchronized (queue)
		{
			Job currJob = getCurrentJob();
			Job peek = queue.peek();
			if (currJob != null && !Priority.High.equals(currJob.getPriority()) && 
				Priority.High.equals(peek == null ? null : peek.getPriority()))
			{
				abortJob();
				// Reenter the aborted job
				_server.reenterJob(currJob);
			}
		}
		_location.exiting("processFIFOQueueNotification(FIFOQueueNotification)");
	}
}
