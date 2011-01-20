package ty.tech.prioritizedJobRep.server;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.FIFOQueue;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.Priority;
import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;


public class ServerImpl implements Server
{
	private EndPoint _endPoint;
	private boolean _finished = false;
	private Map<Priority,FIFOQueue> _queues = new HashMap<Priority, FIFOQueue>();
	private ServerPolicy _policy;
	private Executor _executor;
	private Location _location = Logger.getLocation(this.getClass());
	
	public ServerImpl(int port) throws SocketException, UnknownHostException
	{
		_endPoint = new EndPoint(port);
		_policy = new ServerPolicy();
		initQueues();
		_executor = new Executor(this);
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
	public void register(String host, int port)
	{
		_location.entering("register(host, port)",host,port);
		// TODO: register the server in the dispatcher
		System.err.println("register is not implemented yet");
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
		queue.put(job);
		_location.exiting("putJob()");
	}

	@Override
	public synchronized void reset()
	{
		_location.entering("reset()");
		System.err.println("reset is not implemented yet");
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

}
