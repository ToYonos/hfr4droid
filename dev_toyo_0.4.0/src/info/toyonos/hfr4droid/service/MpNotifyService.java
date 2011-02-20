package info.toyonos.hfr4droid.service;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.PostsActivity;
import info.toyonos.hfr4droid.activity.TopicsActivity;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;

/**
 * <p>Service qui va notifier les nouveaux mps à l'utilisateur.</p>
 * <p>Si au moins un nouveau mp est détecté, une notification est envoyée au système.</p>
 * 
 * @author ToYonos
 * @see android.app.Service
 *
 */
public class MpNotifyService extends Service
{
	public static final int NOTIFICATION_ID = 42;

	public static int currentNewMps = 0;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onStart(final Intent intent, int startId)
	{
		super.onStart(intent, startId);
		Runnable run = doService(intent);
		if (run != null) new Thread(run).start();
	}
	
	protected Runnable doService(final Intent intent)
	{
		return new Runnable()
		{	
			@Override
			public void run()
			{
				int nbMps = intent.getIntExtra("nbMps", 0);
				Topic mp = (Topic) intent.getSerializableExtra("mp");
				notifyNewMps(nbMps, mp);
				stopSelf();
			}
		};
	}

	protected void notifyNewMps(int nbMps, Topic mp)
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (nbMps < 1) // Pas de nouveau mp, on annule une éventuelle notification en cours
		{
			currentNewMps = 0;
			notificationManager.cancel(MpNotifyService.NOTIFICATION_ID);
			return;
		}
		if (currentNewMps >= nbMps) // Pas de nouvelle notification, le nombre de mps n'a pas changé
		{
			currentNewMps = nbMps;
			return;
		}
		currentNewMps = nbMps;

		String notificationMessage = getResources().getQuantityString(R.plurals.mp_notification_content, nbMps, nbMps);
		PendingIntent pendingIntent = null;
		if (nbMps == 1 && mp != null)
		{
			Intent intent = new Intent(MpNotifyService.this, PostsActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("topic", mp);
			bundle.putInt("pageNumber", mp.getNbPages());
			intent.setAction("" + Math.random()); // Samed issue as this guy : http://stackoverflow.com/questions/2882459/getextra-from-intent-launched-from-a-pendingintent 
			intent.putExtras(bundle);
			pendingIntent = PendingIntent.getActivity(MpNotifyService.this, 0, intent, 0);				
		}
		else
		{
			Intent intent = new Intent(MpNotifyService.this, TopicsActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("cat", Category.MPS_CAT);
			bundle.putInt("pageNumber", 1);
			intent.setAction("" + Math.random()); // Samed issue as this guy : http://stackoverflow.com/questions/2882459/getextra-from-intent-launched-from-a-pendingintent
			intent.putExtras(bundle);
			pendingIntent = PendingIntent.getActivity(MpNotifyService.this, 0, intent, 0);			
		}

		Notification notification = new Notification(R.drawable.icon, notificationMessage, System.currentTimeMillis());
		notification.setLatestEventInfo(MpNotifyService.this, getString(R.string.app_name), notificationMessage, pendingIntent);
		notification.vibrate = new long[] { 0, 250, 200, 250};
		notification.ledOnMS = 100;
		notification.ledOffMS = 100;
		notification.ledARGB = Color.GREEN;
		notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
}
