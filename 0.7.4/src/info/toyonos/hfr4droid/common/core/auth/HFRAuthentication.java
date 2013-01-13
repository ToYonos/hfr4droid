package info.toyonos.hfr4droid.common.core.auth;

import info.toyonos.hfr4droid.common.HFR4droidApplication;
import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.core.data.HFRDataRetriever;
import info.toyonos.hfr4droid.common.core.utils.HttpClient;
import info.toyonos.hfr4droid.common.core.utils.HttpClientHelper;
import info.toyonos.hfr4droid.common.core.utils.TransformStreamException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

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
 * @author Harkonnen & ToYonos
 *
 */
public class HFRAuthentication
{
	private HFR4droidApplication context;
	private HttpClient<String> client;
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
	public HFRAuthentication(HFR4droidApplication context, HttpClientHelper httpClientHelper, String user, String password) throws AuthenticationException
	{
		this.context = context;
		userName = user;
		userPassword = password;
		
		createHttpClient(httpClientHelper);
		
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

	public HFRAuthentication(HFR4droidApplication context, HttpClientHelper httpClientHelper) throws AuthenticationException
	{        
		this.context = context;
		createHttpClient(httpClientHelper);

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

	private void createHttpClient(HttpClientHelper httpClientHelper)
	{
		client = new HttpClient<String>(httpClientHelper)
		{		
			@Override
			protected String transformStream(InputStream is) throws TransformStreamException
			{
				try
				{
					return HFRDataRetriever.streamToString(is, false);
				}
				catch (IOException e)
				{
					throw new TransformStreamException(e);
				}
			}
		};
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

	private CookieStore login() throws IOException, ClassNotFoundException, TransformStreamException, URISyntaxException
	{
		CookieStore cs = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pseudo", userName));
		params.add(new BasicNameValuePair("password", userPassword));
		
		String response = client.doPost(AUTH_FORM_URL, null, params);
		
		if (client.getHttpClientHelper().getHttpClient().getCookieStore() != null &&
			client.getHttpClientHelper().getHttpClient().getCookieStore().getCookies().size() != 0 &&
			!response.matches(".*Votre mot de passe ou nom d'utilisateur n'est pas valide.*"))
		{
			cs = client.getHttpClientHelper().getHttpClient().getCookieStore();
			serializeCookies(cs);
		}

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
		catch (ClassNotFoundException e) // Pour gérer le changement de nom des packages
		{
			Log.w(HFR4droidApplication.TAG, "Wrong classname for the cookies, cancelling the auto-login");
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
