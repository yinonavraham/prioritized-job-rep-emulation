package ty.tech.prioritizedJobRep.common;

import java.io.Serializable;

public class JobResult implements Serializable
{
	private static final long serialVersionUID = -410386454415256326L;
	
	private Object _result;
	private String _jobId;
	private long _startTime;
	private long _totalTimeinSys;

	public JobResult(Job job, Object result) 
	{
		_result = result;
		_jobId = job.getID();
		_startTime = job.getStatistics().getStartTime();
		_totalTimeinSys = 0;
	}

	public Object getResult() 
	{
		return _result;
	}

	public String getJobId() 
	{
		return _jobId;
	}

	public long getStartTime() 
	{
		return _startTime;
	}
	
	public void setTotalTimeinSys(long totalTime)
	{
		_totalTimeinSys = totalTime;
	}
	
	public long getTotalTimeInSys() 
	{
		return _totalTimeinSys;
	}	
	
	
}
