package info.toyonos.hfr4droid.util.asyncktask;

import info.toyonos.hfr4droid.HFR4droidException;
import info.toyonos.hfr4droid.activity.HFR4droidActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public abstract class SimpleAsyncTask<T> extends AsyncTask<Void, Void, T>
{
	private HFR4droidActivity context;
	private ProgressDialog progressDialog;

	public SimpleAsyncTask(HFR4droidActivity context, String message)
	{
		this.context = context;
		progressDialog = new ProgressDialog(context);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage(message);
	}

	protected abstract T executeInBackground() throws HFR4droidException;

	protected abstract void onActionFinished(T response);

	protected void onPreExecute() 
	{
		progressDialog.show();
	}

	protected T doInBackground(Void... params)
	{
		T response = null;
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

	protected void onPostExecute(T response)
	{
		progressDialog.dismiss();
		onActionFinished(response);
	}
}