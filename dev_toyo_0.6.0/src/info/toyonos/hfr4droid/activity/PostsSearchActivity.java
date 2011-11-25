package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Post;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class PostsSearchActivity extends PostsActivity
{
	private List<Post> fromPosts;
	private String pseudo;
	private String word;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final LinearLayout searchPanel = (LinearLayout) findViewById(R.id.SearchPostsPanel);
		searchPanel.setVisibility(View.VISIBLE);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreateInit(Bundle bundle)
	{
		fromPosts = new ArrayList<Post>();
		Post fromPost = bundle != null ? (Post) bundle.getSerializable("fromPost") : new Post(0);
		currentPageNumber = 1;
		addFromPost(fromPost);
		
		pseudo = bundle != null ? bundle.getString("pseudo") : null;
		word = bundle != null ? bundle.getString("word") : null;
		
		if (bundle != null && bundle.getSerializable("posts") != null)
		{
			posts = (List<Post>) bundle.getSerializable("posts");
			if (posts != null && posts.size() > 0)
			{
				topic = posts.get(0).getTopic();
				Post lastPost = posts.get(posts.size() - 1);
				addFromPost(lastPost);
				displayPosts(posts);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onPrepareOptionsMenu(menu);
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
	protected void reloadPage()
	{
		searchPosts(topic, pseudo, word, fromPosts.get(currentPageNumber - 1));
	}

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

		ImageView buttonNP = (ImageView) findViewById(R.id.ButtonNavNextPage);
		buttonNP.setEnabled(currentPageNumber != topic.getNbPages());
		buttonNP.setAlpha(currentPageNumber != topic.getNbPages() ? 255 : 105);

		ImageView buttonLP = (ImageView) findViewById(R.id.ButtonNavLastPage);
		buttonLP.setEnabled(false);
		buttonLP.setAlpha(105);
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
}
