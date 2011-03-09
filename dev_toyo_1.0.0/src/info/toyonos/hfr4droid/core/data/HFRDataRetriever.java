package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.Utils;
import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.SubCategory;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.util.Log;

/**
 * <p>Implémentation abstraite pour le forum de Hardware.fr du <code>MDDataRetriever</code></p>
 * 
 * @author ToYonos
 * @see info.toyonos.core.data.MDDataRetriever
 *
 */
public abstract class HFRDataRetriever implements MDDataRetriever
{
	private static final String CATS_CACHE_FILE_NAME = "hfr4droid_cats.dat";
	
	public static final String BASE_URL 		= "http://forum.hardware.fr";
	public static final String BASE_IMAGE_URL	= "http://forum-images.hardware.fr/images/";
	
	protected Context context;
	private HFRAuthentication auth;
	protected Map<Category, List<SubCategory>> cats;

	public HFRDataRetriever(Context context)
	{
		this(context, null, false);
	}
	
	public HFRDataRetriever(Context context, boolean clearCache)
	{
		this(context, null, clearCache);
	}

	public HFRDataRetriever(Context context, HFRAuthentication auth, boolean clearCache)
	{
		this.context = context;
		this.auth = auth;
		cats = null;
		if (clearCache) clearCache();
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
	public SubCategory getSubCatById(Category cat, long id) throws DataRetrieverException
	{		
		if (cats == null) throw new DataRetrieverException(context.getString(R.string.no_cats_cache));

		Category keyCat = getCatById(cat.getId());
		if (cats.get(keyCat) == null) throw new DataRetrieverException(context.getString(R.string.no_subcat_cache, keyCat.toString()));
		for (SubCategory subCat : cats.get(keyCat))
		{
			if (subCat.getSubCatId() == id) return subCat;
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
	 * Effectue une requête HTTP GET et récupère un flux en retour
	 * @param url L'url concernée
	 * @return Un <code>InputStream</code> contenant le résultat
	 * @throws IOException Si un problème intervient durant la requête
	 * @throws URISyntaxException Si l'url est foireuse
	 * @throws ServerMaintenanceException Si le forum est en maintenance
	 */
	protected String getAsString(String url) throws IOException, URISyntaxException, ServerMaintenanceException
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
	protected String getAsString(String url, boolean cr) throws IOException, URISyntaxException, ServerMaintenanceException
	{
		Log.d(HFR4droidApplication.TAG, "Retrieving " + url);
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
				content = Utils.streamToString(data, cr);
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

		if  (isOnMaintenance(content)) throw new ServerMaintenanceException(context.getString(R.string.server_maintenance));
		Log.d(HFR4droidApplication.TAG, "GET OK for " + url);
		return content;
	}
	
	abstract protected boolean isOnMaintenance(String content);
	
	protected void serializeCats() throws DataRetrieverException
	{
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		try
		{
		    File cacheDir = context.getCacheDir();
		    if (!cacheDir.exists()) cacheDir.mkdirs();

	        fos = new FileOutputStream(new File(context.getCacheDir(), CATS_CACHE_FILE_NAME));
			oos = new ObjectOutputStream(fos);
			oos.writeObject(cats);
		}
		catch (Exception e) // FileNotFoundException, IOException
		{
			throw new DataRetrieverException(context.getString(R.string.error_serializing_cats), e);
		}
		finally
		{
			if (oos != null) try { oos.close(); } catch (IOException e) {} 
		}
		Log.d(HFR4droidApplication.TAG, "Serializing " + cats.keySet().size() + " categories");
	}
	
	@SuppressWarnings("unchecked")
	protected boolean deserializeCats() throws DataRetrieverException
	{
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try
		{
			fis = new FileInputStream(new File(context.getCacheDir(), CATS_CACHE_FILE_NAME));
			ois = new ObjectInputStream(fis);
			cats = (Map<Category, List<SubCategory>>) ois.readObject();
		}
		catch (FileNotFoundException e)
		{
			Log.d(HFR4droidApplication.TAG, "No cache yet");
			return false;
		}
		catch (Exception e) // ClassNotFoundException, IOException
		{
			throw new DataRetrieverException(context.getString(R.string.error_deserializing_cats), e);
		}
		finally
		{
			if (ois != null) try { ois.close(); } catch (IOException e) {} 
		}
		Log.d(HFR4droidApplication.TAG, "Deserializing " + cats.keySet().size() + " categories");
		return true;
	}

	private void clearCache()
	{
		File f = new File(context.getCacheDir(), CATS_CACHE_FILE_NAME);
		f.delete();
		Log.d(HFR4droidApplication.TAG, "Destroying categories");
	}
}
