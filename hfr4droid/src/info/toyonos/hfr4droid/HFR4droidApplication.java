package info.toyonos.hfr4droid;

import info.toyonos.hfr4droid.activity.HFR4droidActivity.DrawableDisplayType;
import info.toyonos.hfr4droid.core.auth.AuthenticationException;
import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.bean.Profile;
import info.toyonos.hfr4droid.core.data.HFRDataRetriever;
import info.toyonos.hfr4droid.core.data.MDDataRetriever;
import info.toyonos.hfr4droid.core.message.HFRMessageSender;
import info.toyonos.hfr4droid.core.utils.HttpClientHelper;
import info.toyonos.hfr4droid.util.asynctask.PreLoadingAsyncTask;
import info.toyonos.hfr4droid.util.asynctask.PreLoadingAsyncTask.PreLoadingCompleteListener;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
	public static final String PREF_WELCOME_SCREEN			= "PrefWelcomeScreen";
	public static final String PREF_CHECK_MPS_ENABLE		= "PrefCheckMpsEnable";
	public static final String PREF_NOTIFICATION_TYPE		= "PrefNotificationType";
	public static final String PREF_TYPE_DRAPEAU			= "PrefTypeDrapeau";
	public static final String PREF_SIGNATURE_ENABLE		= "PrefSignatureEnable";
	public static final String PREF_DBLTAP_ENABLE			= "PrefDblTapEnable";
	public static final String PREF_OVERRIDE_LIGHT_MODE		= "PrefOverrideLightMode";
	public static final String PREF_SWIPE					= "PrefSwipe";
	public static final String PREF_PRELOADING_CALLBACK		= "PrefPreloadingCallback";
	public static final String PREF_FULLSCREEN_ENABLE		= "PrefFullscreenEnable";
	public static final String PREF_THEME					= "PrefTheme";
	public static final String PREF_POLICE_SIZE				= "PrefPoliceSize";
	public static final String PREF_AVATARS_DISPLAY_TYPE	= "PrefAvatarsDisplayType";
	public static final String PREF_SMILEYS_DISPLAY_TYPE	= "PrefSmileysDisplayType";
	public static final String PREF_IMGS_DISPLAY_TYPE		= "PrefImgsDisplayType";
	public static final String PREF_SRV_MPS_ENABLE			= "PrefSrvMpsEnable";
	public static final String PREF_SRV_MPS_FREQ			= "PrefSrvMpsFreq";
	
	public static final String TAG = "HFR4droid";
	
	private HttpClientHelper httpClientHelper;
	private MDDataRetriever dataRetriever;
	private HFRAuthentication auth;
	private HFRMessageSender msgSender;
	private Map<String, Profile> profiles;
	private boolean isMonoCore;

	@Override
	public void onCreate()
	{
		super.onCreate();
		httpClientHelper = new HttpClientHelper();
		dataRetriever = new HFRDataRetriever(this, httpClientHelper);
		if (new File(HFRAuthentication.OLD_COOKIES_FILE_NAME).exists()) new File(HFRAuthentication.OLD_COOKIES_FILE_NAME).delete();
		profiles = new HashMap<String, Profile>();
		isMonoCore = getNumCores() == 1;
	}

	public HttpClientHelper getHttpClientHelper()
	{
		return httpClientHelper;
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
		auth = fromCache ? new HFRAuthentication(this, httpClientHelper) : new HFRAuthentication(this, httpClientHelper, user, password);

		boolean isLoggedIn = auth.getCookies() != null;
		if (isLoggedIn)
		{
			msgSender = new HFRMessageSender(this, httpClientHelper, auth);
			dataRetriever = new HFRDataRetriever(this, httpClientHelper, auth, !fromCache);
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
	 * Déconnexion du forum
	 */
	public void logout()
	{
		if (auth != null)
		{
			auth.clearCache();
			auth = null;
			msgSender = null;
			dataRetriever = new HFRDataRetriever(this, httpClientHelper, true);
		}
	}

	/**
	 * @return un booleen indiquant si on est loggué
	 */
	public boolean isLoggedIn()
	{
		return auth != null && auth.getCookies() != null;
	}
	
	/**
	 * @return Le profil d'un membre s'il a été stocké en cache
	 */
	public Profile getProfile(String pseudo)
	{
		return profiles.get(pseudo);
	}
	
	/**
	 * Stocke le profil d'un membre en cache
	 */
	public void setProfile(String pseudo, Profile profile)
	{
		profiles.put(pseudo, profile);
	}
	
	// Getter des préférences modifiables par l'utilisateur

	public int getWelcomeScreen()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return Integer.parseInt(settings.getString(PREF_WELCOME_SCREEN, getString(R.string.pref_welcome_screen_default)));
	}

	public boolean isCheckMpsEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_CHECK_MPS_ENABLE, Boolean.parseBoolean(getString(R.string.pref_check_mps_enable_default)));
	}
	
	public int getTypeDrapeau()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return Integer.parseInt(settings.getString(PREF_TYPE_DRAPEAU, getString(R.string.pref_type_drapeau_default)));
	}

	public boolean isSignatureEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_SIGNATURE_ENABLE, Boolean.parseBoolean(getString(R.string.pref_signature_enable_default)));
	}

	public boolean isDblTapEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_DBLTAP_ENABLE, Boolean.parseBoolean(getString(R.string.pref_dbltap_enable_default)));
	}
	
	public boolean isOverrideLightModeEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_OVERRIDE_LIGHT_MODE, Boolean.parseBoolean(getString(R.string.pref_override_light_mode_enable_default)));
	}
	
    public float getSwipe()
    {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            return Float.parseFloat(settings.getString(PREF_SWIPE, getString(R.string.pref_swipe_default)));
    }
	
    public PreLoadingCompleteListener getPreloadingCallback()
    {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int ind = Integer.parseInt(settings.getString(PREF_PRELOADING_CALLBACK, getString(R.string.pref_preloading_callback_default)));
        return ind != -1 ? PreLoadingAsyncTask.PRELOADING_COMPLETE_LISTENERS[ind] : null;
    }
    
	public boolean isFullscreenEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_FULLSCREEN_ENABLE, Boolean.parseBoolean(getString(R.string.pref_fullscreen_enable_default)));
	}
	
	public String getThemeKey()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getString(PREF_THEME, getString(R.string.pref_theme_default));
	}
	
	public int getPoliceSize()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return Integer.parseInt(settings.getString(PREF_POLICE_SIZE, getString(R.string.pref_police_size_default)));
	}

	public DrawableDisplayType getAvatarsDisplayType()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(PREF_AVATARS_DISPLAY_TYPE, getString(R.string.pref_avatars_display_type_default));
		return DrawableDisplayType.fromInt(Integer.parseInt(value));
	}

	public DrawableDisplayType getSmileysDisplayType()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(PREF_SMILEYS_DISPLAY_TYPE, getString(R.string.pref_smileys_display_type_default));
		return DrawableDisplayType.fromInt(Integer.parseInt(value));
	}

	public DrawableDisplayType getImgsDisplayType()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(PREF_IMGS_DISPLAY_TYPE, getString(R.string.pref_imgs_display_type_default));
		return DrawableDisplayType.fromInt(Integer.parseInt(value));
	}

	public boolean isSrvMpEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_SRV_MPS_ENABLE, Boolean.parseBoolean(getString(R.string.pref_srv_mps_enable_default)));
	}
	
	public boolean isLightMode()
	{
		// return isMonoCore && !isOverrideLightModeEnable();
		return !isOverrideLightModeEnable();
	}
	
	public boolean isMonoCore()
	{
		return isMonoCore;
	}
	
	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 * @return The number of cores, or 1 if failed to get result
	 * @see http://stackoverflow.com/questions/7962155/how-can-you-detect-a-dual-core-cpu-on-an-android-device-from-code
	 */
	private int getNumCores()
	{
	    //Private Class to display only CPU devices in the directory listing
	    class CpuFilter implements FileFilter
	    {
	        public boolean accept(File pathname)
	        {
	            //Check if filename is "cpu", followed by a single digit number
	            return Pattern.matches("cpu[0-9]", pathname.getName());
	        }      
	    }

	    try
	    {
	        //Get directory containing CPU info
	        File dir = new File("/sys/devices/system/cpu/");
	        //Filter to only list the devices we care about
	        File[] files = dir.listFiles(new CpuFilter());
	        //Return the number of cores (virtual CPU devices)
	        return files.length;
	    }
	    catch(Exception e)
	    {
	        //Default to return 1 core
	    	Log.e(TAG, e.getMessage(), e);
	        return 1;
	    }
	}
}
