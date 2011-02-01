package ty.tech.prioritizedJobRep.common;

import java.util.ArrayList;

public class ClientStatistics 
{
	private int _numHPjobs = 0;
	private int _numLPjobs = 0;
	private long _minJobTime = 0;
	private long _maxJobTime = 0;
	private long _avgJobTime = 0;
	private long _totJobsTime = 0;
	
	public ClientStatistics(ArrayList<JobResult> jobsResults)
	{
		_minJobTime = jobsResults.get(0).getTotalTimeInSys(); //Initialise to non-zero
		calc(jobsResults);
	}
	
	private void calc(ArrayList<JobResult> jobsResults)
	{
		for (int i = 0; i < jobsResults.size(); ++i)
		{
			JobResult res = jobsResults.get(i);
			
			// count HP
			if (0 == res.getPriority().compareTo(Priority.High))
				_numHPjobs++;
			
			// count LP
			if (0 == res.getPriority().compareTo(Priority.Low))
				_numLPjobs++;
			
			long totalTimeInSys = res.getTotalTimeInSys();
			
			// min total time in system
			if (totalTimeInSys < _minJobTime)
				_minJobTime = totalTimeInSys;
			
			// max total time in system
			if (totalTimeInSys > _maxJobTime)
				_maxJobTime = totalTimeInSys;
			
			// add job's time to total time
			_totJobsTime += totalTimeInSys; 
		}
		
		// calc avg job time
		_avgJobTime = _totJobsTime/jobsResults.size();
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

	public long getAvgJobTime() {
		return _avgJobTime;
	}
	
}
