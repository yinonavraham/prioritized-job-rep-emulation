package ty.tech.prioritizedJobRep.common;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ty.tech.prioritizedJobRep.logging.Location;
import ty.tech.prioritizedJobRep.logging.Logger;

public class StatisticsHandler 
{
	private FileWriter _writer = null;
	private String _newLine = "\n";
	private String _seperator = ",";
	private Location _location = Logger.getLocation(this.getClass());
	
	public StatisticsHandler() throws IOException 
	{
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("ddMMyyyy");
        DateFormat tf = new SimpleDateFormat("hhmmss");
		String timeStamp = df.format(date) + "_" + tf.format(date);
		_writer = new FileWriter("Results_" + timeStamp + ".csv"); //TODO: change timestamp
	}
	
	public void printIterationStatistics(int iterNum, int load, int duration, int jobLength, ArrayList<ServerStatistics> serverStats, ArrayList<JobResult> jobsResults) throws IOException
	{
		_location.entering("printIterationStatistics()");
		printIterationInfo(iterNum, load, duration, jobLength);
		printClientStats(jobsResults);
		printServerStats(serverStats);
		_writer.flush();
		_location.exiting("printIterationStatistics()");
	}
	
	// print iteration info: Load (jobs per minute), Duration (minutes), Job length (seconds)
	private void printIterationInfo(int iterNum, int load, int duration, int jobLength) throws IOException
	{
		_location.entering("printIterationInfo()");
		// iteration number
		_writer.append("Iteration #" + iterNum);
		_writer.append(_newLine);
		// headers
		_writer.append("Load (jobs per minute)" + _seperator + "Duration (minutes)" + _seperator + "Job length (seconds)");
		_writer.append(_newLine);
		// info
		_writer.append((load + _seperator + duration + _seperator + jobLength/1000));
		_writer.append(_newLine);
		_location.exiting("printIterationInfo()");
	}
	
    // print client statistics
    // Total returned jobs		
    // For each priority: Returned jobs
    // Min job total time (seconds)
    // Max job total time (seconds)
    // Avg job total time (seconds)
	private void printClientStats(ArrayList<JobResult> jobsResults) throws IOException
	{
		_location.entering("printClientStats()");
		
		// headers
		_writer.append("Total returned jobs" + _seperator);
		_writer.append("HP returned jobs" + _seperator);
		_writer.append("LP returned jobs" + _seperator);
		_writer.append("Min job total time (seconds)" + _seperator);
		_writer.append("Max job total time (seconds)" + _seperator);
		_writer.append("Avg job total time (seconds)");
		_writer.append(_newLine);	
		
		// data
		ClientStatistics clientStatistics = new ClientStatistics(jobsResults);
		_writer.append(jobsResults.size() + _seperator);
		_writer.append(clientStatistics.getNumHPjobs() + _seperator);
		_writer.append(clientStatistics.getNumLPjobs() + _seperator);
		_writer.append(clientStatistics.getMinJobTime() + _seperator);
		_writer.append(clientStatistics.getMaxJobTime() + _seperator);
		_writer.append(Long.toString(clientStatistics.getAvgJobTime()));
		_writer.append(_newLine);	
		
		_location.exiting("printClientStats()");
	}
	
