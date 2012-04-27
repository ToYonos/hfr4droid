package info.toyonos.hfr4droid.util.asynctask;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.activity.HFR4droidMultiListActivity;
import info.toyonos.hfr4droid.activity.TopicsActivity;

import java.util.List;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

public abstract class PreloadingAsyncTask<E, P, DS> extends DataRetrieverAsyncTask<E, P>
{
	private boolean loadPreviousPage = false;
	
	public PreloadingAsyncTask(HFR4droidMultiListActivity<DS> context)
	{
		super(context);
	}
	
	public PreloadingAsyncTask(HFR4droidMultiListActivity<DS> context, boolean loadPreviousPage)
	{
		super(context);
		this.loadPreviousPage = loadPreviousPage;
	}

	@Override
	protected void onPreExecute() {}
	
	protected abstract View getView();
	
	protected abstract DS getDatasource();
	
	// TODO faire mieux que ça

	@SuppressWarnings("unchecked")
	@Override
	protected void onPostExecuteSameActivity(List<E> elements) throws ClassCastException
	{
		
		/*LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		PullToRefreshListView topicsView = (PullToRefreshListView) inflater.inflate(R.layout.topics_dragable, null);
		ArrayAdapter<Topic> adapter = new TopicAdapter(TopicsActivity.this, R.layout.topic, R.id.ItemContent, topics);
		topicsView.setAdapter(adapter);
		
		applyTheme(currentTheme, topicsView, true);
		onCreateInit(topics, topicsView, getPageNumber());*/

		DS datasource = getDatasource();
		View view = getView();
		
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

		Log.d(HFR4droidApplication.TAG, "Page " + getPageNumber() + " loaded");
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(50);
		
		// On charge aussi la page n-2, typiquement quand on arrive directement sur une page qui n'est pas la page 1
		if (loadPreviousPage)
		{
			//preLoadingAsyncTask = new PreLoadingTopicsAsyncTask(TopicsActivity.this);
			//preLoadingTopicsAsyncTask.execute(context.getCurrentPageNumber() - 1, cat);
			// TODO méthode abstraite
		}
	}

	@Override
	protected void onPostExecuteOtherActivity(List<E> elements)
	{
		throw new UnsupportedOperationException("You can't use " + getClass().getSimpleName() + " when sameActivity is false");
	}
}