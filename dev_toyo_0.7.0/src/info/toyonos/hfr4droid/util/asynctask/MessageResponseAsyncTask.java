package info.toyonos.hfr4droid.util.asynctask;

import info.toyonos.hfr4droid.HFR4droidException;
import info.toyonos.hfr4droid.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.core.message.HFRMessageResponse;
import android.widget.Toast;

public abstract class MessageResponseAsyncTask extends ProgressDialogAsyncTask<Void, Void, HFRMessageResponse>
{
	private String message = null;

	public MessageResponseAsyncTask(HFR4droidActivity context, String message)
	{
		super(context);
		this.message = message;
	}

	protected abstract HFRMessageResponse executeInBackground() throws HFR4droidException;

	protected abstract void onActionFinished(String message);

	protected void onPreExecute() 
	{
		super.onPreExecute();
		progressDialog.setMessage(message);
		progressDialog.show();
	}

	protected HFRMessageResponse doInBackground(Void... params)
	{
		setThreadId();
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
			if (response != null) Toast.makeText(context, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}
}