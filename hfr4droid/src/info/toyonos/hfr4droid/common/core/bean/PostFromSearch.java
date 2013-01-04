package info.toyonos.hfr4droid.common.core.bean;

import java.util.Date;

/**
 * <p>Bean modélisant un post issu du résultat d'une recherche</p>
 * 
 * @author ToYonos
 *
 */
public class PostFromSearch extends Post
{
	private static final long serialVersionUID = 1147041097465599146L;

	private String callbackUrl;

	public PostFromSearch(int id, String content, String pseudo, String avatar,	Date date, Date lastEdition, int nbCitations, boolean isMine, boolean isModo, Topic topic)
	{
		super(id, content, pseudo, avatar, date, lastEdition, nbCitations, isMine, isModo, topic);
	}

	public PostFromSearch(long id, String content)
	{
		super(id, content);
	}

	public PostFromSearch(long id)
	{
		super(id);
	}

	public String getCallbackUrl()
	{
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl)
	{
		this.callbackUrl = callbackUrl;
	}
}
