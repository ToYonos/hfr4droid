package info.toyonos.hfr4droid.core.bean;



/**
 * La classe qui g�re les th�mes de l'application
 * 
 * @author ToYonos
 *
 */
public class Theme
{
	private String key;
	
	private int listBackgroundColor;
	private int listDividerColor;
	private String postHeaderData;
	private int postPseudoColor;
	private int postDateColor;
	private int postTextColor;
	private int postLinkColor;
	private int postEditQuoteBackgroundColor;
	private int postEditQuoteTextColor;
	private int postBlockBackgroundColor;
	private int modoPostBackgroundColor;

	public Theme(String key)
	{
		this.key = key;
		this.listBackgroundColor = -1;
		this.listDividerColor = -1;
		this.postHeaderData = null;
		this.postPseudoColor = -1;
		this.postDateColor = -1;
		this.postTextColor = -1;
		this.postLinkColor = -1;
		this.postEditQuoteBackgroundColor = -1;
		this.postEditQuoteTextColor = -1;
		this.postBlockBackgroundColor = -1;
		this.modoPostBackgroundColor = -1;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public void setListBackgroundColor(int listBackgroundColor)
	{
		this.listBackgroundColor = listBackgroundColor;
	}

	public void setListDividerColor(int listDividerColor)
	{
		this.listDividerColor = listDividerColor;
	}

	public void setPostHeaderData(String postHeaderData)
	{
		this.postHeaderData = postHeaderData;
	}

	public void setPostPseudoColor(int postPseudoColor)
	{
		this.postPseudoColor = postPseudoColor;
	}

	public void setPostDateColor(int postDateColor)
	{
		this.postDateColor = postDateColor;
	}

	public void setPostTextColor(int postTextColor)
	{
		this.postTextColor = postTextColor;
	}

	public void setPostLinkColor(int postLinkColor)
	{
		this.postLinkColor = postLinkColor;
	}

	public void setPostEditQuoteBackgroundColor(int postEditQuoteBackgroundColor)
	{
		this.postEditQuoteBackgroundColor = postEditQuoteBackgroundColor;
	}

	public void setPostEditQuoteTextColor(int postEditQuoteTextColor)
	{
		this.postEditQuoteTextColor = postEditQuoteTextColor;
	}

	public void setPostBlockBackgroundColor(int postBlockBackgroundColor)
	{
		this.postBlockBackgroundColor = postBlockBackgroundColor;
	}

	public void setModoPostBackgroundColor(int modoPostBackgroundColor)
	{
		this.modoPostBackgroundColor = modoPostBackgroundColor;
	}

	public int getListBackgroundColor()
	{
		return listBackgroundColor;
	}

	public int getListDividerColor()
	{
		return listDividerColor;
	}

	public String getPostHeaderData()
	{
		return postHeaderData;
	}

	public int getPostPseudoColor()
	{
		return postPseudoColor;
	}

	public int getPostDateColor()
	{
		return postDateColor;
	}

	public int getPostTextColor()
	{
		return postTextColor;
	}

	public int getPostLinkColor()
	{
		return postLinkColor;
	}

	public int getPostEditQuoteBackgroundColor()
	{
		return postEditQuoteBackgroundColor;
	}

	public int getPostEditQuoteTextColor()
	{
		return postEditQuoteTextColor;
	}

	public int getPostBlockBackgroundColor()
	{
		return postBlockBackgroundColor;
	}

	public int getModoPostBackgroundColor()
	{
		return modoPostBackgroundColor;
	}
	
	public String getListBackgroundColorAsString()
	{
		return "#" + Integer.toHexString(listBackgroundColor).substring(2);
	}

	public String getListDividerColorAsString()
	{
		return "#" + Integer.toHexString(listDividerColor).substring(2);
	}

	public String getPostPseudoColorAsString()
	{
		return "#" + Integer.toHexString(postPseudoColor).substring(2);
	}

	public String getPostDateColorAsString()
	{
		return "#" + Integer.toHexString(postDateColor).substring(2);
	}

	public String getPostTextColorAsString()
	{
		return "#" + Integer.toHexString(postTextColor).substring(2);
	}

	public String getPostLinkColorAsString()
	{
		return "#" + Integer.toHexString(postLinkColor).substring(2);
	}

	public String getPostEditQuoteBackgroundColorAsString()
	{
		return "#" + Integer.toHexString(postEditQuoteBackgroundColor).substring(2);
	}

	public String getPostEditQuoteTextColorAsString()
	{
		return "#" + Integer.toHexString(postEditQuoteTextColor).substring(2);
	}

	public String getPostBlockBackgroundColorAsString()
	{
		return "#" + Integer.toHexString(postBlockBackgroundColor).substring(2);
	}

	public String getModoPostBackgroundColorAsString()
	{
		return "#" + Integer.toHexString(modoPostBackgroundColor).substring(2);
	}
}
