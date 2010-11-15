package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.activity.PostsActivity;
import info.toyonos.hfr4droid.core.bean.BasicElement;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Implémentation pour le forum de Hardware.fr du <code>MDUrlParser</code></p>
 * 
 * @author ToYonos
 * @see info.toyonos.core.data.MDUrlParser
 *
 */
public class HFRUrlParser implements MDUrlParser
{
	public static final String BASE_URL_REGEXP	= "http://forum\\.hardware\\.fr/";
	public static final String POST_REGEXP		= "(?:(?:#t?([0-9]+))|(?:#(bas)))?$";
	
	private MDDataRetriever dataRetriever;
	
	private BasicElement element;
	private int page;
	private TopicType type;

	public HFRUrlParser(MDDataRetriever dataRetriever)
	{
		this.dataRetriever = dataRetriever;
		element = null;
		page = -1;
		type = TopicType.ALL;
	}

	/**
	 * {@inheritDoc}
	 */
	public BasicElement getElement()
	{
		return element;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPage()
	{
		return page;
	}

	/**
	 * {@inheritDoc}
	 */
	public TopicType getType()
	{
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean parseUrl(String url) throws Exception
	{
		element = null;
		page = -1;
		type = TopicType.ALL;

		if (url.matches(BASE_URL_REGEXP))
		{
			return true;
		}
		else if (url.matches(BASE_URL_REGEXP + "hfr/.*"))
		{
			return parseRewrittenUrl(url);
		}
		else if (url.matches(BASE_URL_REGEXP + "forum.*"))
		{
			return parseStandardUrl(url);
		}
		return false;
	}

	/**
	 * Parse une url rewrittée
	 * @see #parseUrl
	 */
	private boolean parseRewrittenUrl(String url) throws Exception
	{
		if (url.matches(BASE_URL_REGEXP + "hfr/.*?/liste_sujet.*?"))
		{
			// C'est une liste de topics
			Pattern p = Pattern.compile("liste_sujet\\-([0-9]+)\\.htm"
										, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			String[] urlElements = url.split("/");
			Matcher m = p.matcher(urlElements[urlElements.length - 1]);
			if (m.find())
			{
				element = dataRetriever.getCatByCode(urlElements[4]);
				page = Integer.parseInt(m.group(1));
				return true;
			}			
		}
		else
		{
			// C'est un topic
			Pattern p = Pattern.compile("_([0-9]+)_([0-9]+)\\.htm" + POST_REGEXP
										, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			String[] urlElements = url.split("/");
			Matcher m = p.matcher(urlElements[urlElements.length - 1]);
			if (m.find())
			{
				Category cat = dataRetriever.getCatByCode(urlElements[4]);
				element = new Topic(Long.parseLong(m.group(1)));
				((Topic) element).setCategory(cat);
				((Topic) element).setLastReadPost(handlePostId(m.group(3) != null ? m.group(3) : m.group(4)));
				page = Integer.parseInt(m.group(2));
				return true;
			}			
		}
		return false;
	}

	/**
	 * Parse une url standard
	 * @see #parseUrl
	 */
	private boolean parseStandardUrl(String url) throws Exception
	{	
		if (url.matches(BASE_URL_REGEXP + "forum1f.*"))
		{
			// C'est une liste de topics toutes cats confondues
			element = Category.ALL_CATS;
			type = TopicType.fromInt(Integer.parseInt(HFRDataRetriever.getSingleElement("(?:&|\\?)owntopic=([0-9])", url)));
			return true;		
		}
		else if (url.matches("http://forum\\.hardware\\.fr/forum1.*"))
		{
			// C'est une liste de topics pour une cat
			element = dataRetriever.getCatById(Long.parseLong(HFRDataRetriever.getSingleElement("(?:&|\\?)cat=([0-9]+)", url)));
			page = Integer.parseInt(HFRDataRetriever.getSingleElement("(?:&|\\?)page=([0-9]+)", url));
			type = TopicType.fromInt(Integer.parseInt(HFRDataRetriever.getSingleElement("(?:&|\\?)owntopic=([0-9])", url)));
			return true;
		}
		else if (url.matches(BASE_URL_REGEXP + "forum2.*"))
		{
			// C'est un topic
			Category cat = dataRetriever.getCatById(Long.parseLong(HFRDataRetriever.getSingleElement("(?:&|\\?)cat=([0-9]+)", url)));
			element = new Topic(Long.parseLong(HFRDataRetriever.getSingleElement("(?:&|\\?)post=([0-9]+)", url)));
			((Topic) element).setCategory(cat);
			((Topic) element).setLastReadPost(handlePostId(HFRDataRetriever.getSingleElement(POST_REGEXP, url)));
			page = Integer.parseInt(HFRDataRetriever.getSingleElement("(?:&|\\?)page=([0-9]+)", url));
			String intType = HFRDataRetriever.getSingleElement("(?:&|\\?)owntopic=([0-9])", url);
			type = intType != null ? TopicType.fromInt(Integer.parseInt(intType)) : TopicType.ALL;
			return true;
		}	
		return false;
	}

	private long handlePostId(String content)
	{
		if (content != null)
		{
			return content.equals("bas") ? PostsActivity.BOTTOM_PAGE_ID : Long.parseLong(content);	
		}
		return -1;
	}
}