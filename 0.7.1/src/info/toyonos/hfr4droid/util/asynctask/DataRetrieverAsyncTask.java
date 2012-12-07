package info.toyonos.hfr4droid.util.asynctask;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;

import java.util.List;

import android.widget.Toast;

public abstract class DataRetrieverAsyncTask<E, P> extends ProgressDialogAsyncTask<P, Void, List<E>>
{
	private boolean sameActivity;
	private boolean displayLoading;
	private String noElementMsg;
	protected int pageNumber;
	private String progressTitle;
	private String progressContent;

	public DataRetrieverAsyncTask(HFR4droidActivity context)
	{
		super(context);
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	protected abstract List<E> retrieveDataInBackground(P... params) throws DataRetrieverException;

	protected abstract void onPostExecuteSameActivity(List<E> elements) throws ClassCastException;

	protected abstract void onPostExecuteOtherActivity(List<E> elements);

	protected void onPostExecuteNoItem(boolean sameActivity, Toast t)
	{
		t.show();
	}
	
	protected void onError(Exception e)
	{
		context.error(e, true, true);
	}

	public void execute(int pageNumber, P... params)
	{
		execute(null, null, null, true, pageNumber, false, params);
	}

	public void execute(String progressTitle, String progressContent, String noElementMsg, boolean sameActivity, int pageNumber, P... params)
	{
		execute(progressTitle, progressContent, noElementMsg, sameActivity, pageNumber, true, params);
	}

	public void execute(String progressTitle, String progressContent, String noElementMsg, boolean sameActivity, P... params)
	{
		execute(progressTitle, progressContent, noElementMsg, sameActivity, -1, true, params);
	}
	
	public void execute(String progressTitle, String progressContent, String noElementMsg, boolean sameActivity, boolean displayLoading, P... params)
	{
		execute(progressTitle, progressContent, noElementMsg, sameActivity, -1, displayLoading, params);
	}
	
	public void execute(String progressTitle, String progressContent, String noElementMsg, boolean sameActivity, int pageNumber, boolean displayLoading, P... params)
	{
		this.noElementMsg = noElementMsg;
		this.sameActivity = sameActivity;
		this.displayLoading = displayLoading;
		this.pageNumber = pageNumber;
		this.progressTitle = progressTitle;
		this.progressContent = progressContent;
		execute(params);
	}

	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		progressDialog.setTitle(progressTitle != null ? progressTitle : context.getString(R.string.loading));
		progressDialog.setMessage(progressContent);
		if (displayLoading) progressDialog.show();
	}

	@Override
	protected List<E> doInBackground(final P... params)
	{
		setThreadId();
		List<E> elements = null;
		try
		{
			elements = retrieveDataInBackground(params);
		}
		catch (DataRetrieverException e)
		{
			onError(e);
		}
		return elements;
	}

	@Override
	protected void onPostExecute(final List<E> elements)
	{
		if (elements != null)
		{
			if (elements.size() > 0)					
			{
				if (sameActivity)
				{
					try
					{
						onPostExecuteSameActivity(elements);
					}
					catch (ClassCastException e)
					{
						context.error(e);
						throw new RuntimeException(e);
					}
				}
				else
				{
					onPostExecuteOtherActivity(elements);
				}
			}
			else
			{
				final Toast t = Toast.makeText(context, noElementMsg, Toast.LENGTH_SHORT);
				onPostExecuteNoItem(sameActivity, t);
			}
		}
		if (displayLoading) progressDialog.dismiss();				
	}
}