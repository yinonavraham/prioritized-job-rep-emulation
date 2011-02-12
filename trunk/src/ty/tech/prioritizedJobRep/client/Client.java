package ty.tech.prioritizedJobRep.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import ty.tech.prioritizedJobRep.api.ProxyFactory;
import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.JobResult;
import ty.tech.prioritizedJobRep.common.ServerStatistics;
import ty.tech.prioritizedJobRep.common.StatisticsHandler;
import ty.tech.prioritizedJobRep.dispatcher.Dispatcher;
import ty.tech.prioritizedJobRep.dispatcher.DispatcherImpl;
import ty.tech.prioritizedJobRep.logging.Logger;

public class Client 
{
	private Dispatcher _dispatcher = null;
	private int _duration = 0;
	private int _jobLength = 0;
	private ArrayList<Integer> _loads = null;	
	
	public Client(EndPoint dispatcherEndPoint, int duration, int jobLength, ArrayList<Integer> loads) throws RemoteException, NotBoundException
	{
		_duration = duration;
		_jobLength = jobLength;
		_loads = loads;	
		registerAtDispatcher(dispatcherEndPoint);
	}
	
	private void registerAtDispatcher(EndPoint endPoint) throws RemoteException, NotBoundException
	{
       _dispatcher = ProxyFactory.createDispatcherProxy(endPoint);
       String msg = "Client registered to dispatcher " + endPoint.getHostName() + ":" + endPoint.getPort();
       System.out.println(msg);
       Logger.getLocation(Client.class).debug(msg);       
	}
	
	public void start() throws InterruptedException, IOException
	{
		Logger.getLocation(Client.class).entering("start()");
		
		int jobId = 0;
		StatisticsHandler statisticsHandler = new StatisticsHandler();
		
		// the number of loads defines the number of iterations
		for (int numIter = 0; numIter < _loads.size(); ++numIter)  
		{
			System.out.println("starting iteration " + numIter);
			Logger.getLocation(Client.class).debug("Starting iteration number " + (numIter+1) + ", load is " + _loads.get(numIter));
			
			_dispatcher.resetAllServers(); // reset server statistics times etc.
			JobsGeneratorThread thread = new JobsGeneratorThread(jobId, _duration, _jobLength, _loads.get(numIter), _dispatcher);
			thread.start();
			thread.join(); // wait for iteration to finish
			jobId = thread.getLastJobId();
			
			// get statistics and jobs results
			try 
			{
				ArrayList<ServerStatistics> serversStatistics = _dispatcher.getServersStatistics();
				ArrayList<JobResult> jobsResults;
				synchronized (DispatcherImpl.getJobResultsLock()) 
				{
					jobsResults = _dispatcher.getJobsResults();
				}
				if (serversStatistics.size() == 0 ||  jobsResults.size() == 0)
				{
					String msg = "In iteration #" + numIter + ", serversStatistics.size()=" + serversStatistics.size() 
					+ " and jobsResults.size()=" + jobsResults.size();
					System.err.println(msg);
					Logger.getLocation(Client.class).debug(msg);
				}
				statisticsHandler.printIterationStatistics(numIter+1, _loads.get(numIter), _duration, _jobLength, serversStatistics, jobsResults);
			} 
			catch (RemoteException e) 
			{
				System.err.println("Error occured in JobsGeneratorThread.resetAllServers(): " + e.getMessage());
			}			

			// stop Client
			thread.interrupt();
		}

		//TODO: call statistics
		Logger.getLocation(Client.class).debug("Calculating statistics");
		
		Logger.getLocation(Client.class).exiting("start()");
	}
}
