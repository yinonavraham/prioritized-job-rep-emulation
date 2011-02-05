package ty.tech.prioritizedJobRep.common;

import java.util.ArrayList;

public class ClientStatistics 
{
	private int _numHPjobs = 0;
	private int _numLPjobs = 0;
	private long _minJobTime = 0;
	private long _maxJobTime = 0;
	private double _avgJobTime = 0;
	private long _totJobsTime = 0;
	private long _maxJobQueueTime = 0;
	private double _avgJobQueueTime = 0;
	private long _minJobExecTime = Long.MAX_VALUE;
	private long _maxJobExecTime = 0;
	private double _avgJobExecTime = 0;
	
	public ClientStatistics(ArrayList<JobResult> jobsResults)
	{
		_minJobTime = jobsResults.get(0).getStatistics().getTotalTime(); //Initialise to non-zero
		calc(jobsResults);
	}
	
	private void calc(ArrayList<JobResult> jobsResults)
	{
		long totalJobQueueTime = 0;
		long totalJobExecTime = 0;
		for (int i = 0; i < jobsResults.size(); ++i)
		{
			JobResult res = jobsResults.get(i);
			
			// count HP
			if (0 == res.getPriority().compareTo(Priority.High))
				_numHPjobs++;
			
			// count LP
			if (0 == res.getPriority().compareTo(Priority.Low))
				_numLPjobs++;
			
			long totalTimeInSys = res.getStatistics().getTotalTime();
			
			// min total time in system
			if (totalTimeInSys < _minJobTime)
				_minJobTime = totalTimeInSys;
			
			// max total time in system
			if (totalTimeInSys > _maxJobTime)
				_maxJobTime = totalTimeInSys;
			
			// add job's time to total time
			_totJobsTime += totalTimeInSys; 
			
			// Max job time in a queue
			long qTime = res.getStatistics().getQueueTime();
			_maxJobQueueTime = qTime > _maxJobQueueTime ? qTime : _maxJobQueueTime;
			totalJobQueueTime += qTime;
			
			// Min & Max job execution time
			long execTime = res.getStatistics().getExecutionTime();
			_minJobExecTime = execTime < _minJobExecTime ? execTime : _minJobExecTime;
			_maxJobExecTime = execTime > _maxJobExecTime ? execTime : _maxJobExecTime;
			totalJobExecTime += execTime;
			
		}
		
		// calc avg job time
		_avgJobTime = (double)_totJobsTime/(double)jobsResults.size();
		// Calc avg job queue time
		_avgJobQueueTime = (double)totalJobQueueTime / (double)jobsResults.size();
		// Calc avg job execution time
		_avgJobExecTime = (double)totalJobExecTime / (double)jobsResults.size();
	}

	public int getNumHPjobs() {
		return _numHPjobs;
	}

	public int getNumLPjobs() {
		return _numLPjobs;
	}

	public long getMinJobTime() {
		return _minJobTime;
	}

	public long getMaxJobTime() {
		return _maxJobTime;
	}

	public double getAvgJobTime() {
		return _avgJobTime;
	}
	
	public long getMaxJobQueueTime()
	{
		return _maxJobQueueTime;
	}
	
	public double getAvgJobQueueTime()
	{
		return _avgJobQueueTime;
	}
	
	public long getMinJobExecTime()
	{
		return _minJobExecTime;
	}
	
	public long getMaxJobExecTime()
	{
		return _maxJobExecTime;
	}
	
	public double getAvgJobExecTime()
	{
		return _avgJobExecTime;
	}
	
}
