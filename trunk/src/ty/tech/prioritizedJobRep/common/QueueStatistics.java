package ty.tech.prioritizedJobRep.common;

import java.io.Serializable;
import java.util.Date;


public class QueueStatistics implements Serializable
{
	private static final long serialVersionUID = -4136548428737884507L;
	
	private long _lastOpTime;
	private long _startTime;
	private double _avgLength;
	private long _maxLength;
	private long _currLength;
	private long _totalJobs;
	
	
	public QueueStatistics()
	{
		_avgLength = 0.0;
		long currTime = new Date().getTime();
		_startTime = currTime;
		_lastOpTime = currTime;
		_maxLength = 0;
		_currLength = 0;
		_totalJobs = 0;
	}
	

	public double getAvgLength()
	{
		return _avgLength;
	}


	public long getMaxLength()
	{
		return _maxLength;
	}
	
	
	public long getCurrentLength()
	{
		return _currLength;
	}
	
	
	public long getTotalJobsCount()
	{
		return _totalJobs;
	}
	
	
	public void update(long currLength, boolean newJob)
	{
		if (newJob) _totalJobs++;
		_currLength = currLength;
		_maxLength = _maxLength > currLength ? _maxLength : currLength;
		long currTime = new Date().getTime();
		long timespan = currTime - _lastOpTime;
		_avgLength = (_avgLength * (_lastOpTime - _startTime) + timespan * currLength) / (currTime - _startTime);
		_lastOpTime = currTime;
	}
}