	// print each server statstics and an average
    // Max queue length
    // Avg queue length
    // Total idle time
    // Avg idle time
    // Min job length (seconds)
    // Max job length (seconds)
    // Avg job length (seconds)
    // For each priority: 
      // Max job queue time 
      // Avg job queue time 
      // Jobs received
      // Jobs finished
      // Jobs aborted
      // Jobs left    	
	private void printServerStats(ArrayList<ServerStatistics> serverStats) throws IOException
	{
		_location.entering("printServerStats()");
		
		long totalIdleTime = 0, avgIdleTime = 0, minJobLength = 0, maxJobLength = 0, avgJobLength = 0;
		long hpMaxQueueLength = 0, hpAvgQueueLength = 0, hpMaxJobQueueTime = 0, hpAvgJobQueueTime= 0;
		long hpJobsReceived = 0, hpJobsFinished = 0, hpJobsAborted = 0, hpJobsLeft = 0;
		long lpMaxQueueLength = 0, lpAvgQueueLength = 0, lpMaxJobQueueTime = 0, lpAvgJobQueueTime= 0;
		long lpJobsReceived = 0, lpJobsFinished = 0, lpJobsAborted = 0, lpJobsLeft = 0;
		
		// headers
		_writer.append(_seperator); //blank cell
		_writer.append("Total idle time (seconds)" + _seperator);
		_writer.append("Avg idle time (seconds)" + _seperator);
		_writer.append("Min job length (seconds)" + _seperator);
		_writer.append("Max job length (seconds)" + _seperator);
		_writer.append("Avg job length (seconds)" + _seperator);
		_writer.append("HP max queue length" + _seperator);
		_writer.append("HP avg queue length" + _seperator);		
		_writer.append("HP max job queue time" + _seperator);
		_writer.append("HP avg job queue time" + _seperator);
		_writer.append("HP jobs received" + _seperator);
		_writer.append("HP jobs finished" + _seperator);
		_writer.append("HP jobs aborted" + _seperator);
		_writer.append("HP jobs left" + _seperator);
		_writer.append("LP max queue length" + _seperator);
		_writer.append("LP avg queue length" + _seperator);		
		_writer.append("LP max job queue time" + _seperator);
		_writer.append("LP avg job queue time" + _seperator);
		_writer.append("LP jobs received" + _seperator);
		_writer.append("LP jobs finished" + _seperator);
		_writer.append("LP jobs aborted" + _seperator);
		_writer.append("LP jobs left");		
		_writer.append(_newLine);	
		
		// servers data
		for (int i = 0; i < serverStats.size(); ++i)
		{
			ServerStatistics stats = serverStats.get(i);
			_writer.append("Server" + (i + 1) + _seperator);
			
			totalIdleTime += stats.getTotalIdleTime();
			_writer.append(stats.getTotalIdleTime()/1000 + _seperator);
			avgIdleTime += stats.getAvgIdleTime();
			_writer.append(stats.getAvgIdleTime()/1000 + _seperator);
			//minJobLength += ;
			_writer.append("Min job length (seconds)" + _seperator); //TODO: implement
			// maxJobLength += ;
			_writer.append("Max job length (seconds)" + _seperator); //TODO: implement
			// avgJobLength += ;
			_writer.append("Avg job length (seconds)" + _seperator); //TODO: implement
			hpMaxQueueLength += stats.getMaxQueueLength(Priority.High);
			_writer.append(stats.getMaxQueueLength(Priority.High) + _seperator);
			hpAvgQueueLength += stats.getAvgQueueLength(Priority.High);
			_writer.append(stats.getAvgQueueLength(Priority.High) + _seperator);	
			// hpMaxJobQueueTime += ;
			_writer.append("HP max job queue time" + _seperator); //TODO: implement
			// hpAvgJobQueueTime += ;
			_writer.append("HP avg job queue time" + _seperator); //TODO: implement
			hpJobsReceived += stats.getJobsTotalCount(Priority.High);
			_writer.append(stats.getJobsTotalCount(Priority.High) + _seperator);//TODO: right method?
			hpJobsFinished += stats.getJobsFinishedCount(Priority.High);
			_writer.append(stats.getJobsFinishedCount(Priority.High) + _seperator);
			hpJobsAborted = stats.getJobsAbortedCount(Priority.High);
			_writer.append(stats.getJobsAbortedCount(Priority.High) + _seperator);
			// hpJobsLeft += ;
			_writer.append("HP jobs left" + _seperator);//TODO: implement
			lpMaxQueueLength += stats.getMaxQueueLength(Priority.Low);
			_writer.append(stats.getMaxQueueLength(Priority.Low) + _seperator);
			lpAvgQueueLength += stats.getAvgQueueLength(Priority.Low);
			_writer.append(stats.getAvgQueueLength(Priority.Low) + _seperator);		
			// lpMaxJobQueueTime += ;
			_writer.append("LP max job queue time" + _seperator); //TODO: implement
			// lpAvgJobQueueTime += ;
			_writer.append("LP avg job queue time" + _seperator); //TODO: implement
			lpJobsReceived += stats.getJobsTotalCount(Priority.Low);
			_writer.append(stats.getJobsTotalCount(Priority.Low) + _seperator);//TODO: right method?
			lpJobsFinished += stats.getJobsFinishedCount(Priority.Low);
			_writer.append(stats.getJobsFinishedCount(Priority.Low) + _seperator);
			lpJobsAborted += stats.getJobsAbortedCount(Priority.Low);
			_writer.append(stats.getJobsAbortedCount(Priority.Low) + _seperator);
			// lpJobsLeft += ;
			_writer.append("LP jobs left");//TODO: implement	
			_writer.append(_newLine);		
		}
		
		// avg data
		int numServers = serverStats.size();
		_writer.append("Average" + _seperator);
		
		_writer.append((totalIdleTime/numServers)/1000 + _seperator);
		_writer.append((avgIdleTime/numServers)/1000 + _seperator);
		_writer.append(minJobLength/numServers + _seperator);
		_writer.append(maxJobLength/numServers + _seperator);
		_writer.append(avgJobLength/numServers + _seperator);
		_writer.append(hpMaxQueueLength/numServers + _seperator);
		_writer.append(hpAvgQueueLength/numServers + _seperator);		
		_writer.append(hpMaxJobQueueTime/numServers + _seperator);
		_writer.append(hpAvgJobQueueTime/numServers + _seperator);
		_writer.append(hpJobsReceived/numServers + _seperator);
		_writer.append(hpJobsFinished/numServers + _seperator);
		_writer.append(hpJobsAborted/numServers + _seperator);
		_writer.append(hpJobsLeft/numServers + _seperator);
		_writer.append(lpMaxQueueLength/numServers + _seperator);
		_writer.append(lpAvgQueueLength/numServers + _seperator);		
		_writer.append(lpMaxJobQueueTime/numServers + _seperator);
		_writer.append(lpAvgJobQueueTime/numServers + _seperator);
		_writer.append(lpJobsReceived/numServers + _seperator);
		_writer.append(lpJobsFinished/numServers + _seperator);
		_writer.append(lpJobsAborted/numServers + _seperator);
		_writer.append(Long.toString(lpJobsLeft/numServers));			
		
		_writer.append(_newLine);	
		_writer.append(_newLine);	
		
		_location.exiting("printServerStats()");
	}


}
