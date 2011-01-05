package ty.tech.prioritizedJobRep.common;

import java.io.Serializable;
import java.util.Date;


public class JobStatistics implements Serializable
{
	private static final long serialVersionUID = -907888961315923148L;

	private String _jobID;
	private long _startTotalTime;
	private long _endTotalTime;
	private long _enqueueTime;
	private long _dequeueTime;
	private long _executionStartTime;
	private long _executionEndTime;
	
	
	public JobStatistics(String jobID)
	{
		_jobID = jobID;
	}

	
	public long getEndTime()
	{
		return _endTotalTime;
	}

	
	public void setEndTime(long endTime)
	{
		_endTotalTime = endTime;
	}

	
	public void setEndTime(Date endTime)
	{
		setEndTime(endTime.getTime());
	}

	
	public void setEndTime()
	{
		setEndTime(new Date());
	}

	
	public long getStartTime()
	{
		return _startTotalTime;
	}

	
	public void setStartTime(long startTime)
	{
		_startTotalTime = startTime;
	}

	
	public void setStartTime(Date startTime)
	{
		setStartTime(startTime.getTime());
	}

	
	public void setStartTime()
	{
		setStartTime(new Date());
	}

	
	public long getDequeueTime()
	{
		return _dequeueTime;
	}

	
	public void setDequeueTime(long dequeueTime)
	{
		_dequeueTime = dequeueTime;
	}

	
	public void setDequeueTime(Date dequeueTime)
	{
		setDequeueTime(dequeueTime.getTime());
	}

	
	public void setDequeueTime()
	{
		setDequeueTime(new Date());
	}

	
	public long getEnqueueTime()
	{
		return _enqueueTime;
	}

	
	public void setEnqueueTime(long enqueueTime)
	{
		_enqueueTime = enqueueTime;
	}

	
	public void setEnqueueTime(Date enqueueTime)
	{
		setEnqueueTime(enqueueTime.getTime());
	}

	
	public void setEnqueueTime()
	{
		setEnqueueTime(new Date());
	}

	
	public long getExecutionEndTime()
	{
		return _executionEndTime;
	}

	
	public void setExecutionEndTime(long executionEndTime)
	{
		_executionEndTime = executionEndTime;
	}

	
	public void setExecutionEndTime(Date executionEndTime)
	{
		setExecutionEndTime(executionEndTime.getTime());
	}

	
	public void setExecutionEndTime()
	{
		setExecutionEndTime(new Date());
	}

	
	public long getExecutionStartTime()
	{
		return _executionStartTime;
	}

	
	public void setExecutionStartTime(long executionStartTime)
	{
		_executionStartTime = executionStartTime;
	}

	
	public void setExecutionStartTime(Date executionStartTime)
	{
		setExecutionStartTime(executionStartTime.getTime());
	}

	
	public void setExecutionStartTime()
	{
		setExecutionStartTime(new Date());
	}

	
	public String getJobID()
	{
		return _jobID;
	}

	
	public long getTotalTime()
	{
		return _endTotalTime - _startTotalTime;
	}

	
	public long getQueueTime()
	{
		return _dequeueTime - _enqueueTime;
	}

	
	public long getExecutionTime()
	{
		return _executionEndTime - _executionStartTime;
	}
	
	
	@Override
	public String toString()
	{
		return "JobStats_" + _jobID;
	}
}
