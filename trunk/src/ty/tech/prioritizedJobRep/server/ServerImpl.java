package ty.tech.prioritizedJobRep.server;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.FIFOQueue;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.Priority;


public class ServerImpl implements Server
{
	private EndPoint _endPoint;
	private boolean _finished = false;
	private Map<Priority,FIFOQueue> _queues = new HashMap<Priority, FIFOQueue>();
	
	public ServerImpl(int port) throws SocketException, UnknownHostException
	{
		_endPoint = new EndPoint(port);
		initQueues();
	}
	
	private void initQueues()
	{
		for (Priority p : Priority.values())
		{
			_queues.put(p, new FIFOQueue());
		}	
	}
	
	private synchronized boolean isFinished()
	{
		return _finished;
	}
	
	public void start()
	{
		while (!isFinished())
		{
			try { Thread.sleep(2000); }
			catch (InterruptedException e) { }
		}
	}
	
	@Override
	public void register(String host, int port)
	{
		// TODO: register the server in the dispatcher
		System.err.println("register is not implemented yet");
	}

	@Override
	public void abortJob(Job job)
	{
		System.err.println("abortJob is not implemented yet");
	}

	@Override
	public void putJob(Job job)
	{
		Priority p = job.getPriority();
		FIFOQueue queue = _queues.get(p);
		synchronized (queue)
		{
			queue.put(job);
		}
	}

	@Override
	public synchronized void reset()
	{
		System.err.println("reset is not implemented yet");
	}

	@Override
	public synchronized void setPolicy(ServerPolicy policy)
	{
		System.err.println("setPolicy is not implemented yet");
	}

	@Override
	public synchronized void stop()
	{
		_finished = true;
		Thread.currentThread().interrupt();
	}

}
