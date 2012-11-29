package info.toyonos.hfr4droid.service;

import info.toyonos.hfr4droid.HFR4droidApplication;
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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

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
	
	public static enum NotificationType
	{
		STATUS_BAR(1),
		TOAST(2);
		
		private final int value;

		private NotificationType(int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return this.value;
		}
		
		public static NotificationType fromInt(int anInt) 
		{
			for (NotificationType type : NotificationType.values())
			{
				if (anInt == type.getValue()) return type;
			}
			return null;
		}
	};

	private Handler handler = null;
	
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
		handler = new Handler(Looper.getMainLooper());
		if (run != null) new Thread(run).start();
	}
	
	protected Runnable doService(final Intent intent)
	{
		return new Runnable()
		{
			public void run()
			{
				int nbMps = intent.getIntExtra("nbMps", 0);
				Topic mp = (Topic) intent.getSerializableExtra("mp");
				notifyNewMps(nbMps, mp, getNotificationType());
				stopSelf();
			}
		};
	}

	protected void notifyNewMps(int nbMps, Topic mp, NotificationType type)
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		synchronized (MpNotifyService.class)
		{
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
		}

		final String notificationMessage = getResources().getQuantityString(R.plurals.mp_notification_content, nbMps, nbMps);
		switch (type)
		{
			case STATUS_BAR:
				PendingIntent pendingIntent = null;
				if (nbMps == 1 && mp != null)
				{
					Intent intent = new Intent(MpNotifyService.this, PostsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
				break;

			case TOAST:
				handler.post(new Runnable()
				{
					public void run()
					{
						Toast.makeText(getApplicationContext(), notificationMessage, Toast.LENGTH_LONG).show();
					}
				});
				break;
		}
	}
	
	protected NotificationType getNotificationType()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(HFR4droidApplication.PREF_NOTIFICATION_TYPE, getString(R.string.pref_notification_type_default));
		return NotificationType.fromInt(Integer.parseInt(value));
	}
}
