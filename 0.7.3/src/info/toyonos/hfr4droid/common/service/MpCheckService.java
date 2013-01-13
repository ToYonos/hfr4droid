package info.toyonos.hfr4droid.common.service;

import info.toyonos.hfr4droid.common.HFR4droidApplication;
import info.toyonos.hfr4droid.common.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.common.core.bean.Topic;
import info.toyonos.hfr4droid.common.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.common.core.data.MDDataRetriever;
import android.content.Intent;
import android.util.Log;

/**
 * <p>Service qui va vérifier et notifier les nouveaux mps à l'utilisateur.</p>
 * <p>Si au moins un nouveau mp est détecté, une notification est envoyée au système.</p>
 * 
 * @author ToYonos
 * @see android.app.Service
 *
 */
public class MpCheckService extends MpNotifyService
{
	@Override
	protected Runnable doService(Intent intent)
	{
		return new Runnable()
		{
			public void run()
			{
				checkNewMps(getNotificationType());
				stopSelf();
			}
		};
	}

	protected void checkNewMps(NotificationType type)
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
		notifyNewMps(nbMps, mp, type);
	}

	private MDDataRetriever getDataRetriever()
	{
		return ((HFR4droidApplication) getApplication()).getDataRetriever();
	}
}
