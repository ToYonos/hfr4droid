package info.toyonos.hfr4droid.service;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.HFR4droidActivity;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * <p>Service qui va vérifier automatiquement les mps de l'utilisateur et lui notifier.</p>
 * <p>Si au moins un nouveau mp est détecté, une notification est envoyée au système.</p>
 * <p>Le service met en place une vérification automatique suivant une fréquence, paramétrable dans l'application.</p>
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
				checkNewMps();
			} 
		}, 0, period);
	}
	
	@Override
	protected void doService(Intent intent)
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
