package ty.tech.prioritizedJobRep;


public enum CommandOption
{
	StartServer,
	StopServer,
	RegisterServer,
	StartDispatcher,
	StopDispatcher,
	StartClient,
	SetDispatcherPolicy,
	GetLocalAddress;
	
	public static CommandOption valueOfIgnoreCase(String name)
	{
		for (CommandOption cmdOp : values())
		{
			if (cmdOp.name().equalsIgnoreCase(name)) return cmdOp;
		}
		return null;
	}
}
