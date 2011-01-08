package ty.tech.prioritizedJobRep.common;

import java.util.LinkedList;
import java.util.List;


public class FIFOQueue
{
	private List<Job> _elements = new LinkedList<Job>();
	
	public FIFOQueue()
	{
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
	
	
	public synchronized int size()
	{
		return _elements.size();
	}
	
	
	public synchronized boolean put(Job element)
	{
		return _elements.add(element);
	}
	
	
	public synchronized void putFirst(Job element)
	{
		_elements.add(0, element);
	}
	
	
	public synchronized boolean remove(Job element)
	{
		return _elements.remove(element);
	}
	
	
	public synchronized void clear()
	{
		_elements.clear();
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
}
