package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.Utils;
import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.SubCategory;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicStatus;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

/**
 * <p>Version avancée du <code>HFRDataRetriever</code> qui utilise en partie l'api REST du forum</p>
 * 
 * @author ToYonos
 * @see info.toyonos.core.data.HFRDataRetriever
 *
 */
public class HFRJsonDataRetriever extends HFRRawHtmlDataRetriever
{
	// Attention, bien modifier les & en ? dans les urls si url rewritée
	public static final String API_URL					= BASE_URL + "/webservices/rest_api.php?uri=forums/hardwarefr/";
	
	public static final String JSON_SUBCATS_URL			= API_URL + "categories/{$cat}/subcategories/";
	public static final String JSON_ALL_TOPICS_URL		= API_URL + "topics/{$type}/";
	public static final String JSON_TOPICS_CAT_URL		= API_URL + "categories/{$cat}/topics/{$type}/&page={$page}";
	public static final String JSON_TOPICS_SUBCAT_URL	= API_URL + "categories/{$cat}/subcategories/{$subcat}/topics/{$type}/&page={$page}";
	public static final String JSON_POSTS_URL			= API_URL + "categories/{$cat}/topics/{$topic}/posts/&page={$page}";
	
	public static final int POSTS_PER_PAGE = 40;
	
	public HFRJsonDataRetriever(Context context)
	{
		super(context);
	}
	
	public HFRJsonDataRetriever(Context context, boolean clearCache)
	{
		super(context, clearCache);
	}

	public HFRJsonDataRetriever(Context context, HFRAuthentication auth, boolean clearCache)
	{
		super(context, auth, clearCache);
	}


	/**
	 * {@inheritDoc}
	 */
	public List<SubCategory> getSubCats(Category cat) throws DataRetrieverException
	{
		Category keyCat = getCatById(cat.getId());
		if (keyCat != null)
		{
			List<SubCategory> currentSubCats = cats.get(keyCat);
			if (currentSubCats == null)
			{
				currentSubCats = new ArrayList<SubCategory>();
				String content = null;
				try
				{
					String url = SUBCATS_URL.replaceFirst("\\{\\$cat\\}", keyCat.getRealId());
					content = getAsString(url);
					JSONArray jsonSubcats = new JSONObject(content).getJSONObject("resource").getJSONArray("resources");
					for (int i = 0; i < jsonSubcats.length(); i++)
					{
						JSONObject jsonSubCat = jsonSubcats.getJSONObject(i);
						SubCategory subCat = new SubCategory(keyCat, getInt(jsonSubCat, "id"), getString(jsonSubCat, "name"));
						currentSubCats.add(subCat);
					}
				}
				catch (Exception e)
				{
					throw new DataRetrieverException(context.getString(R.string.error_dr_subcats), e);
				}

				cats.put(keyCat, currentSubCats);
				Log.d(HFR4droidApplication.TAG, "New subcats retrieved (from " + keyCat.toString() + "), let's serialize them...");
				serializeCats();
			}
			return currentSubCats;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Topic> getTopics(Category cat, TopicType type, int pageNumber) throws DataRetrieverException
	{
		String url = null;
		if (cat.equals(Category.MPS_CAT))
		{
			return super.getTopics(cat, type, pageNumber);
		}
		else if (cat.equals(Category.ALL_CATS))
		{
			url = JSON_ALL_TOPICS_URL.replaceFirst("\\{\\$type\\}",type.getJsonKey());
		}
		else
		{
			url = JSON_TOPICS_CAT_URL;
			if (cat.getSubCatId() != -1)
			{
				String subCatId = String.valueOf(cat.getSubCatId());
				url = JSON_TOPICS_SUBCAT_URL.replaceFirst("\\{\\$subcat\\}", subCatId);
			}
			url = url.replaceFirst("\\{\\$cat\\}", cat.getRealId())
			.replaceFirst("\\{\\$page\\}", String.valueOf(pageNumber))
			.replaceFirst("\\{\\$type\\}", type.getJsonKey());
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
			if (cat.equals(Category.ALL_CATS))
			{
				JSONArray jsonCats = new JSONObject(content).getJSONObject("resource").getJSONArray("resources");
				for (int i = 0; i < jsonCats.length(); i++)
				{
					JSONObject jsonCat = jsonCats.getJSONObject(i);
					Category currentCat = new Category(
						Integer.parseInt(Utils.getSingleElement("/([0-9]+)/$", getString(jsonCat.getJSONObject("links").getJSONObject("category"), "href"))),
						getString(jsonCat.getJSONObject("links").getJSONObject("category"), "title"));
					JSONArray jsonTopics = jsonCat.getJSONArray("resources");
					processCat(jsonTopics, topics, currentCat);
				}
			}
			else
			{
				JSONArray jsonTopics = new JSONObject(content).getJSONObject("resource").getJSONArray("resources");
				processCat(jsonTopics, topics, cat);
			}
			Log.d(HFR4droidApplication.TAG, "JSON browsing OK, " + topics.size() + " topics retrieved");
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_topics), e);
		}
		
		if (!cat.equals(Category.MPS_CAT)) checkNewMps(content);

		return topics;
	}
	
