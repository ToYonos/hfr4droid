package info.toyonos.hfr4droid.common.activity;

import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.core.bean.Category;
import info.toyonos.hfr4droid.common.core.bean.SubCategory;
import info.toyonos.hfr4droid.common.core.bean.SubCategory.ToStringType;
import info.toyonos.hfr4droid.common.core.bean.Theme;
import info.toyonos.hfr4droid.common.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.common.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.common.core.data.HFRDataRetriever;
import info.toyonos.hfr4droid.common.util.asynctask.ProgressDialogAsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * <p>Activity listant les catégories</p>
 * 
 * @author ToYonos
 *
 */
public class CategoriesActivity extends HFR4droidListActivity<Category>
{
	protected AlertDialog infoDialog;
	private boolean isCatsLoaded;
	private GestureDetector gestureDetector;
	protected List<Category> expandedCats  = new ArrayList<Category>();

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories);
		applyTheme(currentTheme);
		infoDialog = getInfoDialog();
		isCatsLoaded = true;

		List<Category> cats  = new ArrayList<Category>();
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null && bundle.getSerializable("topics") != null)
		{
			// Cas spécial, on vient de SplashActivity qui veut afficher des topics
			// et on veut garder l'ordre d'ouverture des activitys (cats -> topics -> posts)
			// => On passe furtivement par ici avant d'ouvrir PostsActivity
			isCatsLoaded = false;
			Intent intent = new Intent(CategoriesActivity.this, TopicsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		else if (bundle != null && bundle.getSerializable("cats") != null)
		{
			cats = (List<Category>) bundle.getSerializable("cats");
		}
		else
		{
			loadCats();
		}

		final ListView lv = getListView();
		adapter = new CategoryAdapter(this, R.layout.category, R.id.ItemContent, cats);
		lv.setAdapter(adapter);

		gestureDetector = new GestureDetector(new SimpleOnGestureListener()
		{
			@Override
			public boolean onDoubleTap(MotionEvent me)
			{
				final int position = lv.pointToPosition((int) me.getX(), (int) me.getY());
				final Category cat = (Category) lv.getItemAtPosition(position);
				if (cat == null || cat instanceof SubCategory) return false;
				
				final boolean isCatExpanded = expandedCats.contains(cat);
				if (!isCatExpanded)
				{
					try
					{
						final boolean isSubCatsLoaded = CategoriesActivity.this.getDataRetriever().isSubCatsLoaded(cat);
						new ProgressDialogAsyncTask<Category, Void, List<SubCategory>>(CategoriesActivity.this)
						{
							@Override
							protected void onPreExecute() 
							{
								if (!isSubCatsLoaded)
								{
									super.onPreExecute();
									progressDialog.setTitle(cat.toString());
									progressDialog.setMessage(getString(R.string.getting_subcats));
									progressDialog.show();
								}
							}

							@Override
							protected List<SubCategory> doInBackground(Category... cat)
							{
								setThreadId();
								List<SubCategory> subCats = null;
								try
								{
									subCats = CategoriesActivity.this.getDataRetriever().getSubCats(cat[0]);
								} 
								catch (DataRetrieverException e)
								{
									error(e, true, true);
								}
								return subCats;
							}

							@Override
							protected void onPostExecute(List<SubCategory> subCats)
							{
								if (!isSubCatsLoaded) progressDialog.dismiss();
								if (subCats != null)
								{
									int i = position + 1;
									for (SubCategory subCat : subCats)
									{
										adapter.insert(subCat, i++);				
									}
									expandedCats.add(cat);
									adapter.notifyDataSetChanged();		
								}
							}
						}.execute(cat);
					}
					catch (DataRetrieverException e)
					{
						error(e, true);
					}
				}
				else
				{
					for(int i = position + 1;;)
					{
						if (i >= adapter.getCount()) break;
						Category currentCat = adapter.getItem(i);
						if (currentCat instanceof SubCategory)
						{
							adapter.remove(currentCat);
						}
						else
						{
							break;
						}
					}
					expandedCats.remove(cat);
					adapter.notifyDataSetChanged();
				}
				return true;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent me)
			{
				int position = lv.pointToPosition((int) me.getX(), (int) me.getY());
				Category cat = (Category) lv.getItemAtPosition(position);
				return openCategory(cat);
			}
		});

		lv.setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				return gestureDetector.onTouchEvent(event);
			}
		});

		lv.setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_UP)
				{
					if (lv.getSelectedItem() != null)
					{
						Category cat = (Category) lv.getSelectedItem();
						return openCategory(cat);
					}
				}
				return false;
			}
		});

		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
			{
				Category currentCat = (Category) getListView().getAdapter().getItem(((AdapterContextMenuInfo)menuInfo).position);
				if (!isLoggedIn() || isMpsCat(currentCat)) return;

				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.drapeaux_simple, menu);
				menu.setHeaderTitle(R.string.menu_drapeaux);
				if (isAllCatsCat(currentCat)) menu.removeItem(R.id.MenuDrapeauxAll);
			}
		});

		startMpTimerCheckService();
		startMpCheckService();
	}
	
	private boolean openCategory(Category cat)
	{
		if (cat == null) return false;

		if (isLoggedIn() && !isMpsCat(cat))
		{
			TopicType type = TopicType.fromInt(getTypeDrapeau());
			if (isAllCatsCat(cat) && type == TopicType.ALL)
			{
				Toast.makeText(CategoriesActivity.this, R.string.warning_allcats_topicall, Toast.LENGTH_LONG).show();
			}
			else
			{
				loadTopics(cat, type, false);
			}
		}
		else
		{
			loadTopics(cat, TopicType.ALL, 1, false);
		}
		return true;
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		if (!isCatsLoaded)
		{
			loadCats();
			isCatsLoaded = true;
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (infoDialog != null) infoDialog.dismiss();
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem aItem)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Category currentCat = (Category) getListView().getAdapter().getItem(menuInfo.position);

		if (aItem.getItemId() == R.id.MenuDrapeauxAll)
		{
			loadTopics(currentCat, TopicType.ALL, 1, false);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuDrapeauxCyan)
		{
			loadTopics(currentCat, TopicType.CYAN, false);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuDrapeauxRouges)
		{
			loadTopics(currentCat, TopicType.ROUGE, false);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuDrapeauxFavoris)
		{
			loadTopics(currentCat, TopicType.FAVORI, false);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.categories, menu);
		getSupportMenuInflater().inflate(R.menu.common, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean result = super.onOptionsItemSelected(item);
		if (!result)
		{
			if (item.getItemId() == R.id.MenuInfo)
			{
				infoDialog.show();
				return true;
			}
			else if (item.getItemId() == R.id.MenuQuit)
			{
				finish();
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return true;
		}
	}

	private AlertDialog getInfoDialog()
	{
		String title = getString(R.string.app_name);

		title += " - V." + getVersionName();
		String infoContent = null;
		InputStream is = null;
		try
		{
			is = getResources().openRawResource(R.raw.about);
			infoContent = HFRDataRetriever.streamToString(is, true);
		}
		catch (IOException e)
		{
			infoContent = getString(R.string.error_about);
		}

		AlertDialog.Builder info = new AlertDialog.Builder(this);
		info.setIcon(R.drawable.icon);
		info.setTitle(title); 
		WebView webView = new WebView(this);
		WebSettings settings = webView.getSettings();
		settings.setDefaultTextEncodingName("UTF-8");
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.loadDataWithBaseURL("", infoContent, "text/html", "UTF-8", null);
		info.setView(webView);
		info.setNeutralButton(R.string.button_ok, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which){}
		}); 

		AlertDialog infoDialog = info.create();
		// Sorte de pre-rendering pour éviter l'affichage en 2 temps
		infoDialog.show();
		infoDialog.hide();
		return infoDialog;
	}

	@Override
	protected void setTitle()
	{
		if (isLoggedIn())
		{
			getSupportActionBar().setTitle(getString(R.string.welcome_message, getHFR4droidApplication().getAuthentication().getUser()));
		}
		else
		{
			getSupportActionBar().setTitle(R.string.app_name);
		}
	}

	@Override
	protected void applyTheme(Theme theme)
	{
		ListView mainList = getListView();
		((LinearLayout) mainList.getParent()).setBackgroundColor(theme.getListBackgroundColor());
		mainList.setDivider(new ColorDrawable(theme.getListDividerColor()));
		mainList.setDividerHeight(1);
		mainList.setCacheColorHint(theme.getListBackgroundColor());
		mainList.setSelector(getKeyByTheme(getThemeKey(), R.drawable.class, "list_selector"));
	}

	@Override
	protected void reloadPage()
	{
		loadCats();
	}

	@Override
	protected void redrawPage()
	{
		adapter.notifyDataSetChanged();
	}

	public void refreshCats(List<Category> cats)
	{
		adapter.clear();
		for (Category c : cats)
		{
			if (!isLoggedIn() && (isMpsCat(c) || isAllCatsCat(c))) continue;
			adapter.add(c);
		}
		adapter.notifyDataSetChanged();
	}

	/* Classes internes */

	private class CategoryAdapter extends ArrayAdapter<Category>
	{
		private List<Category> cats;

		public CategoryAdapter(Context context, int resource, int textViewResourceId, List<Category> cats)
		{
			super(context, resource, textViewResourceId, cats);
			if (!cats.isEmpty() && !isLoggedIn())
			{
				cats.remove(Category.MPS_CAT);
				cats.remove(Category.ALL_CATS);
			}
			this.cats = cats;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = super.getView(position, convertView, parent);

			final Category c = cats.get(position);
			boolean isSubCat = c instanceof SubCategory;
			
			TextView text1 = (TextView) v.findViewById(R.id.ItemContent);
			text1.setTextSize(getTextSize(15));
			float scale = getResources().getDisplayMetrics().density;
			text1.setPadding(isSubCat ? (int) (10 * scale + 0.5f) : 0, 0, 0, 0);
			try
			{
				text1.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, isSubCat ? "item2" : "item"))));
				((TextView) v.findViewById(R.id.ItemArrow)).setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, isSubCat ? "item2" : "item"))));
			}
			catch (Exception e)
			{
				error(e);
			}
			
			String newName = isSubCat ?	((SubCategory) c).toString(ToStringType.SUBCAT) :
			isMpsCat(c) || isAllCatsCat(c) || isModoCat(c) ? "<b>" + c.getName() + "</b>" : c.toString();
			//newName = isMpsCat(c) && c.getName().matches(".*?nouveaux? messages?.*?") ? "<font color=\"red\">" + newName + "</font>" : newName;
			text1.setText(Html.fromHtml(newName));
			return v;
		}
	}
}