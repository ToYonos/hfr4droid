package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.SubCategory;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicStatus;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.text.Html;
import android.util.Log;

/**
 * <p>Implémentation pour le forum de Hardware.fr du <code>MDDataRetriever</code></p>
 * 
 * @author ToYonos
 * @see info.toyonos.core.data.MDDataRetriever
 *
 */
public class HFRDataRetriever implements MDDataRetriever
{
	public static final String BASE_URL			= "http://forum.hardware.fr";
	public static final String CATS_URL			= BASE_URL + "/";
	public static final String SUBCATS_URL		= BASE_URL + "/message.php?&config=hfr.inc&cat={$cat}";
	public static final String TOPICS_URL		= BASE_URL + "/forum1.php?config=hfr.inc&cat={$cat}&subcat={$subcat}&page={$page}&owntopic={$type}";
	public static final String ALL_TOPICS_URL	= BASE_URL + "/forum1f.php?config=hfr.inc&owntopic={$type}";
	public static final String POSTS_URL		= BASE_URL + "/forum2.php?config=hfr.inc&cat={$cat}&post={$topic}&page={$page}";
	public static final String SMILIES_URL		= BASE_URL + "/message-smi-mp-aj.php?config=hfr.inc&findsmilies={$tag}";
	public static final String QUOTE_URL		= BASE_URL + "/message.php?config=hfr.inc&cat={$cat}&post={$topic}&numrep={$post}";
	public static final String EDIT_URL			= BASE_URL + "/message.php?config=hfr.inc&cat={$cat}&post={$topic}&numreponse={$post}";
	public static final String KEYWORDS_URL		= BASE_URL + "/wikismilies.php?config=hfr.inc&detail={$code}";

	public static final String MAINTENANCE 		= "Serveur en cours de maintenance. <br /><br />Veuillez nous excuser pour la gène occasionnée";
	
	private Context context;
	private HFRAuthentication auth;
	private String hashCheck;
	private Map<Category, List<SubCategory>> cats;

	public HFRDataRetriever(Context context)
	{
		this.context = context;
		hashCheck = null;
		cats = null;
	}

