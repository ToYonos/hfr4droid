package info.toyonos.hfr4droid;

import info.toyonos.hfr4droid.core.auth.AuthenticationException;
import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.data.HFRDataRetriever;
import info.toyonos.hfr4droid.core.data.MDDataRetriever;
import info.toyonos.hfr4droid.core.message.HFRMessageSender;

import java.io.File;
import java.io.IOException;

import com.ubikod.capptain.android.sdk.CapptainApplication;

/**
 * <p>Classe repr�sentant l'application HFR4droid. Permet de centraliser les instances 
 * du <code>HttpClientHelper</code> et du <code>MDDataRetriever</code></p>
 * 
 * @author ToYonos
 * @see android.app.Application
 *
 */
public class HFR4droidApplication extends CapptainApplication
{
	public static final String TAG = "HFR4droid";
	
	private MDDataRetriever dataRetriever;
	private HFRAuthentication auth;
	private HFRMessageSender msgSender;

	@Override
	public void onApplicationProcessCreate()
	{
		super.onApplicationProcessCreate();
		dataRetriever = new HFRDataRetriever(this);
		if (new File(HFRAuthentication.OLD_COOKIES_FILE_NAME).exists()) new File(HFRAuthentication.OLD_COOKIES_FILE_NAME).delete();
	}

	public MDDataRetriever getDataRetriever()
	{
		return dataRetriever;
	}

	public HFRMessageSender getMessageSender()
	{
		return msgSender;
	}

	/**
	 * Connexion au forum
	 * @param user Le login
	 * @param password Le mot de passe
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IOException
	 */
	public boolean login(String user, String password) throws AuthenticationException
	{
		boolean fromCache = user == null && password == null;
		auth = fromCache ? new HFRAuthentication(this) : new HFRAuthentication(this, user, password);

		boolean isLoggedIn = auth.getCookies() != null;
		if (isLoggedIn)
		{
			msgSender = new HFRMessageSender(this, auth);
			dataRetriever = new HFRDataRetriever(this, auth, !fromCache);
		}
		return isLoggedIn;
	}

	/**
	 * Connexion au forum depuis le cache
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public boolean login() throws AuthenticationException
	{
		return login(null, null);
	}

	/**
	 * D�connexion du forum
	 */
	public void logout()
	{
		if (auth != null)
		{
			auth.clearCache();
			auth = null;
			msgSender = null;
			dataRetriever = new HFRDataRetriever(this, true);
		}
	}

	/**
	 * @return un booleen indiquant si on est loggu�
	 */
	public boolean isLoggedIn()
	{
		return auth != null && auth.getCookies() != null;
	}
}