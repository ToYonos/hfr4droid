package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.SubCategory;
import info.toyonos.hfr4droid.core.bean.Theme;
import info.toyonos.hfr4droid.core.bean.SubCategory.ToStringType;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.core.data.HFRDataRetriever;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * <p>Activity listant les catégories</p>
 * 
 * @author ToYonos
 *
 */
public class CategoriesActivity extends HFR4droidListActivity<Category>
{
	private AlertDialog infoDialog;
	protected boolean isCatsLoaded;
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
		if (bundle != null && bundle.getSerializable("cats") != null)
		{
			cats = (List<Category>) bundle.getSerializable("cats");
		}
		else
		{
			int welcomeScreen = getWelcomeScreen();
			if (welcomeScreen > 0 && isLoggedIn())
			{
				isCatsLoaded = false;
				loadTopics(Category.ALL_CATS, TopicType.fromInt(welcomeScreen), false);
			}
			else
			{
				loadCats();
			}
		}

		final ListView lv = getListView();
		adapter = new CategoryAdapter(this, R.layout.category, R.id.ItemContent, cats);
		lv.setAdapter(adapter);

		gestureDetector = new GestureDetector(new SimpleNavOnGestureListener()
		{
			@Override
			protected void onLeftToRight(MotionEvent e1, MotionEvent e2){}

			@Override
			protected void onRightToLeft(MotionEvent e1, MotionEvent e2){}

			@Override
			public boolean onDoubleTap(MotionEvent me)
			{
				final int position = lv.pointToPosition((int) me.getX(), (int) me.getY());
				final Category cat = (Category) lv.getItemAtPosition(position);
				if (cat == null) return false;
				
				final boolean isCatExpanded = expandedCats.contains(cat);
				if (!isCatExpanded)
				{
					try
					{
						final boolean isSubCatsLoaded = CategoriesActivity.this.getDataRetriever().isSubCatsLoaded(cat);
						final ProgressDialog progressDialog = new ProgressDialog(CategoriesActivity.this);
						progressDialog.setTitle(cat.toString());
						progressDialog.setMessage(getString(R.string.getting_subcats));
						progressDialog.setIndeterminate(true);
						new AsyncTask<Category, Void, List<SubCategory>>()
						{
							@Override
							protected void onPreExecute() 
							{
								if (!isSubCatsLoaded)
								{
									progressDialog.setCancelable(true);
									progressDialog.setOnCancelListener(new OnCancelListener()
									{
										public void onCancel(DialogInterface dialog)
										{
											cancel(true);
										}
									});
									progressDialog.show();
								}
							}

							@Override
							protected List<SubCategory> doInBackground(Category... cat)
							{
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
		});

		lv.setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				return gestureDetector.onTouchEvent(event);
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
	public boolean onContextItemSelected(MenuItem aItem)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Category currentCat = (Category) getListView().getAdapter().getItem(menuInfo.position);

		switch (aItem.getItemId())
		{
			case R.id.MenuDrapeauxAll:
				loadTopics(currentCat, TopicType.ALL, 1, false);
				return true;
	
			case R.id.MenuDrapeauxCyan:
				loadTopics(currentCat, TopicType.CYAN, false);
				return true;
	
			case R.id.MenuDrapeauxRouges:
				loadTopics(currentCat, TopicType.ROUGE, false);
				return true;
	
			case R.id.MenuDrapeauxFavoris:
				loadTopics(currentCat, TopicType.FAVORI, false);
				return true;    			
	
			default:
				return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.common, menu);
		inflater.inflate(R.menu.categories, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean result = super.onOptionsItemSelected(item);
		if (!result)
		{
			switch (item.getItemId())
			{
				case R.id.MenuInfo :
					infoDialog.show();
					return true;
	
				case R.id.MenuQuit :
					finish();
					return true;
	
				default:
					return false;
			}
		}
		else
		{
			return true;
		}
	}

	public boolean isCatsLoaded()
	{
		return isCatsLoaded;
	}

	private AlertDialog getInfoDialog()
	{
		String title = getString(R.string.app_name);

		PackageInfo packageInfo;
		try
		{
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			title += " - V." + packageInfo.versionName;
		}
		catch (NameNotFoundException e) {}

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
		webView.loadData(infoContent, "text/html", "UTF-8");
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
	protected void setTitle(){}

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