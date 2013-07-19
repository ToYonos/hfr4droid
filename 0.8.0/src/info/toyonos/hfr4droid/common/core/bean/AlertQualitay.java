package info.toyonos.hfr4droid.common.core.bean;

import java.text.SimpleDateFormat;
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
	private Long[] postIds;

	public AlertQualitay(long alertQualitayId, String name, String initiator, Date date, Long[] postIds)
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

	public Long[] getPostIds()
	{
		return postIds;
	}

	public void setPostIds(Long[] postIds)
	{
		this.postIds = postIds;
	}

	@Override
	public String toString()
	{
		StringBuilder content = new StringBuilder("");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		if (date != null)
		{
			content.append("[")
			.append(sdf.format(date))
			.append("] ");
		}
		content.append(name);
		if (initiator != null)
		{
			content.append(" (")
			.append(initiator)
			.append(")");
		}
		return content.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		return this != null &&
		o != null &&
		o instanceof AlertQualitay &&
		this.alertQualitayId == ((AlertQualitay) o).getAlertQualitayId();
	}
}