	private void processCat(JSONArray jsonTopics, List<Topic> topics, Category cat) throws JSONException
	{
		for (int i = 0; i < jsonTopics.length(); i++)
		{
			JSONObject jsonTopic = jsonTopics.getJSONObject(i);
			TopicStatus status = getStatusFromFlag(getInt(jsonTopic, "flag_owntopic"), getBoolean(jsonTopic, "is_read"), getBoolean(jsonTopic, "is_closed"));
			int lastPosition = getInt(jsonTopic, "last_position");
			int lastPage = (int) Math.ceil((float) lastPosition / POSTS_PER_PAGE);
			Topic topic = new Topic(
				getInt(jsonTopic, "id"),
				getString(jsonTopic, "title"),
				getString(jsonTopic.getJSONObject("links").getJSONObject("author"), "title"),
				status,
				status == TopicStatus.NO_NEW_POST || status == TopicStatus.LOCKED || status == TopicStatus.NONE ? -1 : (lastPosition % POSTS_PER_PAGE == 0 ? lastPage + 1 : lastPage),
				getInt(jsonTopic, "last_post_read_id"),
				getInt(jsonTopic.getJSONObject("links").getJSONObject("posts"), "count"),
				(int) Math.ceil((float) getInt(jsonTopic.getJSONObject("links").getJSONObject("posts"), "count") / POSTS_PER_PAGE),
				getBoolean(jsonTopic, "is_sticky"),
				!getBoolean(jsonTopic, "is_read"),
				cat);
			topics.add(topic);
		}		
	}
	
	private TopicStatus getStatusFromFlag(int flag, boolean isRead, boolean isClosed)
	{
		if (isClosed) return TopicStatus.LOCKED;
		if (flag != -1 && isRead) return TopicStatus.NO_NEW_POST;

		switch (flag)
		{
			case 0: // Rouge
				return TopicStatus.NEW_ROUGE;

			case 1: // Cyan
				return TopicStatus.NEW_CYAN;
				
			case 3: // Favori
				return TopicStatus.NEW_FAVORI;
			
			default:
				return TopicStatus.NONE;
		}
		// TODO les MPS ???
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Post> getPosts(Topic topic, int pageNumber) throws DataRetrieverException
	{
		ArrayList<Post> posts = new ArrayList<Post>();
		String url = JSON_POSTS_URL.replaceFirst("\\{\\$cat\\}", topic.getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(topic.getId()))
		.replaceFirst("\\{\\$page\\}", String.valueOf(pageNumber));
		String content = null;
		try
		{
			content = getAsString(url);
			JSONArray jsonPosts = new JSONObject(content).getJSONObject("resource").getJSONArray("resources");
			for (int i = 0; i < jsonPosts.length(); i++)
			{
				JSONObject jsonPost = jsonPosts.getJSONObject(i);
				Post post = new Post(
					getInt(jsonPost, "id"),
					getString(jsonPost, "html_content"),
					getString(jsonPost.getJSONObject("links").getJSONObject("author"), "title"),
					BASE_IMAGE_URL + getString(jsonPost.getJSONObject("links").getJSONObject("author"), "tns3"),
					getDate(jsonPost, "creation_date"),
					null,
					0,
					false,
					getString(jsonPost.getJSONObject("links").getJSONObject("author"), "title").equals("Modération"),
					topic);
				posts.add(post);
			}
			Log.d(HFR4droidApplication.TAG, "JSON browsing OK, " + posts.size() + " posts retrieved");
		}
		catch (Exception e)
		{
			throw new DataRetrieverException(context.getString(R.string.error_dr_posts), e);
		}

        // TODO adapter
		//String nbPages = Utils.getSingleElement("([0-9]+)</(?:a|b)></div><div\\s*class=\"pagepresuiv\"", content);
		//if (nbPages != null) topic.setNbPages(Integer.parseInt(nbPages));

		//hashCheck = Utils.getSingleElement("<input\\s*type=\"hidden\"\\s*name=\"hash_check\"\\s*value=\"(.+?)\" />", content);
		
		// Pour HFRUrlParser, récupération d'informations complémentaires
		// TODO adapter
		if (topic.getName() == null)
		{
			//String topicTitle = getSingleElement("<input\\s*type=\"hidden\"\\s*name=\"sujet\"\\s*value=\"(.+?)\"\\s*/>", content);
			//String topicTitle =  Utils.getSingleElement("(?:&nbsp;)*(.*)", Utils.getSingleElement("([^>]+)(?:</a>)?</h1>", content));
			//if (topicTitle != null) topic.setName(topicTitle);
			//if (Utils.getSingleElement("(repondre\\.gif)", content) == null) topic.setStatus(TopicStatus.LOCKED);
		}

		if (!topic.getCategory().equals(Category.MPS_CAT)) checkNewMps(content);

		return posts;
	}
	
	protected boolean isOnMaintenance(String content)
	{
		// TODO adapter
		return content.matches(MAINTENANCE);
	}
	
	private String getString(JSONObject o, String key)
	{
		try
		{
			return o.getString(key);
		}
		catch (JSONException e)
		{
			return null;
		}
	}
	
	private int getInt(JSONObject o, String key)
	{
		try
		{
			return o.getInt(key);
		}
		catch (JSONException e)
		{
			return -1;
		}
	}
	
	private boolean getBoolean(JSONObject o, String key)
	{
		try
		{
			return o.getBoolean(key);
		}
		catch (JSONException e)
		{
			return false;
		}
	}
	
	private Date getDate(JSONObject o, String key)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.FRANCE);
		try
		{
			return sdf.parse(o.getString(key));
		}
		catch (Exception e)
		{
			return null;
		}
	}
}