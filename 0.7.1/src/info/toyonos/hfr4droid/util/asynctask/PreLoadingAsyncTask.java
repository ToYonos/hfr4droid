package info.toyonos.hfr4droid.util.asynctask;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.HFR4droidMultiListActivity;
import info.toyonos.hfr4droid.util.view.DragableSpace;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

public abstract class PreLoadingAsyncTask<E, P, DS> extends DataRetrieverAsyncTask<E, P>
{
	public final static PreLoadingCompleteListener[] PRELOADING_COMPLETE_LISTENERS = new PreLoadingCompleteListener[]
	{
		new PreLoadingCompleteListener()
		{
			public void onPreLoadingComplete(PreLoadingAsyncTask<?, ?, ?> currentTask)
			{
				Vibrator v = (Vibrator) currentTask.getContext().getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(50);
			}
		},
		new PreLoadingCompleteListener()
		{
			public void onPreLoadingComplete(final PreLoadingAsyncTask<?, ?, ?> currentTask)
			{
				final HFR4droidMultiListActivity<?> context = (HFR4droidMultiListActivity<?>) currentTask.getContext();
				new Timer().schedule(new TimerTask()
				{
					public void run()
					{
						context.runOnUiThread(new Runnable()
						{
							public void run()
							{
								DragableSpace space = context.getSpace();
								int targetScreen = currentTask.getPageNumber() > context.getCurrentPageNumber() ? space.getCurrentScreen() + 1 : space.getCurrentScreen() - 1;
								space.snapToScreen(targetScreen);
							}
						});
					}
				}, 500);
			}
		}
	};
	
	private int[] othersPageNumbers = null;
	private PreLoadingCompleteListener preLoadingCompleteListener = null;
	private boolean pageChangeRequested = false;
	
	public PreLoadingAsyncTask(HFR4droidMultiListActivity<DS> context)
	{
		super(context);
		setPreLoadingCompleteListener(context.getPreloadingCallback());
	}
	
	public PreLoadingAsyncTask(HFR4droidMultiListActivity<DS> context, int... othersPageNumbers)
	{
		this(context);
		this.othersPageNumbers = othersPageNumbers;
	}

	@Override
	protected void onPreExecute() {}
	
	protected abstract DS getDatasource(List<E> elements);
	
	protected abstract void loadAnotherPage(int pageNumber);
	
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
		if (preLoadingCompleteListener != null && pageChangeRequested) preLoadingCompleteListener.onPreLoadingComplete(this);
		
		// On charge aussi la page n-2, typiquement quand on arrive directement sur une page qui n'est pas la page 1
		if (othersPageNumbers != null)
		{
			for (int pageNumber : othersPageNumbers)
			{
				loadAnotherPage(pageNumber);
			}
		}
	}

	@Override
	protected void onPostExecuteOtherActivity(List<E> elements)
	{
		throw new UnsupportedOperationException("You can't use " + getClass().getSimpleName() + " when sameActivity is false");
	}
	
	public void setPreLoadingCompleteListener(PreLoadingCompleteListener preLoadingCompleteListener)
	{
		this.preLoadingCompleteListener = preLoadingCompleteListener;
	}

	public boolean isPageChangeRequested()
	{
		return pageChangeRequested;
	}
	
	public void setPageChangeRequested(boolean pageChangeRequested)
	{
		this.pageChangeRequested = pageChangeRequested;
	}

	public interface PreLoadingCompleteListener 
	{
		public void onPreLoadingComplete(PreLoadingAsyncTask<?, ?, ?> currentTask);
	}
}