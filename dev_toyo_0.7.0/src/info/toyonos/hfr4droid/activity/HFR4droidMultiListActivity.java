package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.util.view.DragableSpace;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * <p>Activity permettant de gérer n écrans via le composant <code>DragableSpace</code>.
 * Chaque écran correspond à une liste alimentée par une source de données de type <code>E</code></p>
 * 
 * @author ToYonos
 *
 * @param <E> le type de source de données
 */
public abstract class HFR4droidMultiListActivity<E> extends HFR4droidActivity
{
	protected DragableSpace space = null;
	private E dataSources[] = null;
	private View views[] = null;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		dataSources = (E[]) new Object[3];
		views = new View[3];
	}

	protected ListView getListView()
	{
		return (ListView) getView().findViewById(R.id.MainList);
	}
	
	protected int getCurrentIndex()
	{
		return space.getCurrentScreen();
	}

	protected E getDatasource()
	{
		return dataSources[getCurrentIndex()];
	}
	
	protected E getDatasource(int index)
	{
		return dataSources[index];
	}
	
	protected E setDatasource(E dataSource)
	{
		return dataSources[0] = dataSource;
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
		views[0] = view;
	}
	
	protected void insertAfter(E dataSource, View view)
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
	
	protected void insertBefore(E dataSource, View view)
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
				space.removeViewAt(2);
				space.addView(view, 0);
				space.setToScreen(1);
				break;

			default:
				throw new UnsupportedOperationException("You can't insert before with the index " + getCurrentIndex());
		}
	}
}