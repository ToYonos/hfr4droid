package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import android.widget.Button;
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
	private Category cat;
	private TopicType type;
	private GestureDetector gestureDetector;

    @SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
       	super.onCreate(savedInstanceState);
        setContentView(R.layout.topics);
        attachEvents();

        Bundle bundle = this.getIntent().getExtras();
        boolean allCats = bundle.getBoolean("allCats", false);
        type = bundle.getSerializable("topicType") != null ? (TopicType) bundle.getSerializable("topicType") : TopicType.ALL;

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
        else if (bundle != null && bundle.getSerializable("cat") != null)
        {
        	cat = (Category) bundle.getSerializable("cat");
        	loadTopics(cat, type);
        }
 
        if (cat != null)
        {
    		setTitle();
    		updateButtonsStates();
    		if (cat.equals(Category.MPS_CAT)) clearNotifications();
        }
        
        final ListView lv = getListView();
        adapter = new TopicAdapter(this, R.layout.topic, R.id.ItemContent, topics);
        lv.setAdapter(adapter);

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
                inflater.inflate(R.menu.nav_simple, menu);
                menu.setHeaderTitle(R.string.nav_go_to);
            	Topic currentTopic = (Topic) getListView().getAdapter().getItem(((AdapterContextMenuInfo)menuInfo).position);
                if (!isLoggedIn() || currentTopic.getLastReadPage() == -1) menu.removeItem(R.id.MenuNavLastReadPage);
        	}
		});
        
		gestureDetector = new GestureDetector(new SimpleNavOnGestureListener()
		{
			@Override
			protected void onLeftToRight()
			{
				if (currentPageNumber != 1 && type == TopicType.ALL) loadPreviousPage();
			}

			@Override
			protected void onRightToLeft()
			{
				if (type == TopicType.ALL) loadNextPage();
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
		drapeaux.setVisible(isLoggedIn() && !isMpsCat());
		drapeaux.setEnabled(isLoggedIn() && !isMpsCat());
		mps.setVisible(isLoggedIn() && !isMpsCat());
		mps.setEnabled(isLoggedIn() && !isMpsCat());
    	
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
	protected void setWindowTitle()
    {
		String title = getString(R.string.app_name) + " (";
		switch (type)
		{			
			case CYAN:
				title += getString(R.string.menu_drapeaux_cyan).toLowerCase();
				break;

			case ROUGE:
				title += getString(R.string.menu_drapeaux_rouges).toLowerCase();
				break;
				
			case FAVORI:
				title += getString(R.string.menu_drapeaux_favoris).toLowerCase();
				break;

			default:
				title += getString(R.string.menu_drapeaux_all).toLowerCase();
				break;
		}
		title += ")";
		if (type.equals(TopicType.ALL)) title += " - P." + currentPageNumber;
		setTitle(title);
    }
	
	@Override
	protected void setTitle()
	{
    	TextView topicTitle = (TextView) findViewById(R.id.CatTitle);
    	topicTitle.setText(cat.toString());
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
	protected void goBack()
	{
		loadCats(false);
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
		if (type == TopicType.CYAN || type == TopicType.ROUGE || type == TopicType.FAVORI)
		{
			nav.setVisibility(View.GONE);
		}
		else
		{
			nav.setVisibility(View.VISIBLE);
			
			Button buttonFP = (Button) findViewById(R.id.ButtonNavFirstPage);
			buttonFP.setEnabled(currentPageNumber != 1);
			
			Button buttonPP = (Button) findViewById(R.id.ButtonNavPreviousPage);
			buttonPP.setEnabled(currentPageNumber != 1);
	
			Button buttonLP = (Button) findViewById(R.id.ButtonNavLastPage);
			buttonLP.setEnabled(false);
		}
	}

	private void attachEvents()
	{
		SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.Nav);
		final Button toggleNav = (Button) findViewById(R.id.NavToggle);
		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener()
		{
			public void onDrawerOpened()
			{
				toggleNav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.right_arrow, 0, 0, 0);
			}
		});

		slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener()
		{
			public void onDrawerClosed()
			{
				toggleNav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.left_arrow, 0, 0, 0);
			}
		});

		Button buttonFP = (Button) findViewById(R.id.ButtonNavFirstPage);
		buttonFP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				loadFirstPage();	
			}
		});
		
		Button buttonPP = (Button) findViewById(R.id.ButtonNavPreviousPage);
		buttonPP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				loadPreviousPage();	
			}
		});
		
		Button buttonUP = (Button) findViewById(R.id.ButtonNavUserPage);
		buttonUP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				loadUserPage();	
			}
		});			
		
		Button buttonNP = (Button) findViewById(R.id.ButtonNavNextPage);
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