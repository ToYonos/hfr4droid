package info.toyonos.hfr4droid.core.auth;

import info.toyonos.hfr4droid.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

/**
 * <b>HfrAuthentication est la classe permettant de gérer la connexion au site http://forum.hardware.fr</b>
 * <p>Les informations obligatoires à fournir sont les suivantes :
 * <ul>
 * <li>Un nom d'utilisateur</li>
 * <li>Un mot de passe</li>
 * </ul>
 * </p>
 * <p>
 * Une fois la connexion effectuée, la methode getCookies() renverra un objet CookieStore contenant les cookies d'identification
 * </p>
 * 
 * @author Harkonnen
 * @version 1.0
 *
 */
public class HFRAuthentication
{
	private Context context;
	private String userName = null;
	private String userPassword = null;
	private String passHash = null;
	private String userId = null;


	public static final String AUTH_FORM_URL = "http://forum.hardware.fr/login_validation.php?config=hfr.inc";
	public static final String OLD_COOKIES_FILE_NAME = "/sdcard/hfr_cookies.dat";
	public static final String COOKIES_FILE_NAME = "hfr_cookies.dat";

	CookieStore cookieStore = null;

	/**
	 * Constructeur HfrAuthentication
	 * 
	 * @param user
	 * 			Le nom d'utilisateur
	 * @param password
	 * 			Le mot de passe
	 * 
	 */
	public HFRAuthentication(Context context, String user, String password) throws AuthenticationException
	{
		this.context = context;
		userName = user;
		userPassword = password;
		
		try
		{
			cookieStore = login();
			retrieveCookiesInfos(cookieStore);
		}
		catch (Exception e)
		{
			throw new AuthenticationException(context.getString(R.string.error_login), e);
		}
	}

	public HFRAuthentication(Context context) throws AuthenticationException
	{        
		this.context = context;
		
		try
		{
			cookieStore = deserializeCookies();
			retrieveCookiesInfos(cookieStore);
		}
		catch (Exception e)
		{
			throw new AuthenticationException(context.getString(R.string.error_login_from_cache), e);
		}
	}

	/**
	 * Retourne les cookies renvoyés par le serveur après login.
	 * 
	 * @return Un objet CookieStore contenant les cookies d'identification
	 */
	public CookieStore getCookies()
	{
		return cookieStore;
	}

	public String getUser()
	{
		return userName;
	}

	public String getPassword()
	{
		return userPassword;
	}

	public String getPassHash()
	{
		return passHash;
	}

	public String getUserId()
	{
		return userId;
	}

	private void retrieveCookiesInfos(CookieStore cs)
	{
		if (cs != null)
		{
			List<Cookie> lstCookies = cs.getCookies();
			for (Cookie cookie : lstCookies)
			{
				if (cookie.getName().equals("md_passs"))
					passHash = cookie.getValue();
				if (cookie.getName().equals("md_id"))
					userId = cookie.getValue();
				if (cookie.getName().equals("md_user") && userName == null)
					userName = URLDecoder.decode(cookie.getValue());
			}
		}
	}

	private CookieStore login() throws IOException, ClassNotFoundException
	{
		CookieStore cs = null;
		HttpPost post = new HttpPost(AUTH_FORM_URL);
		post.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; fr; rv:1.9.2) Gecko/20100101 Firefox/4.0.1");
		DefaultHttpClient client = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(client.getParams(), false);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pseudo", userName));
		params.add(new BasicNameValuePair("password", userPassword));

		post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		client.execute(post);

		if (client.getCookieStore() != null && client.getCookieStore().getCookies().size() != 0)
		{
			cs = client.getCookieStore();
			serializeCookies(cs);			
		}

		client.getConnectionManager().shutdown();

		return cs;
	}

	private void serializeCookies(CookieStore cs) throws IOException, ClassNotFoundException
	{
		List<SerializableCookie> hfrCookies = new ArrayList<SerializableCookie>();
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		for (int i=0; i<cs.getCookies().size(); i++)
			hfrCookies.add(new SerializableCookie(cs.getCookies().get(i)));

		fos = context.openFileOutput(COOKIES_FILE_NAME, Context.MODE_PRIVATE);
		oos = new ObjectOutputStream(fos);
		oos.writeObject(hfrCookies);
		oos.close();
	}

	@SuppressWarnings("unchecked")
	private CookieStore deserializeCookies() throws IOException, ClassNotFoundException
	{
		List<SerializableCookie> hfrCookies = null;
		CookieStore cs = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try
		{
			fis = context.openFileInput(COOKIES_FILE_NAME);
			ois = new ObjectInputStream(fis);
			hfrCookies = (ArrayList<SerializableCookie>) ois.readObject();
		}
		catch (FileNotFoundException e)
		{
			return null;
		}

		if (ois != null)
			ois.close();

		if (hfrCookies != null)
		{
			cs = new BasicCookieStore();
			for (SerializableCookie cookie : hfrCookies)
			{
				cs.addCookie(cookie.getCookie());
			}
		}

		return cs;
	}

	// Ajout temporaire @toyo pour effacer le cache
	public boolean clearCache()
	{
		return context.deleteFile(COOKIES_FILE_NAME);
	}
}
