package info.toyonos.hfr4droid.common.activity;

import info.toyonos.hfr4droid.common.HFR4droidException;
import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.core.bean.Category;
import info.toyonos.hfr4droid.common.core.bean.SubCategory;
import info.toyonos.hfr4droid.common.core.bean.SubCategory.ToStringType;
import info.toyonos.hfr4droid.common.core.bean.Theme;
import info.toyonos.hfr4droid.common.core.bean.Topic;
import info.toyonos.hfr4droid.common.core.bean.Topic.TopicStatus;
import info.toyonos.hfr4droid.common.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.common.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.common.core.message.HFRMessageResponse;
import info.toyonos.hfr4droid.common.util.asynctask.MessageResponseAsyncTask;
import info.toyonos.hfr4droid.common.util.asynctask.PreLoadingAsyncTask;
import info.toyonos.hfr4droid.common.util.dialog.PageNumberDialog;
import info.toyonos.hfr4droid.common.util.listener.OnScreenChangeListener;
import info.toyonos.hfr4droid.common.util.view.DragableSpace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

/**
 * <p>Activity listant les topics</p>
 * 
 * @author ToYonos
 *
 */

public class TopicsActivity extends HFR4droidMultiListActivity<ArrayAdapter<Topic>>
{
	private Category cat = null;
	private TopicType type = null;
	private PreLoadingTopicsAsyncTask preLoadingTopicsAsyncTask = null;
	
	// Dans le cas ou aucun élément n'est récupéré, on utilise la sauvegarde 
	private Category previousCat = null;
	private TopicType previousType = null;
	
