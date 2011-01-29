package ty.tech.prioritizedJobRep.server;

import java.io.Serializable;

import ty.tech.prioritizedJobRep.common.Job;


public class JobNotification implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 191058788248378779L;


	enum Type { Started, Finished, Aborted };
	
	private Type _type;
	private Job _job;
	
	
	public JobNotification(Job job, Type type)
	{
		_job = job;
		_type = type;
	}
	
	
	public Job getJob()
	{
		return _job;
	}
	
	
	public Type getType()
	{
		return _type;
	}
}