	public HFRDataRetriever(Context context, HFRAuthentication auth)
	{
		this.context = context;
		this.auth = auth;
		hashCheck = null;
		cats = null; // TODO cache disque
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHashCheck() throws DataRetrieverException
	{
		if (hashCheck == null)
		{
			// Dans ce cas on va le lire sur la page principale du forum (la page des cats)
			String content = null;
			try
			{
				content = getAsString(CATS_URL);
			}
			catch (Exception e)
			{
				throw new DataRetrieverException(context.getString(R.string.error_dr_hash_check), e);
			}
			hashCheck = getSingleElement("<input\\s*type=\"hidden\"\\s*name=\"hash_check\"\\s*value=\"(.+?)\" />", content);
		}
		return hashCheck;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBaseUrl()
	{
		return BASE_URL;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Category> getCats() throws DataRetrieverException
	{
		ArrayList<Category> cats = new ArrayList<Category>();
		String content = null;
		try
		{
			content = getAsString(CATS_URL);
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_cats), e);
		}
		Pattern p;
		Matcher m;
		
		if (this.cats == null)
		{
			this.cats = new HashMap<Category, List<SubCategory>>();
			p = Pattern.compile("<tr.*?id=\"cat([0-9]+)\".*?" +
								"<td.*?class=\"catCase1\".*?<b><a\\s*href=\"/hfr/([a-zA-Z0-9-]+)/.*?\"\\s*class=\"cCatTopic\">(.+?)</a></b>.*?" +
								"</tr>"
								, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			m = p.matcher(content);
			while (m.find())
			{
				Category newCat = new Category(Integer.parseInt(m.group(1)), m.group(2), m.group(3));
				this.cats.put(newCat, null);
			}
		}
		cats.addAll(this.cats.keySet());

		Category mpCat = new Category(Category.MPS_CAT);		
		String mpCatTitle = getSingleElement("<b><a\\s*class=\"cCatTopic red\"\\s*href=\".*?\">(Vous avez [0-9]+ nouveaux? messages? privés?)</a></b>", content);
		if (mpCatTitle != null) mpCat.setName(mpCatTitle);

		// Cat des messages privés
		cats.add(0, mpCat);

		// Cat représentant "toutes les cats"
		cats.add(1, Category.ALL_CATS);

		// Cat des modals
		p = Pattern.compile("<a\\s*class=\"cCatTopic\"\\s*href=\"/forum1\\.php\\?config=hfr\\.inc&amp;cat=0&amp;"
							, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		m = p.matcher(content);
		if  (m.find())
		{
			cats.add(1, Category.MODO_CAT);
		}

		return cats;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Category getCatByCode(String code) throws DataRetrieverException
	{
		if (code == null) return null;
		
		if (cats == null) getCats();
		for (Category cat : cats.keySet())
		{
			if (code.equals(cat.getCode())) return cat;
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Category getCatById(long id) throws DataRetrieverException
	{		
		if (cats == null) getCats();
		for (Category cat : cats.keySet())
		{
			if (id == cat.getId()) return cat;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SubCategory> getSubCats(Category cat) throws DataRetrieverException
	{
		if (cats != null && cats.containsKey(cat))
		{
			List<SubCategory> currentSubCats = cats.get(cat);
			if (currentSubCats == null)
			{
				currentSubCats = new ArrayList<SubCategory>();
				String content = null;
				try
				{
					String url = SUBCATS_URL.replaceFirst("\\{\\$cat\\}", cat.getRealId());
					content = getAsString(url);
				}
				catch (Exception e)
				{
					throw new DataRetrieverException(context.getString(R.string.error_dr_subcats), e);
				}

				Pattern p = Pattern.compile("<option\\s*value=\"([0-9]+)\"\\s*>(.+?)</option>"
						, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher m = p.matcher(content);
				while (m.find())
				{
					SubCategory subCat = new SubCategory(cat, Integer.parseInt(m.group(1)), m.group(2));
					currentSubCats.add(subCat);
				}
				cats.put(cat, currentSubCats);
			}
			return currentSubCats;
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Topic> getTopics(Category cat, TopicType type) throws DataRetrieverException
	{
		return getTopics(cat, type, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Topic> getTopics(Category cat, TopicType type, int pageNumber) throws DataRetrieverException
	{
		String url = null;
		if (cat.equals(Category.ALL_CATS))
		{
			url = ALL_TOPICS_URL.replaceFirst("\\{\\$type\\}", String.valueOf(type.getValue()));
		}
		else
		{
			String subCatId = cat.getSubCatId() != -1 ? String.valueOf(cat.getSubCatId()) : "";
			url = TOPICS_URL.replaceFirst("\\{\\$cat\\}", cat.getRealId())
			.replaceFirst("\\{\\$subcat\\}", subCatId)
			.replaceFirst("\\{\\$page\\}", String.valueOf(pageNumber))
			.replaceFirst("\\{\\$type\\}", String.valueOf(type.getValue()));
		}
		return innerGetTopics(cat, url);
	}

	private List<Topic> innerGetTopics(Category cat, String url) throws DataRetrieverException
	{
		ArrayList<Topic> topics = new ArrayList<Topic>();
		String content = null;
		try
		{
			content = getAsString(url);
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_topics), e);
		}

		Pattern p = Pattern.compile("(?:(?:<th\\s*class=\"padding\".*?<a\\s*href=\"/forum1\\.php\\?config=hfr\\.inc&amp;cat=([0-9]+).*?\"\\s*class=\"cHeader\">(.*?)</a></th>)" +
									"|(?:<tr\\s*class=\"sujet\\s*ligne_booleen\\s*cBackCouleurTab[0-9]\\s*(ligne_sticky)?.*?" +
									"<td.*?class=\"sujetCase1\\s*cBackCouleurTab[0-9]\\s*\".*?><img\\s*src=\".*?([A-Za-z0-9]+)\\.gif\".*?" +
									"<td.*?class=\"sujetCase3\".*?>(<span\\s*class=\"red\"\\s*title=\".*?\">\\[non lu\\]</span>\\s*)?(?:<img\\s*src=\".*?flechesticky\\.gif\".*?/>\\s*)?(?:&nbsp;)?(?:<img\\s*src=\".*?(lock)\\.gif\".*?/>\\s*)?<a.*?class=\"cCatTopic\"\\s*title=\"Sujet n°([0-9]+)\">(.+?)</a></td>.*?" +
									"<td.*?class=\"sujetCase4\".*?(?:(?:<a.*?class=\"cCatTopic\">(.+?)</a>)|&nbsp;)</td>.*?" +
									"<td.*?class=\"sujetCase5\".*?(?:(?:<a\\s*href=\".*?#t([0-9]+)\"><img.*?src=\".*?([A-Za-z0-9]+)\\.gif\"\\s*title=\".*?\\(p\\.([0-9]+)\\)\".*?/></a>)|&nbsp;)</td>.*?" +
									"<td.*?class=\"sujetCase6\\s*cBackCouleurTab[0-9]\\s*\".*?>(?:<a\\s*rel=\"nofollow\"\\s*href=\"/profilebdd.*?>)?(.+?)(?:</a>)?</td>.*?" +
									"<td.*?class=\"sujetCase7\".*?>(.+?)</td>.*?" +
									"</tr>))"
									, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(content);
		Category currentCat = cat;
		while (m.find())
		{
			/*System.out.println("--- NOUVEAU GROUPE ---");
			for(int i= 1; i<= m.groupCount(); ++i)
				System.out.println("groupe "+i+" :"+m.group(i));
			System.out.println("\n");*/

			if (m.group(1) != null)
			{
				// C'est une cat
				currentCat = new Category(Integer.parseInt(m.group(1)), m.group(2));
			}
			else
			{
				TopicStatus status = getStatusFromImgName(m.group(6) != null ? m.group(6) : (m.group(11) != null ? m.group(11) : m.group(4)));
				int nbPages = m.group(9) != null ? Integer.parseInt(m.group(9)) : 1;
				int lastReadPage = status == TopicStatus.NEW_MP ? nbPages : (m.group(12) != null ? Integer.parseInt(m.group(12)) : -1);
				topics.add(new Topic(Integer.parseInt(m.group(7)),
									m.group(8),
									m.group(13),
									status,
									lastReadPage,
									m.group(10) != null ? Long.parseLong(m.group(10)) : -1,
									Integer.parseInt(m.group(14)),
									nbPages,
									m.group(3) != null,
									m.group(5) != null,
									currentCat
									)
				);
			}
		}

		return topics;
	}

	private TopicStatus getStatusFromImgName(String imgName)
	{
		if (imgName == null)
		{
			return TopicStatus.NONE;
		}
		else if (imgName.equals("flag1"))
		{
			return TopicStatus.NEW_CYAN;
		}
		else if (imgName.equals("flag0"))
		{
			return TopicStatus.NEW_ROUGE;
		}
		else if (imgName.equals("favoris"))
		{
			return TopicStatus.NEW_FAVORI;
		}
		else if (imgName.equals("closed"))
		{
			return TopicStatus.NO_NEW_POST;
		}
		else if (imgName.equals("closedbp"))
		{
			return TopicStatus.NEW_MP;
		}
		else if (imgName.equals("closedp"))
		{
			return TopicStatus.NO_NEW_MP;
		}
		else if (imgName.equals("lock"))
		{
			return TopicStatus.LOCKED;
		}		
		else
		{
			return TopicStatus.NONE;	
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Post> getPosts(Topic topic, int pageNumber) throws DataRetrieverException
	{
		ArrayList<Post> posts = new ArrayList<Post>();
		String url = POSTS_URL.replaceFirst("\\{\\$cat\\}", topic.getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(topic.getId()))
		.replaceFirst("\\{\\$page\\}", String.valueOf(pageNumber));
		String content = null;
		try
		{
			content = getAsString(url);
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_posts), e);
		}

		Pattern p = Pattern.compile("(<tr.*?class=\"message\\s*(?:cBackCouleurTab[0-9])?(caseModoGeneric)?\".*?" +
									"<a.*?href=\"#t([0-9]+)\".*?" +
									"<b.*?class=\"s2\">(?:<a.*?>)?(.*?)(?:</a>)?</b>.*?" +
									"(?:(?:<div\\s*class=\"avatar_center\".*?><img src=\"(.*?)\"\\s*alt=\".*?\"\\s*/></div>)|</td>).*?" +
									"<div.*?class=\"left\">Posté le ([0-9]+)-([0-9]+)-([0-9]+).*?([0-9]+):([0-9]+):([0-9]+).*?" +
									"<div.*?id=\"para[0-9]+\">(.*?)<div style=\"clear: both;\">\\s*</div></p>" +
									"(?:<div\\s*class=\"edited\">)?(?:<a.*?>Message cité ([0-9]+) fois</a>)?(?:<br\\s*/>Message édité par .*? le ([0-9]+)-([0-9]+)-([0-9]+).*?([0-9]+):([0-9]+):([0-9]+)</div>)?.*?" +
									"</div></td></tr></table>)"
									, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(content);
		while (m.find())
		{
			Matcher m2 = Pattern.compile("edit\\-in\\.gif").matcher(m.group(1));
			boolean isMine = m2.find();
			boolean isModo = m.group(2) != null;
			String postContent = m.group(12);
			posts.add(new Post(Integer.parseInt(m.group(3)),
								postContent,
								m.group(4),
								m.group(5),
								new GregorianCalendar(Integer.parseInt(m.group(8)), // Year
										Integer.parseInt(m.group(7)) - 1, // Month
										Integer.parseInt(m.group(6)), // Day
										Integer.parseInt(m.group(9)), // Hour
										Integer.parseInt(m.group(10)), // Minute
										Integer.parseInt(m.group(11))  // Second
								).getTime(),
								m.group(14) != null ? new GregorianCalendar(Integer.parseInt(m.group(16)), // Year
										Integer.parseInt(m.group(15)) - 1, // Month
										Integer.parseInt(m.group(14)), // Day
										Integer.parseInt(m.group(17)), // Hour
										Integer.parseInt(m.group(18)), // Minute
										Integer.parseInt(m.group(19))  // Second
								).getTime() : null,
								m.group(13) != null ? Integer.parseInt(m.group(13)) : 0,
								isMine,
								isModo,
								topic
								)
			);
		}

		String nbPages = getSingleElement("([0-9]+)</(?:a|b)></div><div\\s*class=\"pagepresuiv\"", content);
		if (nbPages != null) topic.setNbPages(Integer.parseInt(nbPages));

		hashCheck = getSingleElement("<input\\s*type=\"hidden\"\\s*name=\"hash_check\"\\s*value=\"(.+?)\" />", content);

		String subCat = getSingleElement("<input\\s*type=\"hidden\"\\s*name=\"subcat\"\\s*value=\"([0-9]+)\"\\s*/>", content);
		if (subCat != null) topic.setSubcat(Integer.parseInt(subCat));
		
		// Pour HFRUrlParser, récupération d'informations complémentaires
		if (topic.getName() == null)
		{
			//String topicTitle = getSingleElement("<input\\s*type=\"hidden\"\\s*name=\"sujet\"\\s*value=\"(.+?)\"\\s*/>", content);
			String topicTitle =  HFRDataRetriever.getSingleElement("(?:&nbsp;)*(.*)", HFRDataRetriever.getSingleElement("([^>]+)(?:</a>)?</h1>", content));
			if (topicTitle != null) topic.setName(topicTitle);
			topic.setStatus(getSingleElement("(repondre\\.gif)", content) != null ? TopicStatus.NONE : TopicStatus.LOCKED);
		}

		return posts;
	}

	/**
	 * {@inheritDoc}
	 */
	public int countNewMps(Topic topic) throws DataRetrieverException
	{
		String url = TOPICS_URL.replaceFirst("\\{\\$cat\\}", "prive")
		.replaceFirst("\\{\\$page\\}", "1")
		.replaceFirst("\\{\\$type\\}", String.valueOf(TopicType.ALL.getValue()));
		String content = null;
		try
		{
			content = getAsString(url);
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_mps), e);
		}

		Pattern p = Pattern.compile("closedbp\\.gif\".*?" +
									"<td.*?class=\"sujetCase3\".*?<a.*?class=\"cCatTopic\"\\s*title=\"Sujet n°([0-9]+)\">(.+?)</a></td>.*?" +
									"<td.*?class=\"sujetCase4\".*?(?:(?:<a.*?class=\"cCatTopic\">(.+?)</a>)|&nbsp;)</td>.*?" +
									"<td.*?class=\"sujetCase7\".*?>(.+?)</td>.*?" +
									"</tr>"
									, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(content);
		int count = 0;
		while (m.find())
		{
			if (count++ == 0 && topic != null)
			{
				topic.setId(Long.parseLong(m.group(1)));
				topic.setName(m.group(2));
				topic.setStatus(TopicStatus.NEW_MP);
				topic.setNbPages(m.group(3) != null ? Integer.parseInt(m.group(3)) : 1);
				topic.setCategory(Category.MPS_CAT);		
			}
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSmiliesByTag(String tag) throws DataRetrieverException
	{
		String encodedTag = tag;
		try
		{
			encodedTag = URLEncoder.encode(tag, "UTF-8");
		}
		catch (UnsupportedEncodingException e1)
		{
			Log.w(HFR4droidApplication.TAG, e1);
		}
		
		String url = SMILIES_URL.replaceFirst("\\{\\$tag\\}",  encodedTag);
		try
		{
			return getAsString(url);
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_smilies), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getQuote(Post post) throws DataRetrieverException
	{
		String url = QUOTE_URL.replaceFirst("\\{\\$cat\\}", post.getTopic().getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(post.getTopic().getId()))
		.replaceFirst("\\{\\$post\\}", String.valueOf(post.getId()));
		return innerGetBBCode(url);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPostContent(Post post) throws DataRetrieverException
	{
		String url = EDIT_URL.replaceFirst("\\{\\$cat\\}", post.getTopic().getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(post.getTopic().getId()))
		.replaceFirst("\\{\\$post\\}", String.valueOf(post.getId()));
		return innerGetBBCode(url);
	}

	private String innerGetBBCode(String url) throws DataRetrieverException
	{
		StringBuilder result = new StringBuilder("");
		String content = null;
		try
		{
			content = getAsString(url, true);
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_bbcode), e);
		}
		
		String BBCode = getSingleElement("<textarea.*?name=\"content_form\".*?>(.*?)</textarea>", content);
		if (BBCode != null)
		{
			for (String line : BBCode.split("\n"))
			{
				result.append(Html.fromHtml(line));
				result.append("\n");
			}
		}
		return result.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getKeywords(String code) throws DataRetrieverException
	{
		String encodedCode = code;
		try
		{
			encodedCode = URLEncoder.encode(code, "UTF-8");
		}
		catch (UnsupportedEncodingException e1)
		{
			Log.w(HFR4droidApplication.TAG, e1);
		}

		String url = KEYWORDS_URL.replaceFirst("\\{\\$code\\}", encodedCode);
		String content = null;
		try
		{
			content = getAsString(url, true);
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_keywords), e);
		}
		
		String keywords = getSingleElement("name=\"keywords0\"\\s*value=\"(.*?)\"\\s*onkeyup", content);
		return keywords;
	}

	/**
	 * Effectue une requête HTTP GET et récupère un flux en retour
	 * @param url L'url concernée
	 * @return Un <code>InputStream</code> contenant le résultat
	 * @throws IOException Si un problème intervient durant la requête
	 * @throws URISyntaxException Si l'url est foireuse
	 * @throws ServerMaintenanceException Si le forum est en maintenance
	 */
	private String getAsString(String url) throws IOException, URISyntaxException, ServerMaintenanceException
	{
		return getAsString(url, false); 
	}

	/**
	 * Effectue une requête HTTP GET et récupère un flux en retour
	 * @param url L'url concernée
	 * @param cr Conserver les retours charriot
	 * @return Un <code>InputStream</code> contenant le résultat
	 * @throws IOException Si un problème intervient durant la requête
	 * @throws URISyntaxException Si l'url est foireuse
	 * @throws ServerMaintenanceException Si le forum est en maintenance
	 */
	private String getAsString(String url, boolean cr) throws IOException, URISyntaxException, ServerMaintenanceException
	{
		DefaultHttpClient client = new DefaultHttpClient();
		InputStream data = null;
		URI uri = new URI(url);
		HttpGet method = new HttpGet(uri);
		HttpContext httpContext = new BasicHttpContext();
		if (auth != null && auth.getCookies() != null)
		{
			httpContext.setAttribute(ClientContext.COOKIE_STORE, auth.getCookies());
		}

		/* Proxy de merde */		
		//HttpHost proxy = new HttpHost("192.168.3.108", 8080);
		//client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		/* -------------- */

		HttpResponse response = client.execute(method, httpContext);
		HttpEntity entity = response.getEntity();
		String content = "";
		if (entity != null)
		{
			try
			{
				data = entity.getContent();
				content = streamToString(data, cr);
			}
			catch (IOException e)
			{
				throw e;
			}
			catch (RuntimeException e)
			{
				method.abort();
				throw e;
			}
			finally
			{
				if (entity != null) entity.consumeContent();
				client.getConnectionManager().shutdown();	
			}
		}

		if  (content.matches(MAINTENANCE)) throw new ServerMaintenanceException(context.getString(R.string.server_maintenance));
		return content;
	}

	/**
	 * Convertit un <code>InputStream</code> en <code>String</code>
	 * @param is Le flux d'entrée
	 * @param cr Conserver les retours charriot
	 * @return La chaine ainsi obtenu
	 * @throws IOException Si un problème d'entrée/sortie intervient
	 */
	public static String streamToString(InputStream is, boolean cr) throws IOException
	{
		if (is != null)
		{
			StringBuilder sb = new StringBuilder();
			String line;
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null)
				{
					sb.append(line);
					if (cr) sb.append("\n");
				}
			}
			finally
			{
				is.close();
			}
			return sb.toString();
		}
		else
		{        
			return "";
		}
	}
	
	/**
	 * Renvoie le premier match ou le second si le premier est null, du premier groupe trouvé dans une chaine donnée.
	 * @param pattern La regexp à appliquer
	 * @param content Le contenu à analyser
	 * @return La chaine trouvée, null sinon
	 */
	public static String getSingleElement(String pattern, String content)
	{
		Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(content);
		return m.find() ? (m.group(1) != null ? m.group(1) : m.group(2)) : null;
	}
}