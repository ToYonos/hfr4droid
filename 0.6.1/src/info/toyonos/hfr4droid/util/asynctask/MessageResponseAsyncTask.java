package info.toyonos.hfr4droid.util.asynctask;

import info.toyonos.hfr4droid.HFR4droidException;
import info.toyonos.hfr4droid.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.core.message.HFRMessageResponse;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class MessageResponseAsyncTask extends AsyncTask<Void, Void, HFRMessageResponse>
{
	private HFR4droidActivity context;
	private ProgressDialog progressDialog;

	public MessageResponseAsyncTask(HFR4droidActivity context, String message)
	{
		this.context = context;
		progressDialog = new ProgressDialog(context);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage(message);
	}

	protected abstract HFRMessageResponse executeInBackground() throws HFR4droidException;

	protected abstract void onActionFinished(String message);

	protected void onPreExecute() 
	{
		progressDialog.show();
	}

	protected HFRMessageResponse doInBackground(Void... params)
	{
		HFRMessageResponse response = null;
		try
		{
			return executeInBackground();
		} 
		catch (HFR4droidException e)
		{
			context.error(e, true, true);
		}
		return response;
	}

	protected void onPostExecute(HFRMessageResponse response)
	{
		progressDialog.dismiss();
		if (response != null && response.isSuccess())
		{
			onActionFinished(response.getMessage());
		}
		else
		{
			Toast.makeText(context, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}
}