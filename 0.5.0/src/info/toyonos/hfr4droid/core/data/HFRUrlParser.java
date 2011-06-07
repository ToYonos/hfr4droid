package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.activity.PostsActivity;
import info.toyonos.hfr4droid.core.bean.BasicElement;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

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
	public boolean parseUrl(String url) throws DataRetrieverException
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
	 * @throws DataRetrieverException 
	 * @see #parseUrl
	 */
	private boolean parseRewrittenUrl(String url) throws DataRetrieverException 
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
				element = new Topic(Long.parseLong(m.group(1)), null);
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
	 * @throws DataRetrieverException
	 * @see #parseUrl
	 */
	private boolean parseStandardUrl(String url) throws DataRetrieverException 
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
			element = getCat(url);
			page = Integer.parseInt(HFRDataRetriever.getSingleElement("(?:&|\\?)page=([0-9]+)", url));
			type = TopicType.fromInt(Integer.parseInt(HFRDataRetriever.getSingleElement("(?:&|\\?)owntopic=([0-9])", url)));
			return true;
		}
		else if (url.matches(BASE_URL_REGEXP + "forum2.*"))
		{
			// C'est un topic
			String numReponse = HFRDataRetriever.getSingleElement("(?:&|\\?)numreponse=([0-9]+)", url);
			if (numReponse != null && !numReponse.equals("0"))
			{
				// Cas spécifique, on récupère la vraie url
				String reelUrl = getReelUrl(url) + "#t" + numReponse;
				return parseUrl(reelUrl);
			}
			else
			{
				Category cat = getCat(url);
				element = new Topic(Long.parseLong(HFRDataRetriever.getSingleElement("(?:&|\\?)post=([0-9]+)", url)), null);
				((Topic) element).setCategory(cat);
				((Topic) element).setLastReadPost(handlePostId(HFRDataRetriever.getSingleElement(POST_REGEXP, url)));
				page = Integer.parseInt(HFRDataRetriever.getSingleElement("(?:&|\\?)page=([0-9]+)", url));
				String intType = HFRDataRetriever.getSingleElement("(?:&|\\?)owntopic=([0-9])", url);
				type = intType != null ? TopicType.fromInt(Integer.parseInt(intType)) : TopicType.ALL;
				return true;
			}
		}	
		return false;
	}
	
	private Category getCat(String url) throws DataRetrieverException
	{
		String catId = HFRDataRetriever.getSingleElement("(?:&|\\?)cat=(prive|[0-9]+)", url);
		try
		{
			return dataRetriever.getCatById(Long.parseLong(catId));
		}
		catch (NumberFormatException e)
		{
			return Category.MPS_CAT;
		}
	}

	private long handlePostId(String content)
	{
		if (content != null)
		{
			return content.equals("bas") ? PostsActivity.BOTTOM_PAGE_ID : Long.parseLong(content);	
		}
		return -1;
	}
	
	private String getReelUrl(String url)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		try
		{
			URI uri = new URI(url);
			HttpHead method = new HttpHead(uri);
			method.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; fr; rv:1.9.2) Gecko/20100101 Firefox/4.0.1");
			client.execute(method);
		}
		catch (ClientProtocolException e)
		{
			if (e.getCause() instanceof CircularRedirectException)
			{
				return HFRDataRetriever.getSingleElement("(http://.*?)'$", ((CircularRedirectException) e.getCause()).getMessage());
			}
		}
		catch (IOException e) {}
		catch (URISyntaxException e){}
		finally
		{
			client.getConnectionManager().shutdown();
		}
		return url;
	}
}