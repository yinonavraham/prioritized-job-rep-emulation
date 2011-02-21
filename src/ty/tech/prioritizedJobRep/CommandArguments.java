package ty.tech.prioritizedJobRep;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class CommandArguments
{
	private static final String NEW_LINE = System.getProperty("line.separator");

	private Map<String,CommandArg> _args = new HashMap<String, CommandArg>();
	private Map<String,Object> _props = new HashMap<String, Object>();
	
	public CommandArguments()
	{
		initSupportedArguments();
	}
	

	private void initSupportedArguments()
	{
		_args.clear();
		_args.put("cmd", new CommandArg(
			"cmd", 
			"Command to execute. Supported commands (all operate on the local machine): " + NEW_LINE
			+ "  StartServer         - Start a server" + NEW_LINE
			+ "  StopServer          - Stop a server" + NEW_LINE
			+ "  RegisterServer      - Register a server in the dispatcher" + NEW_LINE
			+ "  StartDispatcher     - Start the dispatcher" + NEW_LINE
			+ "  StopDispatcher      - Stop the dispatcher" + NEW_LINE
			+ "  SetDispatcherPolicy - Set the policy for the dispatcher" + NEW_LINE
			+ "  GetLocalAddress     - Display the address of the local host" + NEW_LINE
			+ "  StartClient         - Start the clients activity simulation",
			true, true));
		_args.put("port", new CommandArg("port", "Port number of the entity on the local machine", true, false));
		_args.put("target", new CommandArg("target", "Remote target identification in the format <HOST NAME or IP>:<PORT>", true, false));
		_args.put("duration", new CommandArg("duration", "Duration of an iteration (in minutes)", true, false));
		_args.put("joblength", new CommandArg("joblength", "Duration of job (in seconds)", true, false));
		_args.put("loads", new CommandArg("loads", "Number of jobs to be sent in a minute", true, false));
		_args.put("policy", new CommandArg("policy", "Policy properties file", true, false));
		_args.put("help", new CommandArg("help", "Display help on the command line usage", false, false));
		_args.put("?", new CommandArg("?", "Same as help", false, false));
	}

	
	public boolean parse(String[] args)
	{
		String[] argsMissing = requiredArgsMissing(args);
		if (argsMissing.length > 0)
		{
			System.err.println("The following required arguments are missing:");
			System.err.println(Arrays.toString(argsMissing));
			return false;
		}
		else if (args != null)
		{
			String prevArg = null;
			try
			{
				for (String arg : args)
				{
					if (arg.startsWith("-") || arg.startsWith("/"))
					{
						if (prevArg == null)
						{
							prevArg = processArg(arg.substring(1).toLowerCase());
						}
						else
						{
							throw new IllegalArgumentException(prevArg + " : Argument is missing a value");
						}
					}
					else
					{
						processArgValue(prevArg, arg);
						prevArg = null;
					}
				}
			}
			catch (IllegalArgumentException e)
			{
				System.err.println(e.getMessage());
				return false;
			}
		}
		return true;
	}

	private String processArg(String arg) throws IllegalArgumentException
	{
		if (_args.containsKey(arg))
		{
			_props.put(arg, null);
			return _args.get(arg).hasValue() ? arg : null;
		}
		else
		{
			throw new IllegalArgumentException(arg + " : Illegal command argument");
		}
	}


	private void processArgValue(String arg, String value)
	{
		if (_props.containsKey(arg))
		{
			// e.g. -cmd StartServer
			if ("cmd".equals(arg))
			{
				CommandOption cmdOp = CommandOption.valueOfIgnoreCase(value);
				if (cmdOp != null) _props.put(arg, cmdOp);
				else throw new IllegalArgumentException(value + " : Illegal command");
			}
			// e.g. -port 1234
			else if ("port".equals(arg))
			{
				try 
				{
					int port = Integer.parseInt(value);
					if (port < 0 || port > Integer.MAX_VALUE)
						throw new IllegalArgumentException(value + " : Illegal port number");
					_props.put(arg, port);
				}
				catch (NumberFormatException e)
				{
					throw new IllegalArgumentException(value + " : Illegal port number",e);
				}
			}
			// e.g. -target MYMACHINE:1234
			else if ("target".equals(arg))
			{
				String[] parts = value.split(":");
				if (parts.length != 2)
					throw new IllegalArgumentException(value + " : Invalid target syntax, use: <HOST>:<PORT>");
				try 
				{
					int port = Integer.parseInt(parts[1]);
					if (port < 0 || port > Integer.MAX_VALUE)
						throw new IllegalArgumentException(parts[1] + " : Illegal port number");
					_props.put(arg, value);
				}
				catch (NumberFormatException e)
				{
					throw new IllegalArgumentException(value + " : Illegal port number",e);
				}
			}
			// e.g. -duration 5 (in minutes)
			else if ("duration".equals(arg))
			{
				try 
				{
					int duration = Integer.parseInt(value);
					if (duration < 0 || duration > Integer.MAX_VALUE)
						throw new IllegalArgumentException(value + " : Illegal duration (minutes)");
					_props.put(arg, duration);
				}
				catch (NumberFormatException e)
				{
					throw new IllegalArgumentException(value + " : Illegal duration (minutes)",e);
				}				
			}
			// e.g. -joblength 1 (in seconds)
			else if ("joblength".equals(arg))
			{
				try 
				{
					int joblength = Integer.parseInt(value);
					if (joblength < 0 || joblength > Integer.MAX_VALUE)
						throw new IllegalArgumentException(value + " : Illegal job length number (seconds)");
					_props.put(arg, joblength);
				}
				catch (NumberFormatException e)
				{
					throw new IllegalArgumentException(value + " : Illegal job length number (seconds)",e);
				}				
			}			
			// e.g. -loads 100,200,300 (jobs per minute)
			else if ("loads".equals(arg))
			{
				_props.put(arg, parseLoads(value));				
			}			
			// e.g. -policy dispatcher.policy
			else if ("policy".equals(arg))
			{
				try
				{
					Properties properties = new Properties();
					properties.load(new FileInputStream(value));
					_props.put(arg, properties);	
				}
				catch (IOException e)
				{
					throw new IllegalArgumentException(
						String.format("Failed to load the policy file '%s'. Reason: %s",value,e.getMessage()), e);
				}
			}
		}
		else
		{
			throw new IllegalArgumentException(arg + " : Argument was not given for this value");
		}
	}
	
	private ArrayList<Integer> parseLoads(String value)
	{
		ArrayList<Integer> loads = new ArrayList<Integer>();
		
		String[] loadsArr = value.split(",");
		for (int i = 0; i < loadsArr.length ; i++)
		{
			int load = Integer.parseInt(loadsArr[i]);
			if (load < 0 || load > Integer.MAX_VALUE)
				throw new IllegalArgumentException(value + " : Illegal load number");
			else
				loads.add(load);
		}
		
		return loads;
	}
	
	public void printCommandSyntax()
	{
		System.err.println("Following are the supported command arguments:");
		for (CommandArg arg : _args.values())
		{
			System.err.println();
			System.err.println(arg);
		}
		System.err.println();
		System.err.println("Examples");
		System.err.println(" 1. To start a server on the local host with port 1234 use:");
		System.err.println("       -cmd StartServer -port 1234");
		System.err.println(" 2. To register a server on the local host with port 1234,");
		System.err.println("    in the dispatcher on MYMACHINE with port 4321 use:");
		System.err.println("       -cmd RegisterServer -port 1234 -target MYMACHINE:4321");
		System.err.println(" 3. To start a dispatcher on the local host with port 1234 use:");
		System.err.println("       -cmd StartDispatcher -port 1234");
		System.err.println(" 4. To set the policy for a dispatcher on the local host with port 1234 use:");
		System.err.println("       -cmd SetDispatcherPolicy -port 1234 -policy dispatcher.policy");
		System.err.println(" 5. To start a client working with a dispatcher on the local host with port 1234,");
		System.err.println("    length of each duration 5 mins, length of each job 1 sec, ");
		System.err.println("    and 3 iterations of loads 200, 300 and 400 jobs per minute, use: ");
		System.err.println("       -cmd StartClient -duration 5 -jobLength 1 -loads 200,300,400 -target localhost:1234");		
		System.err.println();
	}


	private String[] requiredArgsMissing(String[] args)
	{
		List<String> missingArgs = new ArrayList<String>();
		List<String> argsList = new ArrayList<String>();
		if (args != null)
		{
			for (String arg : args)
				if (arg.startsWith("-") || arg.startsWith("/"))
					argsList.add(arg.substring(1).toLowerCase());
		}
		for (CommandArg arg : _args.values())
		{
			if (arg.isRequired() && 
				(argsList.size() == 0 || !argsList.contains(arg.getArgument())))
			{
				missingArgs.add(arg.getArgument());
			}
		}
		return missingArgs.toArray(new String[missingArgs.size()]);
	}
	
	
	public Object getArgValue(String arg)
	{
		return _props.get(arg);
	}
	
	
	public boolean containsArg(String arg)
	{
		return _props.containsKey(arg);
	}
	
	
	public boolean isHelpRequested()
	{
		return _props.containsKey("?") || _props.containsKey("help"); 
	}
	
	
	public CommandOption getCommandOption()
	{
		return (CommandOption)_props.get("cmd");
	}
	
	
	public int getPort()
	{
		Object obj = _props.get("port");
		if (obj != null) return (Integer)obj;
		else throw new IllegalArgumentException("Command is missing an argument: port");		
	}
	
	
	public String getTargetHost()
	{
		Object obj = _props.get("target");
		if (obj != null) return ((String)obj).split(":")[0];
		else throw new IllegalArgumentException("Command is missing an argument: target host");
	}
	
	
	public int getTargetPort()
	{
		Object obj = _props.get("target");
		if (obj != null) return Integer.valueOf(((String)_props.get("target")).split(":")[1]);
		else throw new IllegalArgumentException("Command is missing an argument: target port");
	}
	
	public int getDuration()
	{
		Object obj = _props.get("duration");
		if (obj != null) return (Integer)obj;
		else throw new IllegalArgumentException("Command is missing an argument: duration");
	}
	
	public int getJobLength()
	{
		Object obj = _props.get("joblength");
		if (obj != null) return (Integer)obj;
		else throw new IllegalArgumentException("Command is missing an argument: jobLength");
	}	
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getLoads()
	{
		Object obj = _props.get("loads");
		if (obj != null) return (ArrayList<Integer>)obj;
		else throw new IllegalArgumentException("Command is missing an argument: loads");
	}
	
	
	public Properties getPolicy()
	{
		Object obj = _props.get("policy");
		if (obj != null) return (Properties)obj;
		else throw new IllegalArgumentException("Command is missing an argument: policy");
	}
	
	
	@Override
	public String toString()
	{
		return _props.toString();
	}
	
	
	public class CommandArg
	{
		private String _arg;
		private String _desc;
		private boolean _hasValue;
		private boolean _required;
		
		public CommandArg(String arg, String desc, boolean hasValue, boolean required)
		{
			_arg = arg;
			_desc = desc;
			_hasValue = hasValue;
			_required = required;
		}
		
		
		public String getArgument()
		{
			return _arg;
		}
		
		
		public String getDescription()
		{
			return _desc;
		}
		
		
		public boolean isRequired()
		{
			return _required;
		}
		
		
		public boolean hasValue()
		{
			return _hasValue;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("-").append(_arg);
			sb.append(" [");
			sb.append(_required == true ? "Required" : "Optional");
			sb.append("]");
			if (_desc != null)
			{
				sb.append(NEW_LINE);
				sb.append(_desc);	
			}
			return sb.toString();
		}
		
		
		@Override
		public int hashCode()
		{
			return _arg.hashCode();
		}
		
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj != null)
			{
				if (obj instanceof CommandArg)
				{
					CommandArg other = (CommandArg)obj;
					return _arg.equals(other._arg);
				}
				else if (obj instanceof String)
				{
					String other = (String)obj;
					return _arg.equals(other);
				}
			}
			return false;
		}
	}
}
