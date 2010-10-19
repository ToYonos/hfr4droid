package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.service.MpCheckService;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
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
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.prefs);
	    final PreferenceScreen preferenceScreen = getPreferenceScreen();

	    Preference freqEnable = findPreference(HFR4droidActivity.PREF_SRV_MPS_ENABLE);
	    freqEnable.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	    {
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Intent intent = new Intent(HFR4droidPrefs.this, MpCheckService.class); 
				preferenceScreen.findPreference(HFR4droidActivity.PREF_SRV_MPS_FREQ).setEnabled((Boolean) newValue);
				if ((Boolean) newValue) startService(intent); else stopService(intent);
				return true;
			}
		});

	    Preference freqPref = findPreference(HFR4droidActivity.PREF_SRV_MPS_FREQ);
	    freqPref.setEnabled(
	    		preferenceScreen.getSharedPreferences().getBoolean(
	    							HFR4droidActivity.PREF_SRV_MPS_ENABLE,
	    							Boolean.parseBoolean(getString(R.string.pref_srv_mps_freq_default))));
	    freqPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	    {
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				int oldFreq = Integer.parseInt(preferenceScreen.getSharedPreferences().getString(
												HFR4droidActivity.PREF_SRV_MPS_FREQ,
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
							Toast t = Toast.makeText(HFR4droidPrefs.this, getString(R.string.error_settings), Toast.LENGTH_SHORT);
							t.show();
						}
					});
					return false;
				}

				if (oldFreq != freq)
				{
					Intent intent = new Intent(HFR4droidPrefs.this, MpCheckService.class);
					stopService(intent);
					startService(intent);
				}
				return true;
			}
		});	
	}
}
