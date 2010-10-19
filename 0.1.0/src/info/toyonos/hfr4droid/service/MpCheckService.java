package info.toyonos.hfr4droid.service;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.activity.PostsActivity;
import info.toyonos.hfr4droid.activity.TopicsActivity;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.data.MDDataRetriever;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
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
public class MpCheckService extends Service
{
	public static final int NOTIFICATION_ID = 42;

	private Timer timer;
	public static int nbNotification = 0;	
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

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
	        	catch (Exception e)
	        	{
	        		Log.e(this.getClass().getSimpleName(), String.format(getString(R.string.error), e.getClass().getName(), e.getMessage()));
				}

	        	if (nbMps > 0)
	        	{
	        		notifyNewMps(nbMps, mp);
	        	}
	        } 
	    }, 0, period);
	}
	 
	@Override
	public void onDestroy()
	{
	    this.timer.cancel(); 
	}
	
	private MDDataRetriever getDataRetriever()
	{
		return ((HFR4droidApplication)getApplication()).getDataRetriever();
	}
	
	private int getSrvMpsFreq()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(HFR4droidActivity.PREF_SRV_MPS_FREQ, getString(R.string.pref_srv_mps_freq_default));
		return Integer.parseInt(value);
	}
	
	private void notifyNewMps(int nbMps, Topic mp)
	{
		if (nbMps < 1) return;

		nbNotification++;
		String notificationMessage = getResources().getQuantityString(R.plurals.mp_notification_content, nbMps, nbMps);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	PendingIntent pendingIntent = null;
    	if (nbMps == 1)
		{
	    	Intent intent = new Intent(MpCheckService.this, PostsActivity.class);
	    	Bundle bundle = new Bundle();
			bundle.putSerializable("topic", mp);
			intent.setAction("" + Math.random()); // Samed issue as this guy : http://stackoverflow.com/questions/2882459/getextra-from-intent-launched-from-a-pendingintent 
			intent.putExtras(bundle);
	    	pendingIntent = PendingIntent.getActivity(MpCheckService.this, 0, intent, 0);				
		}
		else
		{
	    	Intent intent = new Intent(MpCheckService.this, TopicsActivity.class);
	    	Bundle bundle = new Bundle();
			bundle.putSerializable("cat", Category.MPS_CAT);
			intent.putExtras(bundle);
	    	pendingIntent = PendingIntent.getActivity(MpCheckService.this, 0, intent, 0);			
		}

    	Notification notification = new Notification(R.drawable.icon, notificationMessage, System.currentTimeMillis());
        notification.setLatestEventInfo(MpCheckService.this, getString(R.string.app_name), notificationMessage, pendingIntent);
        //if (nbNotification == 1) notification.defaults |= Notification.DEFAULT_VIBRATE;
        if (nbNotification == 1) notification.vibrate = new long[] { 0, 250, 100, 250};
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, notification);
	}
}
