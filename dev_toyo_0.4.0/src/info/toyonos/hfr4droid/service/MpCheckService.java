package info.toyonos.hfr4droid.service;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.PostsActivity;
import info.toyonos.hfr4droid.activity.TopicsActivity;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.data.MDDataRetriever;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * <p>Service qui va vérifier les mps de l'utilisateur.</p>
 * <p>Si au moins un nouveau mp est détecté, une notification est envoyée au système.</p>
 * 
 * @author ToYonos
 * @see android.app.Service
 *
 */
public class MpCheckService extends Service
{
	public static final int NOTIFICATION_ID = 42;

	public static int nbNotification = 0; // TODO modifier comme huit

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		startService();
	}
	
	protected void startService()
	{
		// TODO le service en one shot
	}

	protected MDDataRetriever getDataRetriever()
	{
		return ((HFR4droidApplication)getApplication()).getDataRetriever();
	}

	protected void notifyNewMps(int nbMps, Topic mp)
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		if (nbMps < 1)
		{
			nbNotification = 0;
			notificationManager.cancel(MpCheckService.NOTIFICATION_ID);
			return;
		}

		nbNotification++;
		String notificationMessage = getResources().getQuantityString(R.plurals.mp_notification_content, nbMps, nbMps);
		PendingIntent pendingIntent = null;
		if (nbMps == 1)
		{
			Intent intent = new Intent(MpCheckService.this, PostsActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("topic", mp);
			bundle.putInt("pageNumber", mp.getNbPages());
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
