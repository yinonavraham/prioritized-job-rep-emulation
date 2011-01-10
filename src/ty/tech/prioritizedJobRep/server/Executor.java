package ty.tech.prioritizedJobRep.server;

import ty.tech.prioritizedJobRep.common.FIFOQueue;
import ty.tech.prioritizedJobRep.common.FIFOQueueListener;
import ty.tech.prioritizedJobRep.common.FIFOQueueNotification;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.Priority;
import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;


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
		System.out.println("Aborting current job...");
		_abortJob = true;
		this.interrupt();
		System.out.println("Current job aborted");
		_location.exiting("abortJob()");
	}
	
	public synchronized void abortJob(Job job)
	{
		_location.entering("abortJob(job)",job);
		synchronized (_lockJob)
		{
			if (isCurrentJob(job))
			{
				abortJob();
			}
		}
		_location.exiting("abortJob()");
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
				setCurrentJob(hpQueue.pop());
				_abortJob = false;
				// TODO: Notify LP job
				processCurrentJob();
			}
			else if (!lpQueue.isEmpty())
			{
				setCurrentJob(lpQueue.pop());
				_abortJob = false;
				processCurrentJob();
				// TODO: Notify HP job
			}
			try { sleep(1); } catch (InterruptedException e) {}
		}
		
		_location.exiting("run()");
	}

	private void processCurrentJob()
	{
		_location.entering("processCurrentJob()", _currJob);
		System.out.println("Processing job: " + _currJob);
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
		if (!_abortJob) System.out.println("Finished job: " + _currJob);
		else System.out.println("Job aborted: " + _currJob);
		_location.exiting("processCurrentJob()");
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
			}
		}
		_location.exiting("processFIFOQueueNotification(FIFOQueueNotification)");
	}
}
