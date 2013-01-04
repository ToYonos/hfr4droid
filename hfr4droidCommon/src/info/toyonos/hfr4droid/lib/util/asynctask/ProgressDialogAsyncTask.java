package info.toyonos.hfr4droid.lib.util.asynctask;

import info.toyonos.hfr4droid.lib.activity.HFR4droidActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

public abstract class ProgressDialogAsyncTask<P, G, R> extends AsyncTask<P, G, R>
{
	protected HFR4droidActivity context;
	protected ProgressDialog progressDialog;
	private long backgroundThreadId = -1;
	
	public ProgressDialogAsyncTask(HFR4droidActivity context)
	{
		this.context = context;
	}

	@Override
	protected void onPreExecute() 
	{
		progressDialog = new ProgressDialog(context);
		progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(new OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				cancel(true);
			}
		});
	}
	
	@Override
	protected void onCancelled()
	{
		context.getHFR4droidApplication().getHttpClientHelper().abortRequest(backgroundThreadId);
	}

	protected void setThreadId()
	{
		backgroundThreadId = Thread.currentThread().getId();
	}

	public HFR4droidActivity getContext()
	{
		return context;
	}
}
