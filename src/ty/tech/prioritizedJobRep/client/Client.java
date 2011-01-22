package ty.tech.prioritizedJobRep.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.Entities;
import ty.tech.prioritizedJobRep.dispatcher.Dispatcher;
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
        Registry registry = LocateRegistry.getRegistry(endPoint.getHostAddress());
        _dispatcher = (Dispatcher) registry.lookup(Entities.DISPATCHER);		
	}
	
	public void start() throws InterruptedException
	{
		Logger.getLocation(Client.class).entering("start()");
		int jobId = 0;
		//TODO: start the dispatcher
		Logger.getLocation(Client.class).debug("Client registered to dispatcher");
		
		// the number of loads defines the number of iterations
		for (int numIter = 0; numIter < _loads.size(); ++numIter)  
		{
			System.out.println("starting iteration " + numIter);
			Logger.getLocation(Client.class).debug("Starting iteration number " + (numIter+1) + ", load is " + _loads.get(numIter));
			
			JobsGeneratorThread thread = new JobsGeneratorThread(jobId,_duration, _jobLength, _loads.get(numIter), _dispatcher);
			thread.start();
			thread.join(); // wait for all iteration to finish
			jobId = thread.getLastJobId();
			thread.interrupt();
		}

		//TODO: call statistics
		Logger.getLocation(Client.class).debug("Calculating statistics");
		
		Logger.getLocation(Client.class).exiting("start()");
	}

}
