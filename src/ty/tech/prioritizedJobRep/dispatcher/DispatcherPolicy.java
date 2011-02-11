package ty.tech.prioritizedJobRep.dispatcher;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ty.tech.prioritizedJobRep.common.Priority;


public class DispatcherPolicy implements Serializable
{
	private static final long serialVersionUID = -2538122756588242965L;


	public static final String PROP_JOB_REPLICATIONS = "job.replications";
	

	/**
	 * Number of replications of each priority to create for a job 
	 */
	private Map<Priority,Integer> _jobReplicationsNum = new HashMap<Priority, Integer>();
	
	
	public DispatcherPolicy()
	{
		initDefault();
	}
	
	
	public DispatcherPolicy(Properties properties)
	{
		initDefault();
		init(properties);
	}
	
	
	private void initDefault()
	{
		for (Priority priority : Priority.values())
		{
			_jobReplicationsNum.put(priority, 1);
		}
	}
	
	
	private void init(Properties prop)
	{
		String value;
		value = prop.getProperty(PROP_JOB_REPLICATIONS);
		if (value != null) parseJobReplicationsNum(value);
	}
	
	
	private void parseJobReplicationsNum(String value)
	{
		_jobReplicationsNum.clear();
		String[] priorities = value.split(";");
		for (String priority : priorities)
		{
			String[] parts = priority.split(":");
			Priority p = Priority.valueOf(parts[0]);
			Integer  i = Integer.valueOf(parts[1]);
			_jobReplicationsNum.put(p, i);
		}
	}


	/**
	 * Get the number of replications (of a given priority) to create for each job 
	 * @param priority - priority of the replication 
	 * @return the number of replications
	 */
	public int getJobReplicationsNumber(Priority priority)
	{
		Integer num = _jobReplicationsNum.get(priority);
		return (num == null || num < 0) ? 0 : num;
	}
	
	
	@Override
	public String toString()
	{
		return "Job Replications Number: " + _jobReplicationsNum;
	}
}
