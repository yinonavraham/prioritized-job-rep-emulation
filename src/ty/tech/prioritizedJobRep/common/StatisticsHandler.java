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
	private String _separator = ",";
	private Location _location = Logger.getLocation(this.getClass());
	private boolean _headersPrinted = false;
	
	public StatisticsHandler() throws IOException 
	{
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("ddMMyyyy");
        DateFormat tf = new SimpleDateFormat("HHmmss");
		String timeStamp = df.format(date) + "_" + tf.format(date);
		_writer = new FileWriter("Results_" + timeStamp + ".csv"); //TODO: change timestamp
	}
	
	public void printIterationStatistics(int iterNum, int load, int duration, int jobLength, ArrayList<ServerStatistics> serverStats, ArrayList<JobResult> jobsResults) throws IOException
	{
		_location.entering("printIterationStatistics()");
		if (!_headersPrinted) printHeaders(serverStats);
		printIterationInfo(iterNum, load, duration, jobLength);
		printClientStats(jobsResults);
		printServerStats(serverStats);
		_writer.append(_newLine);
		_writer.flush();
		_location.exiting("printIterationStatistics()");
	}
	
	private void printHeaders(ArrayList<ServerStatistics> serverStats) throws IOException
	{
		_location.entering("printHeaders(ArrayList<ServerStatistics>)", serverStats);
		_headersPrinted = true;
		// First row - servers title
		for (int i = 0; i < 15; i++) _writer.append(_separator);
		_writer.append("Servers - Overall");
		for (int i = 0; i < serverStats.size(); i++) 
		{
			for (int j = 0; j < 15; j++) _writer.append(_separator);
			_writer.append("Server " + (i+1));
		}
		// New line after first row 
		_writer.append(_newLine);
		// Iteration info headers
		_writer.append("Iteration #" + _separator);
		_writer.append("Load (jobs per minute)" + _separator);
		_writer.append("Duration (minutes)" + _separator);
		_writer.append("Job length (seconds)" + _separator);
		// Client statistics headers
		_writer.append("Total returned jobs" + _separator);
		_writer.append("HP returned jobs" + _separator);
		_writer.append("LP returned jobs" + _separator);
		_writer.append("Min job total time (seconds)" + _separator);
		_writer.append("Max job total time (seconds)" + _separator);
		_writer.append("Avg job total time (seconds)" + _separator);
		_writer.append("Max job queue time (seconds)" + _separator);
		_writer.append("Avg job queue time (seconds)" + _separator);
		_writer.append("Min job length (seconds)" + _separator);
		_writer.append("Max job length (seconds)" + _separator);
		_writer.append("Avg job length (seconds)" + _separator);
		// Server statistics - total & per server
		for (int i = 0; i < serverStats.size()+1; i++) 
		{
			_writer.append("Total idle time (seconds)" + _separator);
			_writer.append("Avg idle time (seconds)" + _separator);
			_writer.append("Total Jobs Finished" + _separator);
			_writer.append("HP max queue length" + _separator);
			_writer.append("HP avg queue length" + _separator);
			_writer.append("HP jobs received" + _separator);
			_writer.append("HP jobs finished" + _separator);
			_writer.append("HP jobs aborted" + _separator);
			_writer.append("HP jobs left" + _separator);
			_writer.append("LP max queue length" + _separator);
			_writer.append("LP avg queue length" + _separator);
			_writer.append("LP jobs received" + _separator);
			_writer.append("LP jobs finished" + _separator);
			_writer.append("LP jobs aborted" + _separator);
			_writer.append("LP jobs left" + _separator);
		}
		// New line after headers
		_writer.append(_newLine);
		_location.exiting("printHeaders(ArrayList<ServerStatistics>)");
	}

	// print iteration info: Load (jobs per minute), Duration (minutes), Job length (seconds)
	private void printIterationInfo(int iterNum, int load, int duration, int jobLength) throws IOException
	{
		_location.entering("printIterationInfo()");
		// iteration number
		_writer.append(iterNum + _separator);
		// info
		_writer.append(load + _separator);
		_writer.append(duration + _separator);
		_writer.append(jobLength/1000 + _separator);

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
		
		// data
		ClientStatistics clientStatistics = new ClientStatistics(jobsResults);
		_writer.append(jobsResults.size() + _separator);
		_writer.append(clientStatistics.getNumHPjobs() + _separator);
		_writer.append(clientStatistics.getNumLPjobs() + _separator);
		_writer.append(clientStatistics.getMinJobTime()/1000.0 + _separator);
		_writer.append(clientStatistics.getMaxJobTime()/1000.0 + _separator);
		_writer.append(clientStatistics.getAvgJobTime()/1000.0 + _separator);
		_writer.append(clientStatistics.getMaxJobQueueTime()/1000.0 + _separator);
		_writer.append(clientStatistics.getAvgJobQueueTime()/1000.0 + _separator);
		_writer.append(clientStatistics.getMinJobExecTime()/1000.0 + _separator);
		_writer.append(clientStatistics.getMaxJobExecTime()/1000.0 + _separator);
		_writer.append(clientStatistics.getAvgJobExecTime()/1000.0 + _separator);
		
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
		
		long totalIdleTime = 0, avgIdleTime = 0;
		long hpMaxQueueLength = 0, hpAvgQueueLength = 0;
		long hpJobsReceived = 0, hpJobsFinished = 0, hpJobsAborted = 0, hpJobsLeft = 0;
		long lpMaxQueueLength = 0, lpAvgQueueLength = 0;
		long lpJobsReceived = 0, lpJobsFinished = 0, lpJobsAborted = 0, lpJobsLeft = 0;
		long totalJobsFinished = 0;
		
		// servers data
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < serverStats.size(); ++i)
		{
			ServerStatistics stats = serverStats.get(i);
			
			// Total Idle Time (seconds)
			totalIdleTime += stats.getTotalIdleTime();
			sb.append(stats.getTotalIdleTime()/1000.0 + _separator);
			// Average Idle Time (seconds)
			avgIdleTime += stats.getAvgIdleTime();
			sb.append(stats.getAvgIdleTime()/1000.0 + _separator);
			// Total Jobs Finished
			totalJobsFinished += stats.getJobsFinishedCount(null);
			sb.append(stats.getJobsFinishedCount(null) + _separator);
			// HP Max Queue Length
			hpMaxQueueLength += stats.getMaxQueueLength(Priority.High);
			sb.append(stats.getMaxQueueLength(Priority.High) + _separator);
			// HP Average Queue Length
			hpAvgQueueLength += stats.getAvgQueueLength(Priority.High);
			sb.append(stats.getAvgQueueLength(Priority.High) + _separator);
			// HP Jobs Count
			hpJobsReceived += stats.getJobsTotalCount(Priority.High);
			sb.append(stats.getJobsTotalCount(Priority.High) + _separator);
			// HP Jobs Finished
			hpJobsFinished += stats.getJobsFinishedCount(Priority.High);
			sb.append(stats.getJobsFinishedCount(Priority.High) + _separator);
			// HP Jobs Aborted
			hpJobsAborted = stats.getJobsAbortedCount(Priority.High);
			sb.append(stats.getJobsAbortedCount(Priority.High) + _separator);
			// HP jobs left
			 hpJobsLeft += stats.getJobsInQueueCount(Priority.High);
			 sb.append(stats.getJobsInQueueCount(Priority.High) + _separator);
			// LP Max Queue Length
			lpMaxQueueLength += stats.getMaxQueueLength(Priority.Low);
			sb.append(stats.getMaxQueueLength(Priority.Low) + _separator);
			// LP Average Queue Length
			lpAvgQueueLength += stats.getAvgQueueLength(Priority.Low);
			sb.append(stats.getAvgQueueLength(Priority.Low) + _separator);
			// LP Jobs Count
			lpJobsReceived += stats.getJobsTotalCount(Priority.Low);
			sb.append(stats.getJobsTotalCount(Priority.Low) + _separator);
			// LP Jobs Finished
			lpJobsFinished += stats.getJobsFinishedCount(Priority.Low);
			sb.append(stats.getJobsFinishedCount(Priority.Low) + _separator);
			// LP Jobs Aborted
			lpJobsAborted += stats.getJobsAbortedCount(Priority.Low);
			sb.append(stats.getJobsAbortedCount(Priority.Low) + _separator);
			// LP Total Jobs Left
			 lpJobsLeft += stats.getJobsInQueueCount(Priority.Low);
			 sb.append(stats.getJobsInQueueCount(Priority.Low) + _separator);
		}
		
		// Average data for all the servers
		double numServers = (double)serverStats.size();
		_writer.append((double)(totalIdleTime/1000.0)/numServers + _separator);
		_writer.append((double)(avgIdleTime/1000.0)/numServers + _separator);
		_writer.append((double)totalJobsFinished/numServers + _separator);
		_writer.append((double)hpMaxQueueLength/numServers + _separator);
		_writer.append((double)hpAvgQueueLength/numServers + _separator); 
		_writer.append((double)hpJobsReceived/numServers + _separator);
		_writer.append((double)hpJobsFinished/numServers + _separator);
		_writer.append((double)hpJobsAborted/numServers + _separator);
		_writer.append((double)hpJobsLeft/numServers + _separator);
		_writer.append((double)lpMaxQueueLength/numServers + _separator);
		_writer.append((double)lpAvgQueueLength/numServers + _separator); 
		_writer.append((double)lpJobsReceived/numServers + _separator);
		_writer.append((double)lpJobsFinished/numServers + _separator);
		_writer.append((double)lpJobsAborted/numServers + _separator);
		_writer.append(Double.toString((double)lpJobsLeft/numServers) + _separator);
		
		// Data of each server
		_writer.append(sb.toString());
		
		_location.exiting("printServerStats()");
	}


}
