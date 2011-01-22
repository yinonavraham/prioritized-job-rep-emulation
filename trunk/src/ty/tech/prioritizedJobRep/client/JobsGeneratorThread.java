package ty.tech.prioritizedJobRep.client;

import java.rmi.RemoteException;
import java.util.Properties;

import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.dispatcher.Dispatcher;

public class JobsGeneratorThread extends Thread
{
	private int _millisecsInMinute = 1000*60;
	private int _jobId = 0;
	private long _duration = 0;
	private int _jobLength = 0;
	private int _load = 0;
	private Properties _threadProps = null;
	private Dispatcher _dispatcher;
	
	public JobsGeneratorThread(int firstJobId, int duration, int jobLength, int load, Dispatcher dispatcher)
	{
		_jobId = firstJobId;
		_duration = duration * _millisecsInMinute; // in milliseconds
		_jobLength = jobLength;
		_load = load;
		_dispatcher = dispatcher;
		_threadProps = new Properties();
	}
	
    public void run() 
    {
		// calculate the gap to wait between jobs in milliseconds 
		double jobsGap = (double)_millisecsInMinute/((double)_load);
		long startTime = System.currentTimeMillis();
		long currentTime = startTime;

		do // while the duration has not passed yet
		{
			try 
			{
				Job job = new Job(Integer.toString(_jobId), _jobLength);
				_dispatcher.addJob(job);
				System.out.println("Sent Job " + _jobId);				
			} 
			catch (RemoteException e1) 
			{
				System.err.println("Error occured in JobsGeneratorThread addJob: " + e1.getMessage());
			}
			try 
			{
				Thread.sleep((long)jobsGap);
			} 
			catch (InterruptedException e) 
			{
				System.err.println("Error occured in JobsGeneratorThread sleep: " + e.getMessage());
			}
			
			_jobId++;
			
			// calculate if the iteration time has finished
			currentTime = System.currentTimeMillis();
		} while (_duration > (currentTime - startTime));
		
		_threadProps.put("lastJobId", _jobId);
    }
    
    public int getLastJobId()
    {
    	return (Integer)_threadProps.get("lastJobId");
    }
}
