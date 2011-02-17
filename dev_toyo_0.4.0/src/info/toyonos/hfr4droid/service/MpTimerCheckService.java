package info.toyonos.hfr4droid.service;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;

import java.util.Timer;
import java.util.TimerTask;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * <p>Service qui va vérifier les mps de l'utilisateur.</p>
 * <p>Si au moins un nouveau mp est détecté, une notification est envoyée au système.</p>
 * <p>La fréquence de vérification est paramétrable.</p>
 * 
 * @author ToYonos
 * @see android.app.Service
 *
 */
public class MpTimerCheckService extends MpCheckService
{
	private Timer timer;

	@Override
	public void onCreate()
	{ 
		super.onCreate(); 
		long period = getSrvMpsFreq() * 60 * 1000;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				Topic mp = new Topic(-1, null);
				int nbMps = 0;
				try
				{
					nbMps = getDataRetriever().countNewMps(mp);
				}
				catch (DataRetrieverException e)
				{
					Log.e(HFR4droidApplication.TAG, HFR4droidActivity.getMessage(e, null), e);
				}

				if (nbMps > 0)
				{
					notifyNewMps(nbMps, mp);
				}
			} 
		}, 0, period);
	}
	
	@Override
	protected void startService()
	{
		// Rien ici
	}
	
	@Override
	public void onDestroy()
	{
		this.timer.cancel(); 
	}

	private int getSrvMpsFreq()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(HFR4droidActivity.PREF_SRV_MPS_FREQ, getString(R.string.pref_srv_mps_freq_default));
		return Integer.parseInt(value);
	}
}
