package ty.tech.prioritizedJobRep.common;

public class JobResult 
{
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
