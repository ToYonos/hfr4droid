package info.toyonos.hfr4droid.core.bean;

import java.util.Date;

/**
 * La classe qui matérialise une alerte qualitaÿ
 * 
 * @author ToYonos
 *
 */
public class AlertQualitay
{
	private long alertQualitayId;
	private String name;
	private String initiator;
	private Date date;
	private long[] postIds;

	public AlertQualitay(long alertQualitayId, String name, String initiator, Date date, long[] postIds)
	{
		this.alertQualitayId = alertQualitayId;
		this.name = name;
		this.initiator = initiator;
		this.date = date;
		this.postIds = postIds;
	}

	public long getAlertQualitayId()
	{
		return alertQualitayId;
	}

	public void setAlertQualitayId(long alertQualitayId)
	{
		this.alertQualitayId = alertQualitayId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getInitiator()
	{
		return initiator;
	}

	public void setInitiator(String initiator)
	{
		this.initiator = initiator;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public long[] getPostIds()
	{
		return postIds;
	}

	public void setPostIds(long[] postIds)
	{
		this.postIds = postIds;
	}
}
