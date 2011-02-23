package ty.tech.prioritizedJobRep.server;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;


public class JobsToAbortDaemon extends Thread
{
	private Map<Job,Long> _jobsToAbort = null;
	private Location _location = Logger.getLocation(this.getClass());
	private boolean _finished = false;
	
	public JobsToAbortDaemon(Map<Job,Long> jobsMap)
	{
		_jobsToAbort = jobsMap;
		setDaemon(true);
		setName("JobsToAbortCleanerDaemon");
	}
	
	public void stopDaemon()
	{
		_finished = true;
		this.interrupt();
	}
	
	@Override
	public void run()
	{
		final long timeout = 180000; // 3 minutes = 3(m) * 60(s) * 1000(ms) = 180000ms
		while (!_finished)
		{
			try { Thread.sleep(10000); } catch (InterruptedException e) {}
			if (_finished) break;
			_location.debug("JobsToAbortCleaner: Started iteration");
			long currTime = new Date().getTime();
			synchronized (_jobsToAbort)
			{
				List<Job> jobs = new LinkedList<Job>();
				for (Job job : _jobsToAbort.keySet())
				{
					Long time = _jobsToAbort.get(job);
					if (time == null || currTime - time > timeout)
					{
						jobs.add(job);		
					}
				}
				for (Job job : jobs)
				{
					_location.debug("JobsToAbortCleaner: Remove job " + job);
					_jobsToAbort.remove(job);
				}
			}
			_location.debug("JobsToAbortCleaner: Finished iteration");
		}
	}
}
