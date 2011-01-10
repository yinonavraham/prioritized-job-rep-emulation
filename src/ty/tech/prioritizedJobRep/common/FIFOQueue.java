package ty.tech.prioritizedJobRep.common;

import java.util.LinkedList;
import java.util.List;

import ty.tech.prioritizedJobRep.common.FIFOQueueNotification.Type;


public class FIFOQueue
{
	private String _name;
	private List<Job> _elements = new LinkedList<Job>();
	private List<FIFOQueueListener> _listeners = new LinkedList<FIFOQueueListener>();
	
	public FIFOQueue(String name)
	{
		_name = name;
	}
	
	public synchronized Job pop()
	{
		if (_elements.size() > 0)
		{
			for (int i = 0; i < _elements.size(); i++)
				if (!_elements.get(i).hasSiblingInProcess()) return _elements.remove(i);
		}
		return null;
	}
	
	public synchronized Job peek()
	{
		if (_elements.size() > 0)
		{
			for (int i = 0; i < _elements.size(); i++)
				if (!_elements.get(i).hasSiblingInProcess()) return _elements.get(i);
		}
		return null;
	}
	
	
	public synchronized int size()
	{
		return _elements.size();
	}
	
	public synchronized boolean isEmpty()
	{
		return _elements.size() == 0;
	}
	
	
	public synchronized boolean put(Job element)
	{
		boolean res = _elements.add(element);
		if (_elements.size() == 1) 
			publishNotification(new FIFOQueueNotification(this,
				Type.NotEmpty, _name + ": a new job was added to the empty queue"));
		return res;
	}
	
	
	public synchronized void putFirst(Job element)
	{
		_elements.add(0, element);
		if (_elements.size() == 1) 
			publishNotification(new FIFOQueueNotification(this,
				Type.NotEmpty, _name + ": a new job was added to the empty queue"));
	}
	
	
	public synchronized boolean remove(Job element)
	{
		return _elements.remove(element);
	}
	
	
	public synchronized void clear()
	{
		_elements.clear();
	}
	
	
	public synchronized boolean contains(Job job)
	{
		return _elements.contains(job);
	}
	
	
	public synchronized void markAsWait(Job job)
	{
		if (_elements.contains(job))
		{
			_elements.get(_elements.indexOf(job)).setHasSiblingInProcess(true);
		}
	}
	
	
	public synchronized void markAsActive(Job job)
	{
		if (_elements.contains(job))
		{
			_elements.get(_elements.indexOf(job)).setHasSiblingInProcess(false);
		}
	}
	
	
	public void addListener(FIFOQueueListener l)
	{
		_listeners.add(l);
	}
	
	
	private void publishNotification(final FIFOQueueNotification notification)
	{
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for (FIFOQueueListener l : _listeners)
				{
					l.processFIFOQueueNotification(notification);
				}			
			}
		});
		t.setDaemon(true);
		t.setName("FIFOQueueNotification dispatcher");
		t.start();
	}
}
