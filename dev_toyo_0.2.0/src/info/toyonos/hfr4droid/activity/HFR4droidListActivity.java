package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * <p>Equivalent de la <code>ListActivity</code> mais héritant de 
 * <code>HFR4droidActivity</code></p>
 * 
 * @author ToYonos
 *
 * @param <E> l'élément affiché dans la liste
 */
public abstract class HFR4droidListActivity<E> extends HFR4droidActivity
{	
	protected ArrayAdapter<E> adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	protected ListView getListView()
	{
		return (ListView) findViewById(R.id.MainList);
	}
}
