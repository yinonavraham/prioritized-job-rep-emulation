package ty.tech.prioritizedJobRep.server;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
	private List<Job> _jobsToAbort = new LinkedList<Job>();
	private Location _location = Logger.getLocation(this.getClass());
	
	public ServerImpl(int port) throws SocketException, UnknownHostException
	{
		_endPoint = new EndPoint(port);
		_policy = new ServerPolicy();
		initQueues();
		initStatistics();
		initExecutor();
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
		for (Priority p : Priority.values())
		{
			_queues.put(p, new FIFOQueue(p.name() + " Priority Q"));
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
		Priority p = job.getPriority();
		FIFOQueue queue = _queues.get(p);
		synchronized (queue)
		{
			queue.put(job);
		}
		synchronized (_stats)
		{
			_stats.jobEnqueued(p);
		}
		_location.exiting("putJob()");
	}

	@Override
	public synchronized void reset()
	{
		_location.entering("reset()");
		_executor.stopExecutor();
		initQueues();
		initStatistics();
		initExecutor();
		_executor.start();
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
		_finished = true;
		Thread.currentThread().interrupt();
		_location.exiting("stop()");
	}

	@Override
	public ServerStatistics getStatistics() throws RemoteException
	{
		return _stats;
	}

	@Override
	public void jobSiblingStarted(Job job) throws RemoteException
	{
		_location.entering("jobSiblingStarted(job)", job);
		Priority p = job.getPriority();
		// If the job that started is the HP sibling
		if (Priority.High.equals(p))
		{
			// Add the job to the list of sibling jobs that started
			synchronized (_jobsToAbort)
			{
				_jobsToAbort.add(job);
			}
			// If the job is in the queue
			FIFOQueue queue = _queues.get(p);
			synchronized (queue)
			{
			}
		}
		_location.exiting("jobSiblingStarted(job)");
	}

}
