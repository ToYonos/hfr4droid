package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.service.MpTimerCheckService;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * <p>Activity gérant les préférences de l'application</p>
 * 
 * @author ToYonos
 *
 */
public class HFR4droidPrefs extends PreferenceActivity
{	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		switchFullscreen(PreferenceManager.getDefaultSharedPreferences(this)
											.getBoolean(HFR4droidApplication.PREF_FULLSCREEN_ENABLE,
														Boolean.parseBoolean(getString(R.string.pref_fullscreen_enable_default))));
		
		addPreferencesFromResource(R.xml.prefs);
		final PreferenceScreen preferenceScreen = getPreferenceScreen();
		
		Preference checkMpsEnable = findPreference(HFR4droidApplication.PREF_CHECK_MPS_ENABLE);
		checkMpsEnable.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{ 
				preferenceScreen.findPreference(HFR4droidApplication.PREF_NOTIFICATION_TYPE).setEnabled((Boolean) newValue);
				return true;
			}
		});
		
		Preference notificationTypePref = findPreference(HFR4droidApplication.PREF_NOTIFICATION_TYPE);
		notificationTypePref.setEnabled(
				preferenceScreen.getSharedPreferences().getBoolean(
						HFR4droidApplication.PREF_CHECK_MPS_ENABLE,
						Boolean.parseBoolean(getString(R.string.pref_check_mps_enable_default))));

		Preference fullscreenEnable = findPreference(HFR4droidApplication.PREF_FULLSCREEN_ENABLE);
		fullscreenEnable.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				switchFullscreen((Boolean) newValue);
				return true;
			}
		});
		
		Preference srvMpsEnable = findPreference(HFR4droidApplication.PREF_SRV_MPS_ENABLE);
		srvMpsEnable.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Intent intent = new Intent(HFR4droidPrefs.this, MpTimerCheckService.class); 
				preferenceScreen.findPreference(HFR4droidApplication.PREF_SRV_MPS_FREQ).setEnabled((Boolean) newValue);
				if ((Boolean) newValue) startService(intent); else stopService(intent);
				return true;
			}
		});

		Preference freqPref = findPreference(HFR4droidApplication.PREF_SRV_MPS_FREQ);
		freqPref.setEnabled(
				preferenceScreen.getSharedPreferences().getBoolean(
						HFR4droidApplication.PREF_SRV_MPS_ENABLE,
						Boolean.parseBoolean(getString(R.string.pref_srv_mps_freq_default))));
		freqPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				int oldFreq = Integer.parseInt(preferenceScreen.getSharedPreferences().getString(
						HFR4droidApplication.PREF_SRV_MPS_FREQ,
						getString(R.string.pref_srv_mps_freq_default)));
				int freq = -1;
				try
				{
					freq = Integer.parseInt(newValue.toString());
					if (freq < 1) throw new NumberFormatException();
				}
				catch (NumberFormatException e)
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(HFR4droidPrefs.this, getString(R.string.error_settings), Toast.LENGTH_SHORT).show();
						}
					});
					return false;
				}

				if (oldFreq != freq)
				{
					Intent intent = new Intent(HFR4droidPrefs.this, MpTimerCheckService.class);
					stopService(intent);
					startService(intent);
				}
				return true;
			}
		});
	}
	
	public void switchFullscreen(boolean fullscreen)
	{
		if (fullscreen)
		{
			getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN,   
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else
		{
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}
}
