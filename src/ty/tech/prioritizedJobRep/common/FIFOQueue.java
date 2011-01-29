package ty.tech.prioritizedJobRep.common;

import java.util.LinkedList;
import java.util.List;

import ty.tech.prioritizedJobRep.common.FIFOQueueNotification.Type;


public class FIFOQueue
{
	private String _name;
	private long _maxSize = -1;
	private List<Job> _elements = new LinkedList<Job>();
	private List<FIFOQueueListener> _listeners = new LinkedList<FIFOQueueListener>();
	private QueueStatistics _stats = new QueueStatistics();
	
	public FIFOQueue(String name)
	{
		_name = name;
	}
	
	public synchronized Job pop()
	{
		if (_elements.size() > 0)
		{
			for (int i = 0; i < _elements.size(); i++)
				if (!_elements.get(i).hasSiblingInProcess()) 
				{
					_stats.update(size()-1, false);
					return _elements.remove(i);
				}
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
		if (_maxSize < 0 || _elements.size() < _maxSize)
		{
			boolean res = _elements.add(element);
			if (res == true) _stats.update(size(), true);
			if (_elements.size() == 1) 
				publishNotification(new FIFOQueueNotification(this,
					Type.NotEmpty, _name + ": a new job was added to the empty queue"));
			return res;
		}
		else
		{
			return false;
		}
	}
	
	
	public synchronized void putLast(Job element)
	{
		if (_maxSize >= 0 && _elements.size() >= _maxSize)
		{
			_elements.set(_elements.size()-1, element);
		}
		else
		{
			_elements.add(element);
		}
		_stats.update(size(), false);
	}
	
	
	public synchronized void putFirst(Job element)
	{
		if (_maxSize >= 0 && _elements.size() >= _maxSize)
		{
			_elements.remove(_elements.size()-1);
		}
		_elements.add(0, element);
		_stats.update(size(), false);
		if (_elements.size() == 1) 
			publishNotification(new FIFOQueueNotification(this,
				Type.NotEmpty, _name + ": a new job was added to the empty queue"));
	}
	
	
	public synchronized boolean remove(Job element)
	{
		boolean res = _elements.remove(element);
		if (res == true) _stats.update(size(), false);
		return res;
	}
	
	
	public synchronized void clear()
	{
		_elements.clear();
		_stats.update(size(), false);
	}
	
	
	public synchronized boolean contains(Job job)
	{
		return _elements.contains(job);
	}
	
	
	public void setMaxSize(long size)
	{
		_maxSize = size;
	}
	
	
	public long getMaxSize()
	{
		return _maxSize;
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
	
	
	public QueueStatistics getStatistics()
	{
		return _stats;
	}
}
