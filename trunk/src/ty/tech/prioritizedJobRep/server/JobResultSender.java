package ty.tech.prioritizedJobRep.server;

import ty.tech.prioritizedJobRep.api.ProxyFactory;
import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.FIFOQueue;
import ty.tech.prioritizedJobRep.common.FIFOQueueListener;
import ty.tech.prioritizedJobRep.common.FIFOQueueNotification;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.JobResult;
import ty.tech.prioritizedJobRep.common.FIFOQueueNotification.Type;
import ty.tech.prioritizedJobRep.dispatcher.Dispatcher;
import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;


public class JobResultSender extends Thread implements FIFOQueueListener
{
	private FIFOQueue _queue;
	private boolean _finished = false;
	private Location _location = Logger.getLocation(this.getClass());
	
	public JobResultSender()
	{
		setName("JobResultSender");
		setDaemon(true);
		_queue = new FIFOQueue("JobResultsQueue");
		_queue.addListener(this);
	}
	
	@Override
	public void run()
	{
		while (!_finished)
		{
			if (!_queue.isEmpty())
			{
				Job job = _queue.pop();
				sendJob(job);
			}
			else
			{
				try { sleep(100); } catch (InterruptedException e) { }
			}
		}
	}
	
	
	public void putJob(Job job)
	{
		_queue.put(job);
	}
	
	
	private void sendJob(Job job)
	{
		_location.entering("sendJob(job)", job);
		try
		{
			EndPoint ep = job.getDispatcher();
			_location.debug("Sending job '" + job + "' back to dispatcher at: " + ep);
			JobResult result = new JobResult(job, job.getPriority(), true);
			Dispatcher dispatcher = ProxyFactory.createDispatcherProxy(ep);
			dispatcher.keepJobResults(result);
		}
		catch (Throwable e)
		{
			_location.throwing("sendJob(job)", e);
		}
		_location.exiting("sendJob(job)");
	}
	

	public void stopSender()
	{
		_finished = true;
		interrupt();
	}

	
	@Override
	public void processFIFOQueueNotification(FIFOQueueNotification notification)
	{
		if (Type.NotEmpty.equals(notification.getType()))
		{
			interrupt();
		}
	}
}
