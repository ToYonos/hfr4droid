package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicStatus;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

/**
 * <p>Activity listant les topics</p>
 * 
 * @author ToYonos
 *
 */
public class TopicsActivity extends HFR4droidListActivity<Topic>
{
	private Category cat = null;
	private TopicType type = null;
	private GestureDetector gestureDetector;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topics);
		attachEvents();

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
		}
		else
		{
			if (bundle != null && bundle.getSerializable("cat") != null)
			{
				cat = (Category) bundle.getSerializable("cat");
			}
			if (cat != null) loadTopics(cat, type, currentPageNumber);
		}

		final ListView lv = getListView();
		adapter = new TopicAdapter(this, R.layout.topic, R.id.ItemContent, topics);
		lv.setAdapter(adapter);
		
		if (cat != null)
		{
			setTitle();
			updateButtonsStates();
			if (cat.equals(Category.MPS_CAT)) clearNotifications();
		}

		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> a, View v, int position, long id)
			{	
				Topic topic = (Topic) lv.getItemAtPosition(position);
				if (topic.getId() != -1)
				{
					int page = topic.getLastReadPage() != -1 ? topic.getLastReadPage() : 1;
					loadPosts(topic, page, false);
				}
				else
				{
					cat = topic.getCategory();
					loadTopics(topic.getCategory(), type);
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
					inflater.inflate(R.menu.nav_simple, menu);
					menu.setHeaderTitle(R.string.nav_go_to);
					if (!isLoggedIn() || currentTopic.getLastReadPage() == -1) menu.removeItem(R.id.MenuNavLastReadPage);
				}
				else
				{
					inflater.inflate(R.menu.drapeaux_simple, menu);
					menu.setHeaderTitle(R.string.menu_drapeaux);
				}
			}
		});

		gestureDetector = new GestureDetector(new SimpleNavOnGestureListener()
		{
			@Override
			protected void onLeftToRight()
			{
				if (type != TopicType.ALL) return;
				if (currentPageNumber != 1)
				{
					loadPreviousPage();
				}
				else
				{
					reloadPage();
				}
			}

			@Override
			protected void onRightToLeft()
			{
				if (type == TopicType.ALL) loadNextPage();
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent e)
			{
				return false;
			}
		});

		lv.setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				return gestureDetector.onTouchEvent(event);
			}
		});
	}

	@Override
	public boolean onContextItemSelected(MenuItem aItem)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Topic currentTopic = (Topic) getListView().getAdapter().getItem(menuInfo.position);

		switch (aItem.getItemId())
		{    	
			case R.id.MenuNavLastReadPage:
				loadPosts(currentTopic, currentTopic.getLastReadPage(), false);
				return true;
	
			case R.id.MenuNavFirstPage:
				loadPosts(currentTopic, 1, false);
				return true;
	
			case R.id.MenuNavUserPage:
				new PageNumberDialog(currentTopic.getNbPages())
				{
					protected void onValidate(int pageNumber)
					{
						loadPosts(currentTopic, pageNumber, false);
					}
				}.show();
				return true;
	
			case R.id.MenuNavLastPage:
				loadPosts(currentTopic, currentTopic.getNbPages(), false);
				return true;
				
			case R.id.MenuDrapeauxAll:
				cat = currentTopic.getCategory();
				loadTopics(currentTopic.getCategory(), TopicType.ALL, 1);
				return true;
	
			case R.id.MenuDrapeauxCyan:
				cat = currentTopic.getCategory();
				loadTopics(currentTopic.getCategory(), TopicType.CYAN);
				return true;
	
			case R.id.MenuDrapeauxRouges:
				cat = currentTopic.getCategory();
				loadTopics(currentTopic.getCategory(), TopicType.ROUGE);
				return true;
	
			case R.id.MenuDrapeauxFavoris:
				cat = currentTopic.getCategory();
				loadTopics(currentTopic.getCategory(), TopicType.FAVORI);
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
		inflater.inflate(R.menu.drapeaux, menu);
		inflater.inflate(R.menu.misc, menu);
		inflater.inflate(R.menu.nav, menu);
		inflater.inflate(R.menu.topics, menu);
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
		// Pour l'instant on a juste la cr�ation de mp
		if (isMpsCat()) addTopic.setTitle(R.string.new_mp);
		addTopic.setVisible(isLoggedIn() && isMpsCat());
		addTopic.setEnabled(isLoggedIn() && isMpsCat());

		MenuItem drapeauxAll = drapeaux.getSubMenu().findItem(R.id.MenuDrapeauxAll);
		drapeauxAll.setVisible(!isAllCatsCat());
		drapeauxAll.setEnabled(!isAllCatsCat());

		boolean myTopics = type == TopicType.CYAN || type == TopicType.ROUGE || type == TopicType.FAVORI;
		MenuItem menuNav = menu.findItem(R.id.MenuNav);

		menuNav.setVisible(!myTopics);
		menuNav.setEnabled(!myTopics);

		MenuItem menuNavRefresh =  menuNav.getSubMenu().findItem(R.id.MenuNavRefresh);
		menuNavRefresh.setVisible(!myTopics && !isMpsCat() && isLoggedIn());
		menuNavRefresh.setEnabled(!myTopics && !isMpsCat() && isLoggedIn());
		MenuItem refresh = menu.findItem(R.id.MenuRefresh);
		refresh.setVisible(myTopics || isMpsCat() || !isLoggedIn());
		refresh.setEnabled(myTopics || isMpsCat() || !isLoggedIn());	

		MenuItem menuNavFP =  menuNav.getSubMenu().findItem(R.id.MenuNavFirstPage);
		menuNavFP.setVisible(currentPageNumber != 1);
		menuNavFP.setEnabled(currentPageNumber != 1);

		MenuItem menuNavPP =  menuNav.getSubMenu().findItem(R.id.MenuNavPreviousPage);
		menuNavPP.setVisible(currentPageNumber != 1);
		menuNavPP.setEnabled(currentPageNumber != 1);

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
				case R.id.MenuDrapeauxAll :
					type = TopicType.ALL;
					if (currentPageNumber < 1) currentPageNumber = 1;
					reloadPage();
					return true;
	
				case R.id.MenuDrapeauxCyan :
					type = TopicType.CYAN;
					reloadPage();
					return true;
	
				case R.id.MenuDrapeauxRouges :
					type = TopicType.ROUGE;
					reloadPage();
					return true; 
	
				case R.id.MenuDrapeauxFavoris :
					type = TopicType.FAVORI;
					reloadPage();
					return true;
					
				case R.id.MenuAddTopic :
					Intent intent = new Intent(TopicsActivity.this, NewPostActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Bundle bundle = new Bundle();
					bundle.putSerializable("cat", cat);
					intent.putExtras(bundle);
					startActivity(intent);
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

	protected TopicType getType() 
	{
		return type;
	}

	protected void setType(TopicType type) 
	{
		this.type = type;
	}

	@Override
	protected void setTitle()
	{
		TextView catTitle = (TextView) findViewById(R.id.CatTitle);
		catTitle.setTextSize(getTextSize(15));
		String title;
		if (isMpsCat() && adapter.getCount() > 0)
		{
			int newMps = 0;
			for (int i = 0; i < adapter.getCount(); i++)
			{
				if (adapter.getItem(i).getStatus() == TopicStatus.NEW_MP) newMps++;
			}
			cat.setName(newMps == 0 ? 
								Category.MPS_CAT.getName() :
								getResources().getQuantityString(R.plurals.mp_notification_content, newMps, newMps));
			title = newMps == 0 ? "P." + currentPageNumber + " - " + cat.toString() : cat.toString();
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
	}

	@Override
	protected void loadFirstPage()
	{
		loadTopics(cat, TopicType.ALL, 1);	
	}

	@Override
	protected void loadPreviousPage()
	{
		loadTopics(cat, TopicType.ALL, currentPageNumber - 1);	
	}

	@Override
	protected void loadUserPage()
	{
		new PageNumberDialog()
		{
			protected void onValidate(int pageNumber)
			{
				loadTopics(cat, TopicType.ALL, pageNumber);
			}
		}.show();
	}

	@Override
	protected void loadNextPage()
	{
		loadTopics(cat, TopicType.ALL, currentPageNumber + 1);
	}

	@Override
	protected void reloadPage()
	{
		loadTopics(cat, type, currentPageNumber);	
	}

	@Override
	protected void redrawPage()
	{
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void goBack()
	{
		loadCats(false);
	}

	@Override
	protected void onLogout()
	{
		if (isMpsCat())
		{
			loadCats(false);
		}
		else
		{
			setType(TopicType.ALL);
			loadFirstPage();
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

	private void updateButtonsStates()
	{		
		SlidingDrawer nav = (SlidingDrawer) findViewById(R.id.Nav);
		TextView catTitle = (TextView) findViewById(R.id.CatTitle);
		if (type == TopicType.CYAN || type == TopicType.ROUGE || type == TopicType.FAVORI)
		{
			nav.setVisibility(View.GONE);
			catTitle.setPadding(5, 0, 5, 0);
		}
		else
		{
			nav.setVisibility(View.VISIBLE);
			catTitle.setPadding(5, 0, 55, 0);

			ImageView buttonFP = (ImageView) findViewById(R.id.ButtonNavFirstPage);
			buttonFP.setEnabled(currentPageNumber != 1);
			buttonFP.setAlpha(currentPageNumber != 1 ? 255 : 105);

			ImageView buttonPP = (ImageView) findViewById(R.id.ButtonNavPreviousPage);
			buttonPP.setEnabled(currentPageNumber != 1);
			buttonPP.setAlpha(currentPageNumber != 1 ? 255 : 105);

			ImageView buttonLP = (ImageView) findViewById(R.id.ButtonNavLastPage);
			buttonLP.setEnabled(false);
			buttonLP.setAlpha(105);
		}
	}

	private void attachEvents()
	{
		SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.Nav);
		final ImageView toggleNav = (ImageView) ((LinearLayout) findViewById(R.id.NavToggle)).getChildAt(0);
		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener()
		{
			public void onDrawerOpened()
			{
				toggleNav.setImageResource(R.drawable.right_arrow);
			}
		});

		slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener()
		{
			public void onDrawerClosed()
			{
				toggleNav.setImageResource(R.drawable.left_arrow);
			}
		});

		ImageView buttonFP = (ImageView) findViewById(R.id.ButtonNavFirstPage);
		buttonFP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				loadFirstPage();	
			}
		});

		ImageView buttonPP = (ImageView) findViewById(R.id.ButtonNavPreviousPage);
		buttonPP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				loadPreviousPage();	
			}
		});

		ImageView buttonUP = (ImageView) findViewById(R.id.ButtonNavUserPage);
		buttonUP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				loadUserPage();	
			}
		});			

		ImageView buttonNP = (ImageView) findViewById(R.id.ButtonNavNextPage);
		buttonNP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				loadNextPage();	
			}
		});
	}

	public void refreshTopics(List<Topic> topics)
	{
		adapter.clear();
		if (isAllCatsCat(cat)) topics = addCats(topics);
		for (Topic t : topics)
		{
			adapter.add(t);
		}
		adapter.notifyDataSetChanged();
		updateButtonsStates();
		getListView().setSelection(0);
	}

	protected boolean isMpsCat()
	{
		return isMpsCat(cat);
	}

	protected boolean isAllCatsCat()
	{
		return isAllCatsCat(cat);
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

			TextView text1 = (TextView) v.findViewById(R.id.ItemContent);
			text1.setTextSize(getTextSize(14));
			ImageView flag = (ImageView) v.findViewById(R.id.TopicFlag);
			boolean isDummyTopic = t.getId() == -1;

			try
			{
				text1.setTextColor(isDummyTopic ? ColorStateList.createFromXml(getResources(), getResources().getXml(R.color.item_cat_header))
						: ColorStateList.createFromXml(getResources(), getResources().getXml(R.color.item)));
			} catch (Exception e) {}

			LinearLayout ll = (LinearLayout) text1.getParent();
			if (isMpsCat())
			{
				TextView author = (TextView) ll.findViewById(R.id.ItemAuthor);
				author.setTextSize(getTextSize(14));
				author.setText(Html.fromHtml("<b>@" + t.getAuthor() + "</b> : "));
				try
				{
					author.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(R.color.item)));
				} catch (Exception e) {}

			}
			else
			{
				ll.removeView(ll.findViewById(R.id.ItemAuthor));
			}

			text1.setText(isDummyTopic ? t.getCategory().toString() : t.toString());
			text1.setTypeface(null, isDummyTopic || t.isSticky() ? Typeface.BOLD : Typeface.NORMAL);
			text1.setGravity(isDummyTopic ? Gravity.CENTER : Gravity.LEFT);
			ll.setBackgroundColor(isDummyTopic ? Color.parseColor("#336699") : Color.TRANSPARENT);			

			if (isDummyTopic)
			{
				flag.setVisibility(View.GONE);
			}
			else
			{
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
			}

			return v;
		}
	}
}