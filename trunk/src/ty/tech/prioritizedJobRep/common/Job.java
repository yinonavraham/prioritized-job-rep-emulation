package ty.tech.prioritizedJobRep.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class Job implements Serializable
{
	private static final long serialVersionUID = -7178564194342696938L;

	private String _id;
	private Priority _priority = null;
	private List<EndPoint> _siblingsLocations = new LinkedList<EndPoint>();
	private JobResult _result = null;
	private JobStatistics _stats;
	private boolean _hasSiblingInProcess = false;
	private long _executionTime;
	
	public Job(String id, long executionTime)
	{
		if (id == null || id.isEmpty())
			throw new IllegalArgumentException("'" + id + "': Job ID cannot be null nor empty");
		if (executionTime <= 0)
			throw new IllegalArgumentException("'" + executionTime + "': Job execution time must be a positive number");
		_id = id;
		_executionTime = executionTime;
		_stats = new JobStatistics(id);
	}
	
	
	public Priority getPriority()
	{
		return _priority;
	}

	
	public void setPriority(Priority priority)
	{
		_priority = priority;
	}

	
	public List<EndPoint> getSiblingsLocations()
	{
		return _siblingsLocations;
	}

	
	public void addSiblingLocation(EndPoint siblingLocation)
	{
		_siblingsLocations.add(siblingLocation);
	}

	
	public void addSiblingsLocations(Collection<? extends EndPoint> siblingsLocations)
	{
		_siblingsLocations.addAll(siblingsLocations);
	}

	
	public Object getResult()
	{
		return _result;
	}

	
	public void setResult(JobResult result)
	{
		_result = result;
	}

	
	public boolean hasSiblingInProcess()
	{
		return _hasSiblingInProcess;
	}

	
	public void setHasSiblingInProcess(boolean hasSiblingInProcess)
	{
		_hasSiblingInProcess = hasSiblingInProcess;
	}

	
	public long getExecutionTime()
	{
		return _executionTime;
	}
	
	
	public void setExecutionTime(long executionTime)
	{
		_executionTime = executionTime;
	}


	public String getID()
	{
		return _id;
	}

	
	public JobStatistics getStatistics()
	{
		return _stats;
	}
	
	
	@Override
	public String toString()
	{
		return "Job_" + _id + "[" + _priority + "]";
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof Job)
		{
			Job job = (Job)obj;
			return _id.equals(job._id);
		}
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
		return _id.hashCode();
	}
	
	public Job clone(Priority priority) 
	{
		Job job = new Job(getID(),getExecutionTime());
		job.setPriority(priority);
		return job;
	}
}
