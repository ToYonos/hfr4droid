package info.toyonos.hfr4droid.lib.core.bean;

import java.util.Date;

/**
 * <p>Bean modélisant un post</p>
 * 
 * @author ToYonos
 *
 */
public class Post extends BasicElement
{
	private static final long serialVersionUID = -7121499010061075532L;

	private String pseudo;
	private String avatarUrl;
	private Date date;
	private Date lastEdition;
	private int nbCitations;
	private Topic topic;
	private boolean isMine;
	private boolean isModo;

	public Post(long id)
	{
		super(id);
		this.pseudo = null;
		this.avatarUrl = null;
		this.date = null;
		this.lastEdition = null;
		this.nbCitations = 0;
		this.isMine = false;
		this.isModo = false;
		this.topic = null;
	}

	public Post(long id, String content)
	{
		this(id);
		this.name = content;
	}

	public Post(int id, String content, String pseudo, String avatar, Date date, Date lastEdition, int nbCitations, boolean isMine, boolean isModo, Topic topic)
	{
		super(id, content);
		this.pseudo = pseudo;
		this.avatarUrl = avatar;
		this.date = date;
		this.lastEdition = lastEdition;
		this.nbCitations = nbCitations;
		this.isMine = isMine;
		this.isModo = isModo;
		this.topic = topic;
	}

	public String getContent()
	{
		return name;
	}

	public void setContent(String content)
	{
		this.name = content;
	}

	public String getPseudo()
	{
		return pseudo;
	}

	public void setPseudo(String pseudo)
	{
		this.pseudo = pseudo;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getAvatarUrl()
	{
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl)
	{
		this.avatarUrl = avatarUrl;
	}

	public Date getLastEdition()
	{
		return lastEdition;
	}

	public void setLastEdition(Date lastEdition)
	{
		this.lastEdition = lastEdition;
	}

	public int getNbCitations()
	{
		return nbCitations;
	}

	public void setNbCitations(int nbCitations)
	{
		this.nbCitations = nbCitations;
	}

	public Topic getTopic()
	{
		return topic;
	}

	public void setTopic(Topic topic)
	{
		this.topic = topic;
	}

	public boolean isMine()
	{
		return isMine;
	}

	public void setMine(boolean isMine)
	{
		this.isMine = isMine;
	}
	
	public boolean isModo()
	{
		return isModo;
	}

	public void setModo(boolean isModo)
	{
		this.isModo = isModo;
	}
}
