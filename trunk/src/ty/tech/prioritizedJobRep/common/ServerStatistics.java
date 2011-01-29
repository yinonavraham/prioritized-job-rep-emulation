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
	// Counters
	private Map<Priority, Long> _jobsAbortedCount = new HashMap<Priority, Long>();
	private Map<Priority, Long> _jobsFinishedCount = new HashMap<Priority, Long>();
	private Map<Priority, QueueStatistics> _queueStatistics = new HashMap<Priority, QueueStatistics>();
	// Time accumulators
	private long _idleTotalTime;
	private Map<Priority, Long> _executionTotalTime = new HashMap<Priority, Long>();
	private Map<Priority, Long> _queueTotalTime = new HashMap<Priority, Long>();

	
	public ServerStatistics(EndPoint serverEndPoint)
	{
		if (serverEndPoint == null)
			throw new IllegalArgumentException("Server end point cannot be null");
		_serverID = new EndPoint(serverEndPoint.getInetAddress(), serverEndPoint.getPort());
		init();
	}
	
	
	private void init()
	{
		_jobPriorityExecuted = null;
		initCounts();
		initTimes();
		initQueueStats();
	}


	private void initQueueStats()
	{
		_queueStatistics.clear();
	}


	public void addQueueStatistics(Priority priority, QueueStatistics stats)
	{
		_queueStatistics.put(priority, stats);
	}


	private void initCounts()
	{
		_idleTotalTime = 0;
		for (Priority priority : Priority.values())
		{
			_jobsAbortedCount.put(priority, 0L);
			_jobsFinishedCount.put(priority, 0L);
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
		return _queueStatistics.get(jobPriority).getAvgLength();
	}
	
	
	public synchronized double getAvgExecutionTime(Priority jobPriority)
	{
		long endTime = _endTime > 0 ? _endTime : new Date().getTime();
		return (double)_executionTotalTime.get(jobPriority) / (double)(endTime - _startTime);
	}
	
	
	public synchronized long getMaxQueueLength(Priority jobPriority)
	{
		long max = 0;
		if (jobPriority == null)
		{
			for (Priority priority : Priority.values())
			{
				long qMax = _queueStatistics.get(priority).getMaxLength();
				max = max < qMax ? qMax : max;
			}
		}
		else max = _queueStatistics.get(jobPriority).getMaxLength();
		return max;
	}
	
	
	public synchronized long getJobsInQueueCount(Priority jobPriority)
	{
		long count = 0;
		if (jobPriority == null)
		{
			for (Priority priority : Priority.values())
			{
				count += _queueStatistics.get(priority).getCurrentLength();
			}
		}
		else count = _queueStatistics.get(jobPriority).getCurrentLength();
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
				count += _queueStatistics.get(priority).getTotalJobsCount();
			}
		}
		else count = _queueStatistics.get(jobPriority).getTotalJobsCount();
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
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Server: " + getServerID() + "\n");
		for (Priority p : Priority.values())
		{
			sb.append("Priority: " + p + "\n");
			sb.append("\tAverage Execution Time: " + getAvgExecutionTime(p) + "\n");
			sb.append("\tAverage Queue Length:   " + getAvgQueueLength(p) + "\n");
			sb.append("\tJobs Aborted Count:     " + getJobsAbortedCount(p) + "\n");
			sb.append("\tJobs Finished Count:    " + getJobsFinishedCount(p) + "\n");
			sb.append("\tJobs In Queue Count:    " + getJobsInQueueCount(p) + "\n");
			sb.append("\tJobs Total Count:       " + getJobsTotalCount(p) + "\n");
			sb.append("\tMax Queue Length:       " + getMaxQueueLength(p) + "\n");
		}
		sb.append("Total for server: \n");
		sb.append("\tJobs Aborted Count:     " + getJobsAbortedCount(null) + "\n");
		sb.append("\tJobs Finished Count:    " + getJobsFinishedCount(null) + "\n");
		sb.append("\tJobs In Queue Count:    " + getJobsInQueueCount(null) + "\n");
		sb.append("\tJobs Total Count:       " + getJobsTotalCount(null) + "\n");
		sb.append("\tMax Queue Length:       " + getMaxQueueLength(null) + "\n");
		sb.append("\tAverage Idle Time: " + getAvgIdleTime() + "\n");
		sb.append("\tTotal Idle Time:   " + getTotalIdleTime() + "\n");
		return sb.toString();
	}
}
