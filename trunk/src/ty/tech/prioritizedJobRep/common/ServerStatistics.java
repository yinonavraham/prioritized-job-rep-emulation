package ty.tech.prioritizedJobRep.common;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ServerStatistics implements Serializable
{
	private static final long serialVersionUID = 8764441975335237871L;
	
	private EndPoint _serverID;
	private Priority _jobPriorityExecuted;
	private long _startTime;
	private long _endTime;
	// Last operation time
	private long _executionLastOpTime;
	private Map<Priority, Long> _queueLastOpTime = new HashMap<Priority, Long>();
	// Counters
	private Map<Priority, Long> _jobsTotalCount = new HashMap<Priority, Long>();
	private Map<Priority, Long> _jobsAbortedCount = new HashMap<Priority, Long>();
	private Map<Priority, Long> _jobsFinishedCount = new HashMap<Priority, Long>();
	private Map<Priority, Long> _jobsInQueueCount = new HashMap<Priority, Long>();
	private Map<Priority, Long> _maxQueueLength = new HashMap<Priority, Long>();
	// Time accumulators
	private long _idleTotalTime;
	private Map<Priority, Long> _executionTotalTime = new HashMap<Priority, Long>();
	private Map<Priority, Long> _queueTotalTime = new HashMap<Priority, Long>();
	// Averages
	private Map<Priority, Double> _queueAvgLength = new HashMap<Priority, Double>();

	
	public ServerStatistics(EndPoint serverEndPoint)
	{
		if (serverEndPoint == null)
			throw new IllegalArgumentException("Server end point cannot be null");
		_serverID = new EndPoint(serverEndPoint.getHostName(), serverEndPoint.getPort());
		init();
	}
	
	
	private void init()
	{
		_jobPriorityExecuted = null;
		initCounts();
		initTimes();
		initAverages();
	}
	
	
	private void initAverages()
	{
		for (Priority priority : Priority.values())
		{
			_queueAvgLength.put(priority, 0.0);
		}
	}


	private void initCounts()
	{
		_idleTotalTime = 0;
		for (Priority priority : Priority.values())
		{
			_jobsTotalCount.put(priority, 0L);
			_jobsAbortedCount.put(priority, 0L);
			_jobsFinishedCount.put(priority, 0L);
			_jobsInQueueCount.put(priority, 0L);
		}
	}


	private void initTimes()
	{
		long currTime = new Date().getTime(); 
		_startTime = currTime;
		_endTime = -1;
		_executionLastOpTime = currTime;
		for (Priority priority : Priority.values())
		{
			_queueLastOpTime.put(priority, currTime);
			_executionTotalTime.put(priority, 0L);
			_queueTotalTime.put(priority, 0L);
		}	
	}


	public EndPoint getServerID()
	{
		return _serverID;
	}
	
	
	public synchronized double getAvgQueueLength(Priority jobPriority)
	{
		return _queueAvgLength.get(jobPriority);
	}
	
	
	public synchronized double getAvgExecutionTime(Priority jobPriority)
	{
		long endTime = _endTime > 0 ? _endTime : new Date().getTime();
		return (double)_executionTotalTime.get(jobPriority) / (double)endTime;
	}
	
	
	public synchronized long getMaxQueueLength(Priority jobPriority)
	{
		long max = 0;
		if (jobPriority == null)
		{
			for (Priority priority : Priority.values())
			{
				max = max < _maxQueueLength.get(priority) ? _maxQueueLength.get(priority) : max;
			}
		}
		else max = _maxQueueLength.get(jobPriority);
		return max;
	}
	
	
	public synchronized long getJobsInQueueCount(Priority jobPriority)
	{
		long count = 0;
		if (jobPriority == null)
		{
			for (Priority priority : Priority.values())
			{
				count += _jobsInQueueCount.get(priority);
			}
		}
		else count = _jobsInQueueCount.get(jobPriority);
		return count;
	}
	
	
	public synchronized long getJobsAbortedCount(Priority jobPriority)
	{
		long count = 0;
		if (jobPriority == null)
		{
			for (Priority priority : Priority.values())
			{
				count += _jobsAbortedCount.get(priority);
			}
		}
		else count = _jobsAbortedCount.get(jobPriority);
		return count;
	}
	
	
	public synchronized long getJobsFinishedCount(Priority jobPriority)
	{
		long count = 0;
		if (jobPriority == null)
		{
			for (Priority priority : Priority.values())
			{
				count += _jobsFinishedCount.get(priority);
			}
		}
		else count = _jobsFinishedCount.get(jobPriority);
		return count;
	}
	
	
	public synchronized long getJobsTotalCount(Priority jobPriority)
	{
		long count = 0;
		if (jobPriority == null)
		{
			for (Priority priority : Priority.values())
			{
				count += _jobsTotalCount.get(priority);
			}
		}
		else count = _jobsTotalCount.get(jobPriority);
		return count;
	}
	
	
	public synchronized long getTotalIdleTime()
	{
		return _idleTotalTime;
	}
	
	
	public synchronized double getAvgIdleTime()
	{
		long endTime = _endTime > 0 ? _endTime : new Date().getTime();
		return (double)_idleTotalTime / (double)endTime;
	}
	
	
	private void updateAvgQueueLength(Priority jobPriority, long currLength)
	{
		long currTime = new Date().getTime();
		long lastOpTime = _queueLastOpTime.get(jobPriority);
		long timespan = currTime - lastOpTime;
		double avgLength = _queueAvgLength.get(jobPriority);
		avgLength = (avgLength * (lastOpTime - _startTime) + timespan * currLength) / (currTime - _startTime);
		_queueAvgLength.put(jobPriority,avgLength);
		_queueLastOpTime.put(jobPriority,currTime);
	}
	
	
	public synchronized void jobEnqueued(Priority jobPriority)
	{
		// Update the average queue length
		long currLength = _jobsInQueueCount.get(jobPriority);
		updateAvgQueueLength(jobPriority, currLength);
		// Update the current queue length
		currLength++;
		_jobsInQueueCount.put(jobPriority,currLength);
		// Update the max queue length
		long max = _maxQueueLength.get(jobPriority);
		_maxQueueLength.put(jobPriority, currLength > max ? currLength : max);
	}
	
	
	public synchronized void jobDequeued(Priority jobPriority)
	{
		// Update the average queue length
		long currLength = _jobsInQueueCount.get(jobPriority);
		updateAvgQueueLength(jobPriority, currLength);
		// Update the current queue length
		currLength--;
		_jobsInQueueCount.put(jobPriority,currLength);
	}
	
	
	public synchronized void jobExecutionStarted(Priority jobPriority)
	{
		// Update the idle total time
		long currTime = new Date().getTime();
		_idleTotalTime += currTime - _executionLastOpTime;
		// Update the execution last operation time
		_executionLastOpTime = currTime;
		_jobPriorityExecuted = jobPriority;
	}
	
	
	public synchronized void jobExecutionFinished(Priority jobPriority)
	{
		// Update the execution total time
		long currTime = new Date().getTime();
		long timespan = currTime - _executionLastOpTime;
		_executionTotalTime.put(jobPriority, _executionTotalTime.get(jobPriority) + timespan);
		// Update the execution last operation time
		_executionLastOpTime = currTime;
		// Update the finished jobs count
		_jobsFinishedCount.put(jobPriority, _jobsFinishedCount.get(jobPriority) + 1);
		_jobPriorityExecuted = null;
	}
	
	
	public synchronized void jobExecutionAborted(Priority jobPriority)
	{
		// Update the execution total time
		long currTime = new Date().getTime();
		long timespan = currTime - _executionLastOpTime;
		_executionTotalTime.put(jobPriority, _executionTotalTime.get(jobPriority) + timespan);
		// Update the execution last operation time
		_executionLastOpTime = currTime;
		// Update the aborted jobs count
		_jobsAbortedCount.put(jobPriority, _jobsAbortedCount.get(jobPriority) + 1);
		_jobPriorityExecuted = null;
	}
	
	
	public synchronized void close()
	{
		// Close idle total time if currently idle
		long currTime = new Date().getTime();
		if (_jobPriorityExecuted == null) 
		{
			_idleTotalTime += currTime - _executionLastOpTime;
		}
		else // there is a job being executed
		{
			long timespan = currTime - _executionLastOpTime;
			long totalTime = _executionTotalTime.get(_jobPriorityExecuted) + timespan;
			_executionTotalTime.put(_jobPriorityExecuted, totalTime);
		}
	}
	
	
	public synchronized void reset()
	{
		init();
	}
}
