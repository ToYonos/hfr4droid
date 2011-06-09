package info.toyonos.hfr4droid.service;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.core.data.MDDataRetriever;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ubikod.capptain.android.sdk.CapptainAgent;

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
			String logMsg = HFR4droidActivity.getMessage(e, null);
			Log.e(HFR4droidApplication.TAG, logMsg, e);

			Bundle bundle = new Bundle();
		    Writer result = new StringWriter();
		    PrintWriter printWriter = new PrintWriter(result);
		    e.printStackTrace(printWriter);
			bundle.putString("Stack trace", result.toString());
			CapptainAgent.getInstance(this).sendSessionError(logMsg, bundle);
		}
		notifyNewMps(nbMps, mp, type);
	}

	private MDDataRetriever getDataRetriever()
	{
		return ((HFR4droidApplication) getApplication()).getDataRetriever();
	}
}