	protected boolean moreTopicInfos;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topics);
		space = (DragableSpace) findViewById(R.id.Space);
		space.setSwipeSensibility(getHFR4droidApplication().getSwipe());
		
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		PullToRefreshListView topicsView = (PullToRefreshListView) inflater.inflate(R.layout.topics_dragable, null);
		setView(topicsView);

		moreTopicInfos = isTopicMoreInfosEnable();
		applyTheme(currentTheme);

		Bundle bundle = this.getIntent().getExtras();
		boolean allCats = bundle == null ? false : bundle.getBoolean("allCats", false);
		if (type == null) type = bundle != null && bundle.getSerializable("topicType") != null ? (TopicType) bundle.getSerializable("topicType") : TopicType.ALL;

		List<Topic> topics = new ArrayList<Topic>();
		if (bundle != null && bundle.getSerializable("topics") != null)
		{
			topics = (List<Topic>) bundle.getSerializable("topics");
			if (allCats)
			{
				cat = Category.ALL_CATS;
				topics = addCats(topics);
			}
			else if (topics.size() > 0)
			{
				cat = topics.get(0).getCategory();
			}
			preloadTopics();
		}
		else
		{
			if (bundle != null && bundle.getSerializable("cat") != null)
			{
				cat = (Category) bundle.getSerializable("cat");
			}
			if (cat != null) loadTopics(cat, type, currentPageNumber);
		}
		
		ArrayAdapter<Topic> adapter = setDatasource(new TopicAdapter(this, R.layout.topic, R.id.ItemContent, topics));
		PullToRefreshListView lv = (PullToRefreshListView) getListView();
		lv.setAdapter(adapter);

		// Listener pour le changement de view dans le composant DragableSpace
		space.setOnScreenChangeListener(new OnScreenChangeListener()
		{
			public void onScreenChange(int oldIndex, int newIndex)
			{
				if (oldIndex == newIndex) return;
				
				preLoadingTopicsAsyncTask.cancel(true);
				boolean forward = oldIndex < newIndex;
				int targetPageNumber = -1;
				if (forward)
				{
					currentPageNumber++;
					if (newIndex != 1 || getView(2) == null || getDatasource(2) == null)
					{
						targetPageNumber = currentPageNumber + 1;
					}
				}
				else
				{
					currentPageNumber--;
					if (currentPageNumber != 1) targetPageNumber = currentPageNumber - 1;
				}
				
				if (targetPageNumber != -1)
				{
					preLoadingTopicsAsyncTask = new PreLoadingTopicsAsyncTask(TopicsActivity.this);
					preLoadingTopicsAsyncTask.execute(targetPageNumber, cat);
				}

				setTitle();
				supportInvalidateOptionsMenu();
			}

			public void onFailForward()
			{
				displayPreloadingToast(preLoadingTopicsAsyncTask);
			}

			public void onFailRearward()
			{
				if (currentPageNumber == 1)
				{
					reloadPage();
				}
				else
				{
					displayPreloadingToast(preLoadingTopicsAsyncTask);
				}
			}
		});

		if (Category.MPS_CAT.equals(cat)) clearNotifications();
		onCreateInit(lv, currentPageNumber);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (preLoadingTopicsAsyncTask != null) preLoadingTopicsAsyncTask.cancel(true);
	}
	
	private void onCreateInit(final PullToRefreshListView lv, int pageNumber)
	{
		setRefreshHeader();
		
        lv.setOnRefreshListener(new OnRefreshListener()
        {
            public void onRefresh()
            {
            	reloadPage(true, false);
            }
        });

		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> a, View v, int position, long id)
			{	
				Topic topic = (Topic) lv.getItemAtPosition(position);
				if (topic != null)
				{
					if (topic.getId() != -1)
					{
						int page = topic.getLastReadPage() != -1 ? topic.getLastReadPage() : 1;
						loadPosts(topic, page, false);
					}
					else
					{
						previousCat = cat;
						cat = topic.getCategory();
						loadTopics(topic.getCategory(), type);
					}
				}
				else
				{
					lv.onRefresh();
				}
			}
		});

		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
			{
				MenuInflater inflater = getMenuInflater();
				Topic currentTopic = (Topic) getListView().getAdapter().getItem(((AdapterContextMenuInfo)menuInfo).position);
				if (currentTopic.getId() != -1)
				{
					inflater.inflate(R.menu.topic, menu);
					menu.setHeaderTitle(currentTopic.getName());
					if (!isLoggedIn() || currentTopic.getLastReadPage() == -1) menu.removeItem(R.id.MenuNavLastReadPage);
					if (!isLoggedIn() || !isMpsCat()) menu.removeItem(R.id.MenuNavSetUnread);
					if (!isLoggedIn() || !isMpsCat()) menu.removeItem(R.id.MenuNavDeleteMP);
					if (!isLoggedIn() || isMpsCat()) menu.removeItem(R.id.MenuNavUnflag);
					if (!isLoggedIn() || currentTopic.getStatus() == TopicStatus.LOCKED) menu.removeItem(R.id.MenuNavNewPost);
				}
				else
				{
					inflater.inflate(R.menu.drapeaux_simple, menu);
					menu.setHeaderTitle(R.string.menu_drapeaux);
				}
			}
		});
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem aItem)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Topic currentTopic = menuInfo != null ? (Topic) getListView().getAdapter().getItem(menuInfo.position) : null;

		if (aItem.getItemId() == R.id.MenuNavLastReadPage)
		{
			loadPosts(currentTopic, currentTopic.getLastReadPage(), false);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuNavFirstPage)
		{
			loadPosts(currentTopic, 1, false);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuNavUserPage)
		{
			new PageNumberDialog(this, -1, currentTopic.getNbPages())
			{
				protected void onValidate(int pageNumber)
				{
					loadPosts(currentTopic, pageNumber, false);
				}
			}.show();
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuNavLastPage)
		{
			loadPosts(currentTopic, currentTopic.getNbPages(), false);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuDrapeauxAll)
		{
			cat = currentTopic.getCategory();
			previousType = type;
			type = TopicType.ALL;
			if (currentPageNumber < 1) currentPageNumber = 1;
			reloadPage(false, true);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuDrapeauxCyan)
		{
			cat = currentTopic.getCategory();
			previousType = type;
			type = TopicType.CYAN;
			reloadPage(false, true);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuDrapeauxRouges)
		{
			cat = currentTopic.getCategory();
			previousType = type;
			type = TopicType.ROUGE;
			reloadPage(false, true);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuDrapeauxFavoris)
		{
			cat = currentTopic.getCategory();
			previousType = type;
			type = TopicType.FAVORI;
			reloadPage(false, true);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuNavNewPost)
		{
			Intent intent = new Intent(TopicsActivity.this, NewPostActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("topic", currentTopic);
			if (cat.equals(Category.ALL_CATS)) bundle.putBoolean("allCats", true);
			if (type != null) bundle.putSerializable("fromTopicType", type);
			intent.putExtras(bundle);
			startActivity(intent);
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuNavSetUnread)
		{
			new MessageResponseAsyncTask(this, getString(R.string.unread_loading))
			{					
				@Override
				protected HFRMessageResponse executeInBackground() throws HFR4droidException
				{
					return getMessageSender().setUnread(currentTopic);
				}
				
				@Override
				protected void onActionFinished(String message)
				{
					currentTopic.setStatus(TopicStatus.NEW_MP);
					if (getDatasource() != null) getDatasource().notifyDataSetChanged();
				}
			}.execute();
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuNavUnflag)
		{
			new MessageResponseAsyncTask(this, getString(R.string.unflag_loading))
			{					
				@Override
				protected HFRMessageResponse executeInBackground() throws HFR4droidException
				{
					return getMessageSender().unflag(currentTopic, type, getDataRetriever().getHashCheck());
				}
				
				@Override
				protected void onActionFinished(String message)
				{
					Toast.makeText(TopicsActivity.this, message, Toast.LENGTH_SHORT).show();
					if (getDatasource() != null) getDatasource().remove(currentTopic);
					if (getDatasource() != null) getDatasource().notifyDataSetChanged();	
				}
			}.execute();
			return true;
		}
		else if (aItem.getItemId() == R.id.MenuNavDeleteMP)
		{
			getConfirmDialog(
			this,
			getString(R.string.delete_mp_title),
			getString(R.string.are_u_sure_message),
			new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1)
				{
					new MessageResponseAsyncTask(TopicsActivity.this, getString(R.string.delete_mp_loading))
					{
						@Override
						protected HFRMessageResponse executeInBackground()	throws HFR4droidException
						{
							return getMessageSender().deleteMP(currentTopic, getDataRetriever().getHashCheck());
						}

						@Override
						protected void onActionFinished(String message)
						{
							Toast.makeText(TopicsActivity.this, message, Toast.LENGTH_SHORT).show();
							if (getDatasource() != null) getDatasource().remove(currentTopic);
							if (getDatasource() != null) getDatasource().notifyDataSetChanged();
						}	
					}.execute();
				}
			}).show();
			return true;
		}
		else
		{
			try
			{
				Category newCat = aItem.getItemId() != -1 ?
						getDataRetriever().getSubCatById(cat, Long.valueOf(aItem.getItemId())) :
						getDataRetriever().getCatById(cat.getId());
				if (newCat != null)
				{
					previousCat = cat;
					cat = newCat;
					reloadPage(false, true);
					return true;
				}
			}
			catch (DataRetrieverException e)
			{
				error(e, true);
			}
			return false;
		}
	}
	
	protected ListView getListView()
	{
		return (ListView) getView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.topics, menu);
		getSupportMenuInflater().inflate(R.menu.drapeaux, menu);
		getSupportMenuInflater().inflate(R.menu.nav, menu);
		getSupportMenuInflater().inflate(R.menu.misc, menu);
		getSupportMenuInflater().inflate(R.menu.common, menu);
		SubMenu menuNav = menu.findItem(R.id.MenuNav).getSubMenu();
		menuNav.removeItem(R.id.MenuNavLastPage);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);

		MenuItem drapeaux = menu.findItem(R.id.MenuDrapeaux);
		MenuItem mps = menu.findItem(R.id.MenuMps);
		MenuItem addTopic = menu.findItem(R.id.MenuAddTopic);
		drapeaux.setVisible(isLoggedIn() && !isMpsCat());
		drapeaux.setEnabled(isLoggedIn() && !isMpsCat());
		mps.setVisible(isLoggedIn() && !isMpsCat());
		mps.setEnabled(isLoggedIn() && !isMpsCat());

		// Pour l'instant on a juste la création de mp
		if (isMpsCat()) addTopic.setTitle(R.string.new_mp);
		addTopic.setVisible(isLoggedIn() && isMpsCat());
		addTopic.setEnabled(isLoggedIn() && isMpsCat());

		MenuItem subcat = menu.findItem(R.id.MenuNavSubCats);
		subcat.setVisible(!isMpsCat() && !isAllCatsCat());
		subcat.setEnabled(!isMpsCat() && !isAllCatsCat());
		
		MenuItem drapeauxAll = drapeaux.getSubMenu().findItem(R.id.MenuDrapeauxAll);
		drapeauxAll.setVisible(!isAllCatsCat());
		drapeauxAll.setEnabled(!isAllCatsCat());

		boolean myTopics = type == TopicType.CYAN || type == TopicType.ROUGE || type == TopicType.FAVORI;
		MenuItem menuNav = menu.findItem(R.id.MenuNav);

		menuNav.setVisible(type == TopicType.ALL);
		menuNav.setEnabled(type == TopicType.ALL);

		MenuItem menuNavFP =  menuNav.getSubMenu().findItem(R.id.MenuNavFirstPage);
		menuNavFP.setVisible(currentPageNumber != 1);
		menuNavFP.setEnabled(currentPageNumber != 1);

		MenuItem menuNavPP =  menuNav.getSubMenu().findItem(R.id.MenuNavPreviousPage);
		menuNavPP.setVisible(currentPageNumber != 1);
		menuNavPP.setEnabled(currentPageNumber != 1);
		
		MenuItem menuNavUP =  menuNav.getSubMenu().findItem(R.id.MenuNavUserPage);
		menuNavUP.setVisible(!myTopics);
		menuNavUP.setEnabled(!myTopics);
		
		MenuItem menuNavNP =  menuNav.getSubMenu().findItem(R.id.MenuNavNextPage);
		menuNavNP.setVisible(!myTopics);
		menuNavNP.setEnabled(!myTopics);		

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean result = super.onOptionsItemSelected(item);
		if (!result)
		{
			if (item.getItemId() == R.id.MenuDrapeauxAll)
			{
				previousType = type;
				type = TopicType.ALL;
				if (currentPageNumber < 1) currentPageNumber = 1;
				reloadPage(false, true);
				return true;
			}
			else if (item.getItemId() == R.id.MenuDrapeauxCyan)
			{
				previousType = type;
				type = TopicType.CYAN;
				reloadPage(false, true);
				return true;
			}
			else if (item.getItemId() == R.id.MenuDrapeauxRouges)
			{
				previousType = type;
				type = TopicType.ROUGE;
				reloadPage(false, true);
				return true;
			}
			else if (item.getItemId() == R.id.MenuDrapeauxFavoris)
			{
				previousType = type;
				type = TopicType.FAVORI;
				reloadPage(false, true);
				return true;
			}
			else if (item.getItemId() == R.id.MenuAddTopic)
			{
				Intent intent = new Intent(TopicsActivity.this, NewTopicActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("cat", cat);
				intent.putExtras(bundle);
				startActivity(intent);
				return true;
			}
			else if (item.getItemId() == R.id.MenuNavSubCats)
			{
				openContextMenu(findViewById(R.id.CatTitle));
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
	
	public Category getCat()
	{
		return cat;
	}

	public void setCat(Category cat)
	{
		this.cat = cat;
	}

	protected TopicType getType() 
	{
		return type;
	}

	protected void setType(TopicType type) 
	{
		this.type = type;
	}
	
	public Category getPreviousCat()
	{
		return previousCat;
	}

	public void setPreviousCat(Category previousCat)
	{
		this.previousCat = previousCat;
	}

	public TopicType getPreviousType()
	{
		return previousType;
	}

	public void setPreviousType(TopicType previousType)
	{
		this.previousType = previousType;
	}

	@Override
	protected void setTitle()
	{
		TextView catTitle = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.CatTitle);
		catTitle.setTextSize(getTextSize(15));
		String title;
		if (isMpsCat() && getDatasource() != null && getDatasource().getCount() > 0)
		{
			int newMps = 0;
			for (int i = 0; i < getDatasource().getCount(); i++)
			{
				if (getDatasource().getItem(i).getStatus() == TopicStatus.NEW_MP) newMps++;
			}
			title = newMps == 0 ? "P." + currentPageNumber + " - " + cat.toString() : getResources().getQuantityString(R.plurals.mp_notification_content, newMps, newMps);
		}
		else
		{
			switch (type)
			{
				case CYAN:
					title = " " + cat.toString();
					catTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.flag_cyan), null, null, null);
					break;
		
				case ROUGE:
					title = " " + cat.toString();
					catTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.flag_rouge), null, null, null);
					break;
		
				case FAVORI:
					title = " " + cat.toString();
					catTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.flag_favori), null, null, null);
					break;
		
				default:
					catTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
					title = cat.toString() + ", P." + currentPageNumber;
					break;
			}
		}
		catTitle.setText(title);
		catTitle.setSelected(true);
		
		getSupportActionBar().setTitle(title);
	}
	
	@Override
	protected void applyTheme(Theme theme)
	{
		applyTheme(theme, getListView(), false);
	}
	
	
	@Override
	protected void customizeActionBar()
	{
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		LayoutInflater inflator = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.topics_title, null);
		getSupportActionBar().setCustomView(v);
		
		attachEvents();
	}
	
	private void attachEvents()
	{
		final TextView catTitle = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.CatTitle);
		registerForContextMenu(catTitle);

		catTitle.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
			public void onCreateContextMenu(android.view.ContextMenu menu, View v, ContextMenuInfo menuInfo)
			{
				if (isMpsCat() || isAllCatsCat()) return;

				// Gestion des sous-cats
				menu.setHeaderTitle(R.string.menu_subcat);
				try
				{
					android.view.MenuItem itemNone = menu.add(Menu.NONE, -1, Menu.NONE, R.string.menu_subcat_none);
					itemNone.setCheckable(cat.getSubCatId() == -1);
					itemNone.setChecked(cat.getSubCatId() == -1);
					for (SubCategory subCat : getDataRetriever().getSubCats(cat))
					{
						android.view.MenuItem item = menu.add(Menu.NONE, (int) subCat.getSubCatId(), Menu.NONE, subCat.toString(ToStringType.SUBCAT));
						item.setCheckable(cat.equals(subCat));
						item.setChecked(cat.equals(subCat));
					}
				}
				catch (DataRetrieverException e)
				{
					error(e, true);
				}
			}
		});
		
		catTitle.setOnTouchListener(new OnTouchListener()
		{
			private GestureDetector gd = new GestureDetector(new SimpleOnGestureListener()
			{
				public boolean onDoubleTap(MotionEvent e)
				{
					reloadPage();
					return true;
				}

				@Override
				public boolean onSingleTapConfirmed(MotionEvent e)
				{
					catTitle.setEllipsize(catTitle.getEllipsize() == TruncateAt.MARQUEE ? TruncateAt.END : TruncateAt.MARQUEE);
					return true;
				}
			});

			public boolean onTouch(View v, MotionEvent event)
			{
				if (event != null) return gd.onTouchEvent(event);
				return false;
			}
		});
	}
	
	protected void applyTheme(Theme theme, ListView mainList, boolean listOnly)
	{
		if (mainList == null) return;
		if (!listOnly) ((LinearLayout) mainList.getParent().getParent()).setBackgroundColor(theme.getListBackgroundColor());
		mainList.setDivider(new ColorDrawable(theme.getListDividerColor()));
		mainList.setDividerHeight(1);
		mainList.setCacheColorHint(theme.getListBackgroundColor());
		mainList.setSelector(getKeyByTheme(getThemeKey(), R.drawable.class, "list_selector"));
		
		TextView refreshText = (TextView) mainList.findViewById(R.id.pull_to_refresh_text);
		try
		{
			refreshText.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, "item"))));
		}
		catch (Exception e)
		{
			error(e);
		}
		
		((PullToRefreshListView) mainList).inverseColor(currentTheme.isProgressBarInversed());
	}

	@Override
	protected void loadFirstPage()
	{
		if (currentPageNumber == 2)
		{
			space.snapToScreen(getCurrentIndex() - 1);
		}
		else
		{
			loadTopics(cat, TopicType.ALL, 1, false);
		}
	}

	@Override
	protected void loadPreviousPage()
	{
		space.snapToScreen(getCurrentIndex() - 1);	
	}

	@Override
	protected void loadUserPage()
	{
		new PageNumberDialog(this, currentPageNumber)
		{
			protected void onValidate(int pageNumber)
			{
				if (Math.abs(pageNumber - currentPage) == 1)
				{
					space.snapToScreen(getCurrentIndex() + (pageNumber - currentPage));
				}
				else
				{
					loadTopics(cat, TopicType.ALL, pageNumber, false);
				}
			}
		}.show();
	}

	@Override
	protected void loadNextPage()
	{
		space.snapToScreen(getCurrentIndex() + 1);
	}

	@Override
	protected void reloadPage()
	{
		loadTopics(cat, type, currentPageNumber);	
	}

	protected void reloadPage(boolean sameActivity, boolean displayLoading)
	{
		loadTopics(cat, type, currentPageNumber, sameActivity, displayLoading);	
	}
	
	@Override
	protected void redrawPage()
	{
		setRefreshHeader();
		if (getDatasource() != null) getDatasource().notifyDataSetChanged();
		reset();
		preloadTopics();
	}

	@Override
	protected void goBack()
	{
		loadCats(false);
	}

	@Override
	protected void onLogout()
	{
		if (isMpsCat() || isAllCatsCat())
		{
			loadCats(false);
		}
		else
		{
			setType(TopicType.ALL);
			loadTopics(cat, TopicType.ALL, 1, false); // Load first page
		}
	}

	private List<Topic> addCats(List<Topic> topics)
	{
		Category currentCat = null;
		ArrayList<Topic> result = new ArrayList<Topic>();
		for (Topic t : topics)
		{
			if (!t.getCategory().equals(currentCat))
			{
				Topic catTopic = new Topic(-1, "Dummy topic");
				catTopic.setCategory(t.getCategory());
				result.add(catTopic);
			}
			result.add(t);
			currentCat = t.getCategory();
		}
		return result;
	}
	
	public void preloadTopics()
	{
		// Préchargement de la page suivante dans le composant DragableSpace 
		if (type == TopicType.ALL)
		{
			preLoadingTopicsAsyncTask = currentPageNumber != 1 ? 
					new PreLoadingTopicsAsyncTask(this, currentPageNumber - 1) :
					new PreLoadingTopicsAsyncTask(this);
			preLoadingTopicsAsyncTask.execute(currentPageNumber + 1, cat);
		}
	}

	public void refreshTopics(List<Topic> topics)
	{
		if (getDatasource() != null)
		{
			getDatasource().clear();
			if (isAllCatsCat(cat)) topics = addCats(topics);
			for (Topic t : topics)
			{
				getDatasource().add(t);
			}
			getDatasource().notifyDataSetChanged();
			supportInvalidateOptionsMenu();
			if (getListView() != null) getListView().setSelection(0);
		}
	}

	protected boolean isMpsCat()
	{
		return isMpsCat(cat);
	}

	protected boolean isAllCatsCat()
	{
		return isAllCatsCat(cat);
	}
	
	protected void setRefreshHeader()
	{
		TextView refreshText = (TextView) getListView().findViewById(R.id.pull_to_refresh_text);
		refreshText.setTextSize(getTextSize(15));
		float scale = getResources().getDisplayMetrics().density;
		int top = (int) (5 * scale + 0.5f);
		int left = (int) (25 * (getPoliceSize() - 1) * scale + 0.5f);
		refreshText.setPadding(left, top, 0, 0);
	}

	/* Classes internes */

	private class TopicAdapter extends ArrayAdapter<Topic>
	{
		private List<Topic> topics;

		public TopicAdapter(Context context, int resource, int textViewResourceId, List<Topic> topics)
		{
			super(context, resource, textViewResourceId, topics);
			this.topics = topics;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = super.getView(position, convertView, parent);
			Topic t = topics.get(position);
			boolean isDummyTopic = t.getId() == -1;

			TextView topicTitle = (TextView) v.findViewById(R.id.ItemContent);
			ImageView flag = (ImageView) v.findViewById(R.id.TopicFlag);
			LinearLayout topicInfos = (LinearLayout) v.findViewById(R.id.TopicInfos);
			LinearLayout topicAddInfos = (LinearLayout) v.findViewById(R.id.TopicAddInfos);
			TextView lastPostInfos = (TextView) v.findViewById(R.id.LastPostInfos);
			TextView pagesInfos = (TextView) v.findViewById(R.id.PagesInfos);
			TextView remainingPages = (TextView) v.findViewById(R.id.ItemRemainingPages);
			TextView catTitle = (TextView) v.findViewById(R.id.CatTitle);
			
			int left, right, top, bottom;
			float scale = getResources().getDisplayMetrics().density;

			if (isDummyTopic)
			{
				flag.setVisibility(View.GONE);
				topicInfos.setVisibility(View.GONE);
				remainingPages.setVisibility(View.GONE);
				catTitle.setVisibility(View.VISIBLE);
				
				catTitle.setText(t.getCategory().toString());
				catTitle.setTextSize(getTextSize(14));
				v.setBackgroundResource(getKeyByTheme(getThemeKey(), R.drawable.class, "selector"));
				
				try
				{				
					catTitle.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, "item_cat_header"))));
				}
				catch (Exception e)
				{
					error(e);
				}
				
				left = right = (int) (7 * scale + 0.5f);
				top = bottom = (int) (12 * scale + 0.5f);
				v.setPadding(left, top, right, bottom);
			}
			else
			{
				try
				{				
					topicTitle.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, "item"))));
				}
				catch (Exception e)
				{
					error(e);
				}
				
				if (isMpsCat())
				{
					TextView unread = (TextView) topicInfos.findViewById(R.id.ItemUnread);
					unread.setTextSize(getTextSize(12));
					unread.setVisibility(t.isUnread() ? View.VISIBLE : View.GONE);

					TextView author = (TextView) topicInfos.findViewById(R.id.ItemAuthor);
					author.setTextSize(getTextSize(14));
					author.setText(Html.fromHtml("<b>@" + t.getAuthor() + "</b> : "));
					try
					{
						author.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, "item"))));
					}
					catch (Exception e)
					{
						error(e);
					}
				}
				else
				{
					topicInfos.findViewById(R.id.ItemUnread).setVisibility(View.GONE);
					topicInfos.findViewById(R.id.ItemAuthor).setVisibility(View.GONE);
				}
				
				catTitle.setVisibility(View.GONE);
				topicTitle.setTypeface(null, t.isSticky() ? Typeface.BOLD : Typeface.NORMAL);
				topicTitle.setText(t.toString());
				topicTitle.setTextSize(getTextSize(14));
				v.setBackgroundResource(0);
				
				flag.setVisibility(View.VISIBLE);
				switch (t.getStatus())
				{			
					case NEW_CYAN:
						flag.setBackgroundResource(R.drawable.flag_cyan);
						break;
	
					case NEW_ROUGE:
						flag.setBackgroundResource(R.drawable.flag_rouge);
						break;

					case NEW_FAVORI:
						flag.setBackgroundResource(R.drawable.flag_favori);
						break;

					case NO_NEW_POST:
						flag.setBackgroundResource(R.drawable.flag_none);
						break;

					case NEW_MP:
						flag.setBackgroundResource(R.drawable.flag_mp);
						break;

					case NO_NEW_MP:
						flag.setBackgroundResource(R.drawable.flag_mp_none);
						break;
	
					case LOCKED:
						flag.setBackgroundResource(R.drawable.flag_lock);
						break;						
	
					default:
						flag.setBackgroundResource(R.drawable.flag_blank);
						break;
				}
				
				topicInfos.setVisibility(View.VISIBLE);

				if (!moreTopicInfos)
				{
					topicAddInfos.setVisibility(View.GONE);
					remainingPages.setVisibility(t.getLastReadPage() != -1 && (t.getNbPages() - t.getLastReadPage()) > 0 ? View.VISIBLE : View.GONE);
					remainingPages.setText("(" + (t.getNbPages() - t.getLastReadPage()) + ")");
					
					try
					{
						remainingPages.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, "item2"))));
					}
					catch (Exception e)
					{
						error(e);
					}
					
					top = bottom = (int) (12 * scale + 0.5f);
				}
				else
				{
					remainingPages.setVisibility(View.GONE);
					topicAddInfos.setVisibility(View.VISIBLE);
					lastPostInfos.setTextSize(getTextSize(11));
					pagesInfos.setTextSize(getTextSize(11));
					SimpleDateFormat todaySdf = new SimpleDateFormat("HH:mm");
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
					lastPostInfos.setText(getString(R.string.last_post_infos, formatDate(todaySdf, sdf, t.getLastPostDate()), t.getLastPostPseudo()));
					if (t.getLastReadPage() != -1)
					{
						pagesInfos.setText(Html.fromHtml("(" + t.getNbPages() + "/<b>" + (t.getNbPages() - t.getLastReadPage()) + "</b>)"));
					}
					else
					{
						pagesInfos.setText("(" + t.getNbPages() + ")");
					}
	
					try
					{
						lastPostInfos.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, "item2"))));
						pagesInfos.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(getKeyByTheme(getThemeKey(), R.color.class, "item2"))));
					}
					catch (Exception e)
					{
						error(e);
					}
					
					top = bottom = (int) (6 * scale + 0.5f);
				}

				left = right = (int) (7 * scale + 0.5f);
				v.setPadding(left, top, right, bottom);
			}
			return v;
		}
	}
	
	public View buildView(ArrayAdapter<Topic> topics)
	{
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.topics_dragable, null);
	}
	
	private class PreLoadingTopicsAsyncTask extends PreLoadingAsyncTask<Topic, Category, ArrayAdapter<Topic>>
	{
		public PreLoadingTopicsAsyncTask(HFR4droidMultiListActivity<ArrayAdapter<Topic>> context)
		{
			super(context);
		}

		public PreLoadingTopicsAsyncTask(HFR4droidMultiListActivity<ArrayAdapter<Topic>> context, int... otherPageNumbers)
		{
			super(context, otherPageNumbers);
		}

		@Override
		protected ArrayAdapter<Topic> getDatasource(List<Topic> topics)
		{
			return new TopicAdapter(TopicsActivity.this, R.layout.topic, R.id.ItemContent, topics);
		}

		@Override
		protected void init(View v, ArrayAdapter<Topic> datasource)
		{
			((PullToRefreshListView) v).setAdapter(datasource);
			applyTheme(currentTheme, ((PullToRefreshListView) v), true);
			onCreateInit(((PullToRefreshListView) v), getPageNumber());
		}

		@Override
		protected void loadAnotherPage(int pageNumber)
		{
			preLoadingTopicsAsyncTask = new PreLoadingTopicsAsyncTask(TopicsActivity.this);
			preLoadingTopicsAsyncTask.execute(pageNumber, cat);		
		}

		@Override
		protected List<Topic> retrieveDataInBackground(Category... categories) throws DataRetrieverException
		{
			return getDataRetriever().getTopics(categories[0], TopicType.ALL, getPageNumber());
		}
		
	}
}