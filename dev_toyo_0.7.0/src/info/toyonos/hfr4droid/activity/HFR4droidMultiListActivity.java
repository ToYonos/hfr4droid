package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.util.view.DragableSpace;
import android.os.Bundle;
import android.view.View;

/**
 * <p>Activity permettant de g�rer n �crans via le composant <code>DragableSpace</code>.
 * Chaque �cran correspond � une liste aliment�e par une source de donn�es de type <code>E</code></p>
 * 
 * @author ToYonos
 *
 * @param <E> le type de source de donn�es
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
	
	protected int getCurrentIndex()
	{
		return space.getCurrentScreen();
	}

	protected DS getDatasource()
	{
		return dataSources[getCurrentIndex()];
	}
	
	protected DS getDatasource(int index)
	{
		return dataSources[index];
	}
	
	protected DS setDatasource(DS dataSource)
	{
		return dataSources[getCurrentIndex()] = dataSource;
	}
	
	protected View getView()
	{
		return views[getCurrentIndex()];
	}
	
	protected View getView(int index)
	{
		return views[index];
	}
	
	protected void setView(View view)
	{
		View oldView = views[getCurrentIndex()];
		if (oldView != null)
		{
			space.removeViewAt(getCurrentIndex());
		}
		space.addView(view);
		views[getCurrentIndex()] = view;
	}
	
	public void insertAfter(DS dataSource, View view)
	{
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
				dataSources[0] = dataSources[1];
				dataSources[1] = dataSources[2];
				dataSources[2] = dataSource;
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
		switch (getCurrentIndex())
		{
			case 0:
				dataSources[2] = dataSources[1];
				dataSources[1] = dataSources[0];
				dataSources[0] = dataSource;
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
}