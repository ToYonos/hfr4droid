package info.toyonos.hfr4droid.util.asynctask;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.HFR4droidMultiListActivity;

import java.util.List;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

public abstract class PreLoadingAsyncTask<E, P, DS> extends DataRetrieverAsyncTask<E, P>
{
	private boolean loadPreviousPage = false;
	
	public PreLoadingAsyncTask(HFR4droidMultiListActivity<DS> context)
	{
		super(context);
	}
	
	public PreLoadingAsyncTask(HFR4droidMultiListActivity<DS> context, boolean loadPreviousPage)
	{
		super(context);
		this.loadPreviousPage = loadPreviousPage;
	}

	@Override
	protected void onPreExecute() {}
	
	protected abstract DS getDatasource(List<E> elements);
	
	protected abstract void loadPreviousPage();
	
	protected void init(View v, DS datasource) {}
	
	@Override
	protected List<E> doInBackground(P... params)
	{
		Log.i(HFR4droidApplication.TAG, context.getString(R.string.preloading_begin, getPageNumber()));
		return super.doInBackground(params);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onPostExecuteSameActivity(List<E> elements) throws ClassCastException
	{
		DS datasource = getDatasource(elements);
		View view = ((HFR4droidMultiListActivity<DS>) context).buildView(datasource);
		init(view, datasource);
		
		if (pageNumber > context.getCurrentPageNumber())
		{
			((HFR4droidMultiListActivity<DS>) context).insertAfter(datasource, view);
		}
		else if (pageNumber < context.getCurrentPageNumber())
		{
			try
			{
				((HFR4droidMultiListActivity<DS>) context).insertBefore(datasource, view);
			}
			catch (UnsupportedOperationException e)
			{
				// Ne devrait pas arriver mais au cas où on ne fait rien et on log
				Log.e(HFR4droidApplication.TAG, "Could not insert the new view : " + e.getMessage(), e);
			}
		}

		Log.i(HFR4droidApplication.TAG, context.getString(R.string.preloading_ok, getPageNumber()));
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(50);
		
		// On charge aussi la page n-2, typiquement quand on arrive directement sur une page qui n'est pas la page 1
		if (loadPreviousPage) loadPreviousPage();
	}

	@Override
	protected void onPostExecuteOtherActivity(List<E> elements)
	{
		throw new UnsupportedOperationException("You can't use " + getClass().getSimpleName() + " when sameActivity is false");
	}
}