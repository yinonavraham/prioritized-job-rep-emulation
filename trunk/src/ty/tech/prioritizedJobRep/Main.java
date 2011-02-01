package ty.tech.prioritizedJobRep;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import ty.tech.prioritizedJobRep.api.ProxyFactory;
import ty.tech.prioritizedJobRep.client.Client;
import ty.tech.prioritizedJobRep.common.EndPoint;
import ty.tech.prioritizedJobRep.common.Entities;
import ty.tech.prioritizedJobRep.dispatcher.Dispatcher;
import ty.tech.prioritizedJobRep.dispatcher.DispatcherImpl;
import ty.tech.prioritizedJobRep.logging.Logger;
import ty.tech.prioritizedJobRep.server.Server;
import ty.tech.prioritizedJobRep.server.ServerImpl;


public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Logger.getLocation(Main.class).entering("main(String[])", (Object[])args);
		
		CommandArguments cmdArgs = new CommandArguments();
		try
		{
			if (cmdArgs.parse(args) && !cmdArgs.isHelpRequested())
			{
				CommandOption cmdOp = cmdArgs.getCommandOption();
				switch (cmdOp)
				{
					case StartServer:
						startServer(cmdArgs);
						break;
					case StopServer:
						stopServer(cmdArgs);
						break;
					case RegisterServer:
						registerServer(cmdArgs);
						break;
					case StartDispatcher:
						startDispatcher(cmdArgs);
						break;
					case StopDispatcher:
						stopDispatcher(cmdArgs);
						break;
					case StartClient:
						startClient(cmdArgs);
						break;
					default:
						System.err.println(cmdOp + " : Command is not supported");
						Logger.getLocation(Main.class).debug(cmdOp + " : Command is not supported");
						cmdArgs.printCommandSyntax();
				}
			}
			else
			{
				cmdArgs.printCommandSyntax();
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			Logger.getLocation(Main.class).throwing("main(String[])", e);
		}
		Logger.getLocation(Main.class).exiting("main(String[])");
	}

	private static void registerServer(CommandArguments cmdArgs) throws AccessException, RemoteException, NotBoundException
	{
		Logger.getLocation(Main.class).entering("registerServer(CommandArguments)", cmdArgs);
		
		System.out.println("Registering server in a dispatcher...");
		int port = cmdArgs.getPort();
		System.out.println("Server listens on port: " + port);
		Logger.getLocation(Main.class).debug("Server listens on port: " + port);
		
		String targetHost = cmdArgs.getTargetHost();
		int targetPort = cmdArgs.getTargetPort();
		System.out.println("Registering the server in the target dispatcher: " + targetHost + ":" + targetPort);
		Logger.getLocation(Main.class).debug("Registering the server in the target dispatcher: " + targetHost + ":" + targetPort);

		Server server = ProxyFactory.createServerProxy("localhost", port);
		server.register(targetHost, targetPort);
		System.out.println("Server was registered successfully.");
		Logger.getLocation(Main.class).debug("Server was registered successfully.");
		
		Logger.getLocation(Main.class).exiting("registerServer(CommandArguments)");
	}

	private static void stopDispatcher(CommandArguments cmdArgs) throws RemoteException, NotBoundException
	{
		Logger.getLocation(Main.class).entering("stopDispatcher(CommandArguments)", cmdArgs);
		
		System.out.println("Stopping dispatcher...");
		int port = cmdArgs.getPort();
		System.out.println("Dispatcher listens on port: " + port);
		Logger.getLocation(Main.class).debug("Stopping dispatcher which listens on port: " + port);

		Dispatcher dispatcher = ProxyFactory.createDispatcherProxy("localhost", port);
		dispatcher.stop();
		Logger.getLocation(Main.class).debug("Server stopped.");
		
		Logger.getLocation(Main.class).exiting("stopDispatcher(CommandArguments)");		
	}

	private static void stopServer(CommandArguments cmdArgs) throws AccessException, RemoteException, NotBoundException
	{
		Logger.getLocation(Main.class).entering("stopServer(CommandArguments)", cmdArgs);
		
		System.out.println("Stopping server...");
		int port = cmdArgs.getPort();
		System.out.println("Server listens on port: " + port);
		Logger.getLocation(Main.class).debug("Stopping server which listens on port: " + port);

		Server server = ProxyFactory.createServerProxy("localhost", port);
		server.stop();
		Logger.getLocation(Main.class).debug("Server stopped.");
		
		Logger.getLocation(Main.class).exiting("stopServer(CommandArguments)");
	}

	private static void startClient(CommandArguments cmdArgs) throws NotBoundException, InterruptedException, IOException
	{
		Logger.getLocation(Main.class).entering("startClient(CommandArguments)", cmdArgs);
		
		System.out.println("Starting client on local host.");
		String targetHost = cmdArgs.getTargetHost();
		int targetPort = cmdArgs.getTargetPort();
		int duration = cmdArgs.getDuration();
		int jobLength = cmdArgs.getJobLength() * 1000 ; // in milliseconds
		ArrayList<Integer> loads = cmdArgs.getLoads();
		
		System.out.println("Client will work with dispatcher on " + targetHost + ":" + targetPort);
		Logger.getLocation(Main.class).debug("Starting client with dispatcher on " + targetHost + ":" + targetPort);
		
		// Create the server object 
		EndPoint endPoint = new EndPoint(targetHost, targetPort);
		Client client = new Client(endPoint, duration, jobLength, loads);
		client.start();
	}

	private static void startDispatcher(CommandArguments cmdArgs) throws RemoteException, SocketException, UnknownHostException, AlreadyBoundException, NotBoundException
	{
		Logger.getLocation(Main.class).entering("startDispatcher(CommandArguments)", cmdArgs);
		
		System.out.println("Starting dispatcher on local host.");
		int port = cmdArgs.getPort();
		System.out.println("Dispatcher will listen on port: " + port);
		Logger.getLocation(Main.class).debug("Starting dispatcher on port: " + port);
		// Create the dispatcher object 
		DispatcherImpl dispatcherObj = new DispatcherImpl(port);
		// Cast the dispatcher object to a remote object 
		Dispatcher dispatcher = (Dispatcher) UnicastRemoteObject.exportObject(dispatcherObj, 0);
		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.createRegistry(port);
		registry.bind(Entities.DISPATCHER, dispatcher);
		System.out.println("Dispatcher is running.");
		Logger.getLocation(Main.class).debug("Dispatcher is running.");
		dispatcherObj.start();  // program will remain here until dispatcher is stopped

		registry.unbind(Entities.DISPATCHER);
		UnicastRemoteObject.unexportObject(dispatcherObj, true);
		System.out.println("Dispatcher is stopped.");
		Logger.getLocation(Main.class).debug("Dispatcher is stopped.");
		
		Logger.getLocation(Main.class).exiting("startDispatcher(CommandArguments)");		
	}

	private static void startServer(CommandArguments cmdArgs) throws AlreadyBoundException, IOException, NotBoundException
	{
		Logger.getLocation(Main.class).entering("startServer(CommandArguments)", cmdArgs);
		
		System.out.println("Starting server on local host.");
		int port = cmdArgs.getPort();
		System.out.println("Server will listen on port: " + port);
		Logger.getLocation(Main.class).debug("Starting server on port: " + port);
		
		// Create the server object 
		ServerImpl serverObj = new ServerImpl(port);
		// Cast the server object to a remote object 
		Server server = (Server) UnicastRemoteObject.exportObject(serverObj, 0);
		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.createRegistry(port);
		registry.bind(Entities.SERVER, server);
		System.out.println("Server is running.");
		Logger.getLocation(Main.class).debug("Server is running.");
		serverObj.start(); // program will remain here until server is stopped
		
		registry.unbind(Entities.SERVER);
		UnicastRemoteObject.unexportObject(serverObj, true);
		System.out.println("Server is stopped.");
		Logger.getLocation(Main.class).debug("Server is stopped.");
		
		Logger.getLocation(Main.class).exiting("startServer(CommandArguments)");
	}

}
