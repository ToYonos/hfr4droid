package info.toyonos.hfr4droid.common.activity;

import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.core.bean.Post;
import info.toyonos.hfr4droid.common.core.bean.PostFromSearch;
import info.toyonos.hfr4droid.common.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.common.core.data.HFRUrlParser;
import info.toyonos.hfr4droid.common.core.data.MDUrlParser;
import info.toyonos.hfr4droid.common.core.message.HFRMessageSender.ResponseCode;
import info.toyonos.hfr4droid.common.util.helper.NewPostUIHelper;
import info.toyonos.hfr4droid.common.util.listener.OnScreenChangeListener;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.naholyr.android.ui.HFR4droidQuickActionWindow;
import com.naholyr.android.ui.QuickActionWindow;
import com.naholyr.android.ui.QuickActionWindow.Item;

/**
 * <p>Activity permettant la recherche de posts intra topic</p>
 * 
 * @author ToYonos
 *
 */
public class PostsSearchActivity extends PostsActivity
{
	private List<Post> fromPosts;
	private String pseudo;
	private String word;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreateInit(Bundle bundle)
	{
		// Listener pour le changement de view dans le composant DragableSpace
		space.setOnScreenChangeListener(new OnScreenChangeListener()
		{
			public void onScreenChange(int oldIndex, int newIndex){}
			
			public void onFailRearward()
			{
				if (currentPageNumber != 1) loadPreviousPage();
			}
			
			public void onFailForward()
			{
				loadNextPage();
			}
		});
		
		fromPosts = new ArrayList<Post>();
		Post fromPost = bundle != null ? (Post) bundle.getSerializable("fromPost") : new Post(0);
		currentPageNumber = 1;
		addFromPost(fromPost);
		
		pseudo = bundle != null ? bundle.getString("pseudo") : null;
		word = bundle != null ? bundle.getString("word") : null;
		
		if (bundle != null && bundle.getSerializable("posts") != null)
		{
			List<Post> posts = setDatasource((List<Post>) bundle.getSerializable("posts"));
			if (posts != null && posts.size() > 0)
			{
				topic = posts.get(0).getTopic();
				Post lastPost = posts.get(posts.size() - 1);
				addFromPost(lastPost);
				displayPosts(posts);
			}
		}
		
		EditText pseudoET = (EditText) findViewById(R.id.SearchPostsPseudo);
		pseudoET.setText(pseudo);
		EditText wordET = (EditText) findViewById(R.id.SearchPostsWord);
		wordET.setText(word);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		LinearLayout searchPanel = (LinearLayout) findViewById(R.id.SearchPostsPanel);
		searchPanel.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_SEARCH)
		{
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);

		MenuItem menuNav = menu.findItem(R.id.MenuNav);
		SubMenu subMenuNav = menu.findItem(R.id.MenuNav).getSubMenu();

		MenuItem menuNavFP = subMenuNav.findItem(R.id.MenuNavFirstPage);
		menuNavFP.setVisible(currentPageNumber != 1);
		menuNavFP.setEnabled(currentPageNumber != 1);

		MenuItem menuNavPP = subMenuNav.findItem(R.id.MenuNavPreviousPage);
		menuNavPP.setVisible(currentPageNumber != 1);
		menuNavPP.setEnabled(currentPageNumber != 1);
		
		MenuItem menuNavUP = subMenuNav.findItem(R.id.MenuNavUserPage);
		menuNavUP.setVisible(false);
		menuNavUP.setEnabled(false);

		MenuItem menuNavNP = subMenuNav.findItem(R.id.MenuNavNextPage);
		menuNavNP.setVisible(true);
		menuNavNP.setEnabled(true);

		MenuItem menuNavLP = subMenuNav.findItem(R.id.MenuNavLastPage);
		menuNavLP.setVisible(false);
		menuNavLP.setEnabled(false);

		MenuItem menuNavRefresh =  menuNav.getSubMenu().findItem(R.id.MenuNavRefresh);
		menuNavRefresh.setVisible(false);
		menuNavRefresh.setEnabled(false);

		MenuItem refresh = menu.findItem(R.id.MenuRefresh);
		refresh.setVisible(false);
		refresh.setEnabled(false);

