package info.toyonos.hfr4droid;

import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.data.HFRDataRetriever;
import info.toyonos.hfr4droid.core.data.MDDataRetriever;
import info.toyonos.hfr4droid.core.message.HFRMessageSender;

import java.io.IOException;

import android.app.Application;

/**
 * <p>Classe représentant l'application HFR4droid. Permet de centraliser les instances 
 * du <code>HttpClientHelper</code> et du <code>MDDataRetriever</code></p>
 * 
 * @author ToYonos
 * @see android.app.Application
 *
 */
public class HFR4droidApplication extends Application
{	
	private MDDataRetriever dataRetriever;
	private HFRAuthentication auth;
	private HFRMessageSender msgSender;

	@Override
	public void onCreate()
	{
		super.onCreate();
		dataRetriever = new HFRDataRetriever();
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
	public boolean login(String user, String password) throws IOException, ClassNotFoundException
	{
		auth = user != null && password != null ?
				new HFRAuthentication(user, password) :
				new HFRAuthentication();

		boolean isLoggedIn = auth.getCookies() != null;
		if (isLoggedIn)
		{
			msgSender = new HFRMessageSender(auth);
			dataRetriever = new HFRDataRetriever(auth);
		}
		return isLoggedIn;
	}

	/**
	 * Connexion au forum depuis le cache
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public boolean login() throws IOException, ClassNotFoundException
	{
		return login(null, null);
	}

	/**
	 * Déconnexion du forum
	 */
	public void logout()
	{
		if (auth != null)
		{
			auth.clearCache();
			auth = null;
			msgSender = null;
			dataRetriever = new HFRDataRetriever();
		}
	}

	/**
	 * @return un booleen indiquant si on est loggué
	 */
	public boolean isLoggedIn()
	{
		return auth != null && auth.getCookies() != null;
	}	
}
