package ty.tech.prioritizedJobRep;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import ty.tech.prioritizedJobRep.api.ProxyFactory;
import ty.tech.prioritizedJobRep.common.Entities;
import ty.tech.prioritizedJobRep.common.Job;
import ty.tech.prioritizedJobRep.common.Priority;
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
//		Registry registry = LocateRegistry.getRegistry(port);
//		Server server = (Server)registry.lookup(Entities.SERVER);
		Server server = ProxyFactory.createServerProxy("localhost", port);
		server.register(targetHost, targetPort);
		System.out.println("Server was registered successfully.");
		Logger.getLocation(Main.class).debug("Server was registered successfully.");
		
		Logger.getLocation(Main.class).exiting("registerServer(CommandArguments)");
	}

	private static void stopDispatcher(CommandArguments cmdArgs)
	{
		System.err.println("stopDispatcher is not implemented yet");
	}

	private static void stopServer(CommandArguments cmdArgs) throws AccessException, RemoteException, NotBoundException
	{
		Logger.getLocation(Main.class).entering("stopServer(CommandArguments)", cmdArgs);
		
		System.out.println("Stopping server...");
		int port = cmdArgs.getPort();
		System.out.println("Server listens on port: " + port);
		Logger.getLocation(Main.class).debug("Stopping server which listens on port: " + port);
//		Registry registry = LocateRegistry.getRegistry(port);
//		Server server = (Server)registry.lookup(Entities.SERVER);
		Server server = ProxyFactory.createServerProxy("localhost", port);
		server.stop();
		Logger.getLocation(Main.class).debug("Server stopped.");
		
		Logger.getLocation(Main.class).exiting("stopServer(CommandArguments)");
	}

	private static void startClient(CommandArguments cmdArgs) throws AccessException, RemoteException, NotBoundException, InterruptedException
	{
		System.err.println("startClient is not implemented yet");
		int port = cmdArgs.getPort();
		Server server = ProxyFactory.createServerProxy("localhost", port);
		Job job = new Job("" + new Date().getTime(),3000);
		job.setPriority(Priority.Low);
		server.putJob(job);
		job = new Job("" + new Date().getTime(),1000);
		job.setPriority(Priority.Low);
		server.putJob(job);
		job = new Job("" + new Date().getTime(),1000);
		job.setPriority(Priority.Low);
		server.putJob(job);
		Thread.sleep(500);
		job = new Job("" + new Date().getTime(),1000);
		job.setPriority(Priority.High);
		server.putJob(job);
		job = new Job("" + new Date().getTime(),1000);
		job.setPriority(Priority.High);
		server.putJob(job);
		job = new Job("" + new Date().getTime(),1000);
		job.setPriority(Priority.High);
		server.putJob(job);
	}

	private static void startDispatcher(CommandArguments cmdArgs)
	{
		System.err.println("startDispatcher is not implemented yet");
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
		if (cmdArgs.containsArg("target"))
		{
			String targetHost = cmdArgs.getTargetHost();
			int targetPort = cmdArgs.getTargetPort();
			System.out.println("Registering the server in the target dispatcher: " + targetHost + ":" + targetPort);
			Logger.getLocation(Main.class).debug("Registering the server in the target dispatcher: " + targetHost + ":" + targetPort);
			serverObj.register(targetHost, targetPort);
			System.out.println("Server was registered successfully.");
			Logger.getLocation(Main.class).debug("Server was registered successfully.");
		}
		// Cast the server object to a remote object 
		Server server = (Server) UnicastRemoteObject.exportObject(serverObj, 0);
		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.createRegistry(port);
		registry.bind(Entities.SERVER, server);
		System.out.println("Server is running.");
		Logger.getLocation(Main.class).debug("Server is running.");
		serverObj.start();
		
		registry.unbind(Entities.SERVER);
		UnicastRemoteObject.unexportObject(serverObj, true);
		System.out.println("Server is stopped.");
		Logger.getLocation(Main.class).debug("Server is stopped.");
		
		Logger.getLocation(Main.class).exiting("startServer(CommandArguments)");
	}

}
