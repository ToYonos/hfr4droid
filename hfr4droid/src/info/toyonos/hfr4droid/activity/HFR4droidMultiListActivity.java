package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.util.asynctask.PreLoadingAsyncTask;
import info.toyonos.hfr4droid.util.view.DragableSpace;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * <p>Activity permettant de gérer n écrans via le composant <code>DragableSpace</code>.
 * Chaque écran correspond à une liste alimentée par une source de données de type <code>E</code></p>
 * 
 * @author ToYonos
 *
 * @param <E> le type de source de données
 */
public abstract class HFR4droidMultiListActivity<DS> extends HFR4droidActivity
{	
	protected DragableSpace space = null;
	private DS dataSources[] = null;
	private View views[] = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		dataSources = (DS[]) new Object[3];
		views = new View[3];
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		removeViews();
		dataSources = null;
	}
	
	public DragableSpace getSpace()
	{
		return space;
	}

	protected int getCurrentIndex()
	{
		return space.getCurrentScreen();
	}

	protected DS getDatasource()
	{
		return dataSources == null ? null : dataSources[getCurrentIndex()];
	}
	
	protected DS getDatasource(int index)
	{
		return dataSources == null ? null : dataSources[index];
	}
	
	protected DS setDatasource(DS dataSource)
	{
		return dataSources[getCurrentIndex()] = dataSource;
	}
	
	protected View getView()
	{
		return views == null ? null : views[getCurrentIndex()];
	}
	
	protected boolean isViewsHasToBeRestore()
	{
		return views == null;
	}
	
	protected View getView(int index)
	{
		return  views == null ? null : views[index];
	}
	
	protected void setView(int index, View view)
	{
		views[index] = view;
	}
	
	protected void setView(View view)
	{
		if (view == null) return;

		View oldView = views[getCurrentIndex()];
		if (oldView != null)
		{
			space.removeViewAt(getCurrentIndex());
		}
		space.addView(view, getCurrentIndex());

		destroyView(views[getCurrentIndex()]);
		views[getCurrentIndex()] = null;
		views[getCurrentIndex()] = view;
	}
	
	protected boolean isNextPageLoaded()
	{
		int currentIndex = space.getCurrentScreen();
		return
				currentIndex < 2 && 
				dataSources != null && dataSources[currentIndex + 1] != null &&
				views != null && views[currentIndex + 1] != null &&
				space.getChildAt(currentIndex + 1) != null;
	}
	
	protected boolean isPreviousPageLoaded()
	{
		int currentIndex = space.getCurrentScreen();
		return
				currentIndex > 0 && 
				dataSources != null && dataSources[currentIndex - 1] != null &&
				views != null && views[currentIndex - 1] != null &&
				space.getChildAt(currentIndex - 1) != null;
	}

	protected void displayPreloadingToast(PreLoadingAsyncTask<?, ?, ?> task)
	{
		if (task != null && task.getStatus() == Status.RUNNING)
		{
			task.setPageChangeRequested(true);
			Toast.makeText(HFR4droidMultiListActivity.this, getString(R.string.page_loading, task.getPageNumber()), Toast.LENGTH_SHORT).show();
		}
	}

	protected void reset()
	{
		View current = space.getChildAt(getCurrentIndex());
		space.removeAllViews();
		space.addView(current);
		if (getCurrentIndex() != 0)
		{
			dataSources[0] = dataSources[getCurrentIndex()];
			views[0] = views[getCurrentIndex()];
			space.setToScreen(0);
		}
		destroyView(views[1]);
		destroyView(views[2]);
		views[1] = views[2] = null;
		dataSources[1] = dataSources[2] = null;
	}
	
	protected void removeViews()
	{
		if (views != null)
		{
			for (int i = 0; i < 3; i++)
			{
				destroyView(views[i]);
			}
		}
		views = null;
		space.removeAllViews();
	}
	
	protected void removeView(int index)
	{
		if (views != null && views.length > index)
		{
			destroyView(views[index]);
			views[index] = null;
			space.removeViewAt(index);
		}
	}
	
	protected void restoreViews()
	{
		views = new View[3];
		for (int i = 0; i < 3; i++)
		{
			restoreView(i);
		}
		space.setToScreen(space.getCurrentScreen());
	}
	
	protected void restoreView(int index)
	{
		if (dataSources[index] != null)
		{
			View v = buildView(dataSources[index]);
			views[index] = v;
			space.addView(v, index);
		}
	}
	
	abstract public View buildView(DS datasource);
	
	public void insertAfter(DS dataSource, View view)
	{
		if (dataSource == null || view != null) return;
		
		switch (getCurrentIndex())
		{
			case 0:
				dataSources[1] = dataSource;
				views[1] = view;
				space.addView(view);
				break;
			
			case 1:
				dataSources[2] = dataSource;
				views[2] = view;
				space.addView(view);
				break;

			case 2:
				dataSources[0] = null;
				dataSources[0] = dataSources[1];
				dataSources[1] = dataSources[2];
				dataSources[2] = dataSource;
				
				destroyView(views[0]);
				views[0] = null;
				views[0] = views[1];
				views[1] = views[2];
				views[2] = view;
				space.removeViewAt(0);
				space.addView(view);
				space.setToScreen(1);
				break;
				
			default:
				throw new IndexOutOfBoundsException(getCurrentIndex() + " is an invalid index for the DragableSpace view");
		}
	}
	
	public void insertBefore(DS dataSource, View view)
	{
		if (dataSource == null || view != null) return;
		
		switch (getCurrentIndex())
		{
			case 0:
				dataSources[2] = null;
				dataSources[2] = dataSources[1];
				dataSources[1] = dataSources[0];
				dataSources[0] = dataSource;
				
				destroyView(views[2]);
				views[2] = null;
				views[2] = views[1];
				views[1] = views[0];
				views[0] = view;
				if (space.getChildAt(2) != null) space.removeViewAt(2);
				space.addView(view, 0);
				space.setToScreen(1);
				break;

			default:
				throw new UnsupportedOperationException("You can't insert before with the index " + getCurrentIndex());
		}
	}
	
	public void destroyView(View v)	{}
}