		return true;
	}

	@Override
	protected void setTitle()
	{
		final TextView topicTitle = (TextView) findViewById(R.id.TopicTitle);
		topicTitle.setTextSize(getTextSize(15));
		topicTitle.setText(topic.toString());
		topicTitle.setSelected(true);
		final TextView topicPageNumber = (TextView) findViewById(R.id.TopicPageNumber);
		topicPageNumber.setTextSize(getTextSize(15));
		topicPageNumber.setText("P." + currentPageNumber);
	}

	@Override
	protected void loadFirstPage()
	{
		searchPosts(topic, pseudo, word, fromPosts.get(0));
	}

	@Override
	protected void loadPreviousPage()
	{
		searchPosts(topic, pseudo, word, fromPosts.get(currentPageNumber - 2));
	}

	@Override
	protected void loadNextPage()
	{
		searchPosts(topic, pseudo, word, fromPosts.get(currentPageNumber));
	}

	@Override
	protected void reloadPage() {}

	@Override
	protected void goBack()
	{
		finish();
	}

	protected void updateButtonsStates()
	{
		SlidingDrawer nav = (SlidingDrawer) findViewById(R.id.Nav);
		TextView topicTitle = (TextView) findViewById(R.id.TopicTitle);

		nav.setVisibility(View.VISIBLE);
		topicTitle.setPadding(5, 0, 55, 0);

		ImageView buttonFP = (ImageView) findViewById(R.id.ButtonNavFirstPage);
		buttonFP.setEnabled(currentPageNumber != 1);
		buttonFP.setAlpha(currentPageNumber != 1 ? 255 : 105);

		ImageView buttonPP = (ImageView) findViewById(R.id.ButtonNavPreviousPage);
		buttonPP.setEnabled(currentPageNumber != 1);
		buttonPP.setAlpha(currentPageNumber != 1 ? 255 : 105);
		
		ImageView buttonUP = (ImageView) findViewById(R.id.ButtonNavUserPage);
		buttonUP.setEnabled(false);
		buttonUP.setAlpha(105);

		ImageView buttonNP = (ImageView) findViewById(R.id.ButtonNavNextPage);
		buttonNP.setEnabled(true);
		buttonNP.setAlpha(255);

		ImageView buttonLP = (ImageView) findViewById(R.id.ButtonNavLastPage);
		buttonLP.setEnabled(false);
		buttonLP.setAlpha(105);
	}

	@Override
	protected void onPostingOk(ResponseCode code, long postId)
	{
		switch (code)
		{	
			case POST_EDIT_OK: // Edit ok
				jumpToPost(postId);
				break;

			case POST_ADD_OK: // New post ok
				topic.setLastReadPost(NewPostUIHelper.BOTTOM_PAGE_ID);
				loadPosts(topic, topic.getNbPages(), false);
				break;
		}
	}

	protected void addQuickActionWindowItems(HFR4droidQuickActionWindow window, final long currentPostId, boolean isMine)
	{
		QuickActionWindow.Item goToOriginalPost = new QuickActionWindow.Item(PostsSearchActivity.this, "", R.drawable.ic_menu_goto, new QuickActionWindow.Item.Callback()
		{	
			public void onClick(QuickActionWindow window, Item item, View anchor)
			{
				keepNavigationHistory = true;
				jumpToPost(currentPostId);
			}
		});					
		window.addItem(goToOriginalPost);

		super.addQuickActionWindowItems(window, currentPostId, isMine);
	}
	
	protected Post getCurrentFromPost()
	{
		return fromPosts.get(currentPageNumber - 1);
	}
	
	private void jumpToPost(long postId)
	{
		try
		{
			PostFromSearch p = (PostFromSearch) getPostById(postId);
			MDUrlParser urlParser = new HFRUrlParser(getDataRetriever());
			if (urlParser.parseUrl(p.getCallbackUrl()))
			{
				topic.setLastReadPost(postId);
				loadPosts(topic, urlParser.getPage(), false);
			}
		}
		catch (DataRetrieverException e)
		{
			error(getString(R.string.error_dispatching_url), e, true, false);
		}
	}
	
	protected void addFromPost(Post p)
	{
		fromPosts.add(p);
	}

	protected Post getLastFromPost()
	{
		int size = fromPosts.size();
		return size > 0 ? fromPosts.get(size - 1) : null;
	}
	
	protected void setPageNumberFromPost(Post p)
	{
		int index = fromPosts.indexOf(p);
		currentPageNumber = index != -1 ? index + 1 : 1;
	}

	@Override
	protected void attachEvents()
	{
		super.attachEvents();
		
		final TextView topicTitle = (TextView) findViewById(R.id.TopicTitle);
		topicTitle.setOnLongClickListener(null);
	}
	
	@Override
	public void preloadPosts(boolean verify)
	{
		// Rien à faire ici
	}
}
