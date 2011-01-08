package ty.tech.prioritizedJobRep.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ty.tech.prioritizedJobRep.common.Priority;


public class ServerPolicy implements Serializable
{
	private static final long serialVersionUID = -8182740413697285903L;
	
	public enum ReEnter { First, Last, No };
	
	public static final String PROP_FAILED_JOBS_ALLOWED 		= "falied.jobs.allowed";
	public static final String PROP_NOTIFY_SIBLINGS_JOB_STARTED = "notify.siblings.on.job.start";
	public static final String PROP_RUN_HP_ALTHOUGH_LP_STARTED 	= "run.hp.job.although.lp.started";
	public static final String PROP_HP_JOB_PREEMPTION 			= "hp.job.preemption";
	public static final String PROP_LP_JOB_REENTER 				= "lp.job.reenter";
	public static final String PROP_QUEUE_MAX_LENGTH 			= "queue.max.length";

	/**
	 * Failed jobs allowed - Yes/No (Does a LP job continues when its HP relication failed, and vice-versa).
	 */
	private boolean _failedJobsAllowed;
	/**
	 * Notify siblings on job start - Yes/No
	 */
	private boolean _notifySiblingsOnJobStart;
	/**
	 * Execute HP job when LP replication already started - Yes/No (depends on #2)
	 */
	private boolean _runHPJobWhenLPStarted;
	/**
	 * Preemption for HP job over LP job in process - Yes/No
	 */
	private boolean _HPJobPreemptionOverLP;
	/**
	 * Kicked out LP job reentring - No/First/Last
	 */
	private ReEnter _LPJobReEnter;
	/**
	 * Length for each queue (H/L) : -1 or null = Infinit
	 */
	private Map<Priority,Integer> _queueMaxLength = new HashMap<Priority, Integer>();
	
	
	public ServerPolicy()
	{
		initDefault();
	}
	
	
	public ServerPolicy(Properties properties)
	{
		initDefault();
		init(properties);
	}

	
	private void initDefault()
	{
		_failedJobsAllowed = false;
		_notifySiblingsOnJobStart = false;
		_runHPJobWhenLPStarted = false;
		_HPJobPreemptionOverLP = true;
		_LPJobReEnter = ReEnter.Last;
		for (Priority p : Priority.values())
		{
			_queueMaxLength.put(p, null);
		}
	}
	
	
	private void init(Properties prop)
	{
		String value;
		value = prop.getProperty(PROP_FAILED_JOBS_ALLOWED);
		if (value != null) _failedJobsAllowed = Boolean.parseBoolean(value);
		value = prop.getProperty(PROP_HP_JOB_PREEMPTION);
		if (value != null) _HPJobPreemptionOverLP = Boolean.parseBoolean(value);
		value = prop.getProperty(PROP_LP_JOB_REENTER);
		if (value != null) _LPJobReEnter = ReEnter.valueOf(value);
		value = prop.getProperty(PROP_NOTIFY_SIBLINGS_JOB_STARTED);
		if (value != null) _notifySiblingsOnJobStart = Boolean.parseBoolean(value);
		value = prop.getProperty(PROP_RUN_HP_ALTHOUGH_LP_STARTED);
		if (value != null) _runHPJobWhenLPStarted = Boolean.parseBoolean(value);
		value = prop.getProperty(PROP_QUEUE_MAX_LENGTH);
		if (value != null) parseQueueMaxLength(value);
	}
	
	
	private void parseQueueMaxLength(String value)
	{
		String[] priorities = value.split(";");
		for (String priority : priorities)
		{
			String[] parts = priority.split(":");
			Priority p = Priority.valueOf(parts[0]);
			Integer  i = Integer.valueOf(parts[1]);
			_queueMaxLength.put(p, i);
		}
	}


	/**
	 * Kicked out LP job reentring - No/First/Last
	 * @return
	 */
	public ReEnter getLPJobReEnter()
	{
		return _LPJobReEnter;
	}
	
	
	/**
	 * Length for each queue (H/L) : -1 or null = Infinit
	 * @param priority - priority for which to get the max length
	 * @return
	 */
	public Integer getQueueMaxLength(Priority priority)
	{
		return _queueMaxLength.get(priority);
	}
	
	
	/**
	 * Failed jobs allowed - Yes/No (Does a LP job continues when its HP relication failed, and vice-versa).
	 * @return
	 */
	public boolean isFailedJobsAllowed()
	{
		return _failedJobsAllowed;
	}
	
	
	/**
	 * Preemption for HP job over LP job in process - Yes/No
	 * @return
	 */
	public boolean isHPJobPreemptionOverLP()
	{
		return _HPJobPreemptionOverLP;
	}
	
	
	/**
	 * Notify siblings on job start - Yes/No
	 * @return
	 */
	public boolean isNotifySiblingsOnJobStart()
	{
		return _notifySiblingsOnJobStart;
	}
	
	
	/**
	 * Execute HP job when LP replication already started - Yes/No (depends on #2)
	 * @return
	 */
	public boolean isRunHPJobWhenLPStarted()
	{
		return _runHPJobWhenLPStarted;
	}
}
