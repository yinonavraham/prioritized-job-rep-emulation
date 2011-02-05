package ty.tech.prioritizedJobRep.common;

import java.io.Serializable;

public class JobResult implements Serializable
{
	private static final long serialVersionUID = -410386454415256326L;
	
	private Object _result;
	private Job _job;

	public JobResult(Job job, Object result) 
	{
		_result = result;
		_job = job;
	}

	public Object getResult() 
	{
		return _result;
	}

	public String getJobId() 
	{
		return _job.getID();
	}

	public JobStatistics getStatistics() 
	{
		return _job.getStatistics();
	}	
	
	public Priority getPriority() 
	{
		return _job.getPriority();
	}	
	
	public Job getJob()
	{
		return _job;
	}
}
