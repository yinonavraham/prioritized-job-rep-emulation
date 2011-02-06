package ty.tech.prioritizedJobRep.server;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ty.tech.prioritizedJobRep.api.ProxyFactory;
import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.FIFOQueue;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.Priority;
import ty.tech.prioritizedJobRep.common.ServerStatistics;
import ty.tech.prioritizedJobRep.dispatcher.Dispatcher;
import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;


public class ServerImpl implements Server
{
	private EndPoint _endPoint;
	private boolean _finished = false;
	private Map<Priority,FIFOQueue> _queues = new HashMap<Priority, FIFOQueue>();
	private ServerPolicy _policy;
	private Executor _executor;
	private ServerStatistics _stats;
	private Map<Job,Long> _jobsToAbort = new HashMap<Job,Long>();
	private JobResultSender _resultSender;
	private Location _location = Logger.getLocation(this.getClass());
	
	
	public ServerImpl(int port) throws SocketException, UnknownHostException
	{
		_endPoint = new EndPoint(port);
		_policy = new ServerPolicy();
		initStatistics();
		initQueues();
		initExecutor();
		initJobResultSender();
		startJobsToAbortCleanerDaemon();
	}
	

	private void startJobsToAbortCleanerDaemon()
	{
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				final long timeout = 180000; // 3 minutes = 3(m) * 60(s) * 1000(ms) = 180000ms
				while (true)
				{
					try { Thread.sleep(10000); } catch (InterruptedException e) {}
					_location.debug("JobsToAbortCleaner: Started iteration");
					long currTime = new Date().getTime();
					synchronized (_jobsToAbort)
					{
						List<Job> jobs = new LinkedList<Job>();
						for (Job job : _jobsToAbort.keySet())
						{
							Long time = _jobsToAbort.get(job);
							if (time == null || currTime - time > timeout)
							{
								jobs.add(job);		
							}
						}
						for (Job job : jobs)
						{
							_location.debug("JobsToAbortCleaner: Remove job " + job);
							_jobsToAbort.remove(job);
						}
					}
					_location.debug("JobsToAbortCleaner: Finished iteration");
				}
			}
		});
		t.setDaemon(true);
		t.setName("JobsToAbortCleanerDaemon");
		t.start();
	}
	
	
	private void initJobResultSender()
	{
		_resultSender = new JobResultSender();
	}
	

	private void initExecutor()
	{
		_executor = new Executor(this);
	}

	
	private void initStatistics()
	{
		_stats = new ServerStatistics(_endPoint);
	}

	
	private void initQueues()
	{
		_queues.clear();
		for (Priority p : Priority.values())
		{
			FIFOQueue queue = new FIFOQueue(p.name() + " Priority Q"); 
			_queues.put(p, queue);
			_stats.addQueueStatistics(p, queue.getStatistics());
		}	
	}
	
	
	protected FIFOQueue getQueue(Priority priority)
	{
		return _queues.get(priority);
	}
	
	
	private synchronized boolean isFinished()
	{
		return _finished;
	}
	
	
	@Override
	public EndPoint getEndPoint()
	{
		return _endPoint;
	}
	
	
	public void start()
	{
		_location.entering("start()");
		_executor.start();
		_resultSender.start();
		while (!isFinished())
		{
			try { Thread.sleep(2000); }
			catch (InterruptedException e) { }
		}
		_location.exiting("start()");
	}
	
	
	@Override
	public void register(String host, int port) throws RemoteException
	{
		_location.entering("register(host, port)",host,port);
		try
		{
			Dispatcher dispatcher = ProxyFactory.createDispatcherProxy(host, port);
			dispatcher.registerServer(_endPoint);
		}
		catch (AccessException e)
		{
			_location.throwing("register(host, port)", e);
			throw new RemoteException("Failed to register to dispatcher. Reason: " + e.getMessage(), e);
		}
		catch (RemoteException e)
		{
			_location.throwing("register(host, port)", e);
			throw e;
		}
		catch (NotBoundException e)
		{
			_location.throwing("register(host, port)", e);
			throw new RemoteException("Failed to register to dispatcher. Reason: " + e.getMessage(), e);
		}
		_location.exiting("register()");
	}

	
	@Override
	public void abortJob(Job job)
	{
		_location.entering("abortJob(job)",job);
		System.out.println("Aborting job: " + job);
		Priority p = job.getPriority();
		FIFOQueue queue = _queues.get(p);
		synchronized (queue)
		{
			if (queue.contains(job))
			{
				queue.remove(job);
			}
			else if (_executor.isCurrentJob(job))
			{
				_executor.abortJob(job);
			}
		}
		System.out.println("Job aborted: " + job);
		_location.exiting("abortJob()");
	}
	

	@Override
	public void putJob(Job job)
	{
		_location.entering("putJob(job)",job);
		synchronized (_jobsToAbort)
		{
			if (_jobsToAbort.containsKey(job))
			{
				_jobsToAbort.remove(job);
				_location.debug("Received job was tagged to be aborted: " + job);
				_location.exiting("putJob()");
				return;
			}
		}
		Priority p = job.getPriority();
		FIFOQueue queue = _queues.get(p);
		queue.put(job);
		_location.debug("Received job was added to queue: " + job);
		_location.exiting("putJob()");
	}
	
	
	protected void reenterJob(Job job)
	{
		_location.entering("reenterJob(job)", job);
		Priority p = job.getPriority();
		FIFOQueue queue = _queues.get(p);
		switch (getPolicy().getLPJobReEnter())
		{
			case First:
				queue.putFirst(job);
				System.out.println("Reenter job to be first: " + job);
				_location.debug("Reenter job to be first: " + job);
				break;
			case Last:
				queue.putLast(job);
				System.out.println("Reenter job to be last: " + job);
				_location.debug("Reenter job to be last: " + job);
				break;
			case No:
				System.out.println("Job will not be reentered: " + job);
				_location.debug("Job will not be reentered: " + job);
				break;
		}
		_location.exiting("reenterJob(job)");
	}

	
	@Override
	public synchronized void reset()
	{
		_location.entering("reset()");
		System.out.print("Resetting server... ");
		_executor.stopExecutor();
		_resultSender.stopSender();
		initStatistics();
		initQueues();
		initExecutor();
		initJobResultSender();
		_executor.start();
		_resultSender.start();
		System.out.println("Done.");
		_location.exiting("reset()");
	}

	
	@Override
	public synchronized void setPolicy(ServerPolicy policy)
	{
		_policy = policy;
	}
	
	
	protected synchronized ServerPolicy getPolicy()
	{
		return _policy;
	}
	
	
	@Override
	public boolean ping()
	{
		return true;
	}
	

	@Override
	public synchronized void stop()
	{
		_location.entering("stop()");
		_executor.stopExecutor();
		_resultSender.stopSender();
		_finished = true;
		Thread.currentThread().interrupt();
		_location.exiting("stop()");
	}

	
	@Override
	public ServerStatistics getStatistics() throws RemoteException
	{
		_stats.close();
		return _stats;
	}
	
	
	protected ServerStatistics getStatisticsInternal()
	{
		return _stats;
	} 

	
	@Override
	public void processSiblingNotification(JobNotification notification) throws RemoteException
	{
		_location.entering("processSiblingNotification(notification)", notification);
		switch (notification.getType())
		{
			case Started:
				jobSiblingStarted(notification.getJob());
				break;
			case Finished:
				jobSiblingFinished(notification.getJob());
				break;
			case Aborted:
				break;
		}
		_location.exiting("processSiblingNotification(notification)");
	}
	
	
	private void jobSiblingStarted(Job job) throws RemoteException
	{
		_location.entering("jobSiblingStarted(job)", job);
		Priority p = job.getPriority();
		// If the job that started is the HP sibling
		if (Priority.High.equals(p))
		{
			// Add the job to the list of sibling jobs that started
			synchronized (_jobsToAbort)
			{
				_jobsToAbort.put(job,new Date().getTime());
			}
			// Abort the job's execution if it is the current job
			if (_executor.abortJob(job))
			{
				System.out.println("Job was aborted due to a sibling that started: " + job);
				synchronized (_jobsToAbort)
				{
					_jobsToAbort.remove(job);
				}
			}
		}
		_location.exiting("jobSiblingStarted(job)");
	}

	
	private void jobSiblingFinished(Job job) throws RemoteException
	{
		_location.entering("jobSiblingFinished(job)", job);
//		Priority p = job.getPriority();
		// Add the job to the list of sibling jobs that need to be aborted
		synchronized (_jobsToAbort)
		{
			_jobsToAbort.put(job,new Date().getTime());
		}
		// Abort the job's execution if it is the current job
		if (_executor.abortJob(job))
		{
			System.out.println("Job was aborted due to a sibling that finished: " + job);
			synchronized (_jobsToAbort)
			{
				_jobsToAbort.remove(job);
			}
		}
		_location.exiting("jobSiblingFinished(job)");
	}

	
	public boolean isJobMarkedToAbort(Job job)
	{
		synchronized (_jobsToAbort)
		{
			return _jobsToAbort.containsKey(job);
		}
	}
	
	
	protected void sendJobBack(Job job)
	{
		_resultSender.putJob(job);
	}

}
