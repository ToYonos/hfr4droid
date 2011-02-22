package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.HFR4droidException;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.BasicElement;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicStatus;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.core.data.HFRUrlParser;
import info.toyonos.hfr4droid.core.data.MDUrlParser;
import info.toyonos.hfr4droid.core.message.MessageSenderException;
import info.toyonos.hfr4droid.core.message.HFRMessageSender.ResponseCode;
import info.toyonos.hfr4droid.service.MpNotifyService;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Selection;
import android.text.TextUtils.TruncateAt;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

import com.naholyr.android.ui.HFR4droidQuickActionWindow;
import com.naholyr.android.ui.QuickActionWindow;
import com.naholyr.android.ui.QuickActionWindow.Item;

/**
 * <p>Activity listant les posts</p>
 * 
 * @author ToYonos
 *
 */
public class PostsActivity extends NewPostUIActivity
{
	private static final String POST_LOADING 	= ">�>�>�>�>�...post_loading...<�<�<�<�<�";

	public static enum PostCallBackType
	{
		ADD("add"),
		EDIT("edit"),
		QUOTE("quote"),
		DELETE("delete"),
		MULTIQUOTE_ADD("multiquote_add"),
		MULTIQUOTE_REMOVE("multiquote_remove"),
		FAVORITE("favorite");

		private final String key;

		private PostCallBackType(String key)
		{
			this.key = key;
		}

		public String getKey()
		{
			return this.key;
		}
	};

	private Topic topic;
	private List<Post> posts;
	private TopicType fromType;
	private boolean fromAllCats;

	private GestureDetector gestureDetector;
	private int currentScrollY;

	private Dialog postDialog;

	protected Map<Long, String> quotes;
	protected boolean lockQuickAction;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posts);
		attachEvents();
		currentScrollY = -1;
		postDialog = null;
		quotes = Collections.synchronizedMap(new HashMap<Long, String>());
		lockQuickAction = true;

		Bundle bundle = this.getIntent().getExtras();
		if (fromType == null) fromType =  bundle != null && bundle.getSerializable("fromTopicType") != null ? (TopicType) bundle.getSerializable("fromTopicType") : TopicType.ALL;
		fromAllCats = bundle == null ? false : bundle.getBoolean("fromAllCats", false); 
		if (bundle != null && bundle.getSerializable("posts") != null)
		{
			posts = (List<Post>) bundle.getSerializable("posts");
			if (posts != null && posts.size() > 0)
			{
				topic = posts.get(0).getTopic();
				if (isPreloadingEnable()) preLoadPosts(topic, currentPageNumber);
				displayPosts(posts);
			}
		}
		else
		{
			if (bundle != null && bundle.getSerializable("topic") != null)
			{
				topic = (Topic) bundle.getSerializable("topic");
			}
			if (topic != null) loadPosts(topic, currentPageNumber);
		}

		if (topic != null)
		{
			setTitle();
			updateButtonsStates();
			if (topic.getCategory().equals(Category.MPS_CAT))
			{
				clearNotifications();
				if (MpNotifyService.currentNewMps > 0 && topic.getStatus() == TopicStatus.NEW_MP) MpNotifyService.currentNewMps--;
			}
		}

		gestureDetector = new GestureDetector(new SimpleNavOnGestureListener()
		{
			@Override
			protected void onLeftToRight()
			{
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
				if (currentPageNumber != topic.getNbPages())
				{
					loadNextPage();
				}
				else
				{
					reloadPage();
				}
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent e)
			{
				if (!isDblTapEnable()) return false;
				reloadPage();
				return true;
			}
		});
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		WebView postsWV = getWebView();
		if (postsWV != null) postsWV.destroy();
		((WebView) findViewById(R.id.loading)).destroy();
		if (posts != null) posts.clear();
		super.hideWikiSmiliesResults(getSmiliesLayout());
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (postDialog != null) postDialog.dismiss();
	}

	@Override
	public void onConfigurationChanged(Configuration conf)
	{
		super.onConfigurationChanged(conf);
		WebView webView = getWebView();
		if (webView != null)
		{
			Display display = getWindowManager().getDefaultDisplay();
			int width = Math.round(display.getWidth() / webView.getScale());
			webView.loadUrl("javascript:loadDynamicCss(" + width + ")");
		}
	}


	private WebView getWebView()
	{
		LinearLayout parent = ((LinearLayout) findViewById(R.id.PostsLayout));
		return ((WebView) parent.getChildAt(3));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.common, menu);
		inflater.inflate(R.menu.posts, menu);
		inflater.inflate(R.menu.misc, menu);
		inflater.inflate(R.menu.nav, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);

		MenuItem menuNav = menu.findItem(R.id.MenuNav);
		if (topic.getNbPages() == 1)
		{
			menuNav.setVisible(topic.getNbPages() != 1);
			menuNav.setEnabled(topic.getNbPages() != 1);
		}
		else
		{
			SubMenu subMenuNav = menu.findItem(R.id.MenuNav).getSubMenu();

			MenuItem menuNavFP = subMenuNav.findItem(R.id.MenuNavFirstPage);
			menuNavFP.setVisible(currentPageNumber != 1);
			menuNavFP.setEnabled(currentPageNumber != 1);

			MenuItem menuNavPP = subMenuNav.findItem(R.id.MenuNavPreviousPage);
			menuNavPP.setVisible(currentPageNumber != 1);
			menuNavPP.setEnabled(currentPageNumber != 1);

			MenuItem menuNavNP = subMenuNav.findItem(R.id.MenuNavNextPage);
			menuNavNP.setVisible(currentPageNumber != topic.getNbPages());
			menuNavNP.setEnabled(currentPageNumber != topic.getNbPages());

			MenuItem menuNavLP = subMenuNav.findItem(R.id.MenuNavLastPage);
			menuNavLP.setVisible(currentPageNumber != topic.getNbPages());
			menuNavLP.setEnabled(currentPageNumber != topic.getNbPages());

			MenuItem menuNavRefresh =  menuNav.getSubMenu().findItem(R.id.MenuNavRefresh);
			menuNavRefresh.setVisible(isLoggedIn() && topic.getStatus() != TopicStatus.LOCKED);
			menuNavRefresh.setEnabled(isLoggedIn() && topic.getStatus() != TopicStatus.LOCKED);

			MenuItem refresh = menu.findItem(R.id.MenuRefresh);
			refresh.setVisible(!isLoggedIn() || topic.getStatus() == TopicStatus.LOCKED);
			refresh.setEnabled(!isLoggedIn() || topic.getStatus() == TopicStatus.LOCKED);
		}

		MenuItem addPost = menu.findItem(R.id.MenuAddPost);
		addPost.setVisible(isLoggedIn() && topic.getStatus() != TopicStatus.LOCKED);
		boolean lockAddPost = false;
		for(String q : quotes.values()) if (lockAddPost = POST_LOADING.equals(q)) break;
		addPost.setEnabled(!lockAddPost && isLoggedIn() && topic.getStatus() != TopicStatus.LOCKED);
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
				case R.id.MenuAddPost :
					String postContent = null;
					if (!quotes.isEmpty())
					{
						StringBuffer data = new StringBuffer("");
						for (Long postId : new TreeSet<Long>(quotes.keySet())) data.append(quotes.get(postId) + "\n");
						postContent = data.substring(0, data.length() - 1);
						quotes.clear();
					}
					showAddPostDialog(PostCallBackType.ADD, postContent);
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

	public void setPosts(List<Post> posts)
	{
		this.posts = posts;
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
		topicPageNumber.setText((topic.getNbPages() != -1 ? "P." + currentPageNumber + "/" + topic.getNbPages() + " " : ""));
	}

	@Override
	protected void loadFirstPage()
	{
		loadPosts(topic, 1);	
	}

	@Override
	protected void loadPreviousPage()
	{
		loadPosts(topic, currentPageNumber - 1);	
	}

	@Override
	protected void loadUserPage()
	{
		new PageNumberDialog(topic.getNbPages())
		{
			protected void onValidate(int pageNumber)
			{
				loadPosts(topic, pageNumber);
			}
		}.show();
	}

	@Override
	protected void loadNextPage()
	{
		loadPosts(topic, currentPageNumber + 1);
	}

	@Override
	protected void loadLastPage()
	{
		loadPosts(topic, topic.getNbPages());
	}

	@Override
	protected void reloadPage()
	{
		reloadPage(false);
	}
	
	protected void reloadPage(boolean fromCache)
	{
		currentScrollY = getWebView().getScrollY();
		if (!fromCache)
		{
			loadPosts(topic, currentPageNumber);
		}
		else
		{
			refreshPosts(posts);
		}
	}
	
	@Override
	protected void redrawPage()
	{
		reloadPage(true);
	}
	
	@Override
	protected void goBack()
	{
		Category cat = fromAllCats ? Category.ALL_CATS : topic.getCategory();
		loadTopics(cat, fromType, 1, false);
	}

	private void updateButtonsStates()
	{
		SlidingDrawer nav = (SlidingDrawer) findViewById(R.id.Nav);
		TextView topicTitle = (TextView) findViewById(R.id.TopicTitle);
		if (topic.getNbPages() == 1)
		{
			nav.setVisibility(View.GONE);
			topicTitle.setPadding(5, 0, 5, 0);
		}
		else
		{
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
			buttonLP.setEnabled(currentPageNumber != topic.getNbPages());
			buttonLP.setAlpha(currentPageNumber != topic.getNbPages() ? 255 : 105);
		}
	}

	private void attachEvents()
	{
		final TextView topicTitle = (TextView) findViewById(R.id.TopicTitle);
		topicTitle.setOnClickListener(new OnClickListener()
		{	
			public void onClick(View v)
			{
				 TextView topicTitle = (TextView) v;
				 topicTitle.setEllipsize(topicTitle.getEllipsize() == TruncateAt.MARQUEE ? TruncateAt.END : TruncateAt.MARQUEE);
			}
		});
		
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

		ImageView buttonLP = (ImageView) findViewById(R.id.ButtonNavLastPage);
		buttonLP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				loadLastPage();	
			}
		});
	}

	public void refreshPosts(List<Post> posts)
	{
		innerDisplayPosts(posts, true);
	}

	private void displayPosts(List<Post> posts)
	{
		innerDisplayPosts(posts, false);
	}

	private void innerDisplayPosts(List<Post> posts, boolean refresh)
	{
		final LinearLayout parent = ((LinearLayout) findViewById(R.id.PostsLayout));
		WebView oldWebView = getWebView();

		final WebView webView = new WebView(this);
		webView.setFocusable(true);
		webView.setFocusableInTouchMode(false); 
		webView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		webView.setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				return event != null ? gestureDetector.onTouchEvent(event) : false;
			}
		});
		webView.addJavascriptInterface(new Object()
		{
			@SuppressWarnings("unused")
			public void openQuickActionWindow(final long postId, final boolean isMine, final int yOffset)
			{
				if (yOffset >= 0 && !lockQuickAction && topic.getStatus() != TopicStatus.LOCKED)
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							SparseIntArray configuration = new SparseIntArray();
							configuration.put(QuickActionWindow.Config.WINDOW_LAYOUT, R.layout.quick_action_window);
							configuration.put(QuickActionWindow.Config.ITEM_LAYOUT, R.layout.quick_action_item);
							configuration.put(QuickActionWindow.Config.CONTAINER, R.id.QuickActions);
							configuration.put(QuickActionWindow.Config.ITEM_ICON, R.id.QuickActionIcon);
							configuration.put(QuickActionWindow.Config.WINDOW_ANIMATION_STYLE, R.style.Animation_QuickActionWindow);
							configuration.put(QuickActionWindow.Config.ARROW_OFFSET, -2);
							HFR4droidQuickActionWindow window = HFR4droidQuickActionWindow.getWindow(PostsActivity.this, configuration);

							final StringBuilder postLink = new StringBuilder(getDataRetriever().getBaseUrl() + "/forum2.php?config=hfr.inc");
							postLink.append("&cat=").append(topic.getCategory().getId());
							postLink.append("&post=").append(topic.getId());
							postLink.append("&page=").append(currentPageNumber);
							postLink.append("#t").append(postId);
							
							if (isMine)
							{
								QuickActionWindow.Item edit = new QuickActionWindow.Item(PostsActivity.this, "", android.R.drawable.ic_menu_edit, new PostCallBack(PostCallBackType.EDIT, postId, true) 
								{
									@Override
									protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
									{
										String content = getDataRetriever().getPostContent(p);
										return content.substring(0, content.length() - 1);
									}

									@Override
									protected void onActionExecute(String data)
									{
										showAddPostDialog(PostCallBackType.EDIT, data, postId);	
									}
								});
								window.addItem(edit);	

								QuickActionWindow.Item delete = new QuickActionWindow.Item(PostsActivity.this, "", android.R.drawable.ic_menu_delete, new PostCallBack(PostCallBackType.DELETE, postId, true, true)
								{									
									@Override
									protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
									{
										return getMessageSender().deleteMessage(p, getDataRetriever().getHashCheck()) ? "1" : "0";
									}

									@Override
									protected void onActionExecute(String data)
									{
										if (data.equals("1"))
										{
											WebView webView = getWebView();
											webView.loadUrl("javascript:removePost(" + postId + ")");
										}
										else
										{
											Toast.makeText(PostsActivity.this, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
										}
									}
								});
								window.addItem(delete);
							}

							QuickActionWindow.Item quote = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_quote, new PostCallBack(PostCallBackType.QUOTE, postId, true)
							{									
								@Override
								protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
								{
									return getDataRetriever().getQuote(p);
								}

								@Override
								protected void onActionExecute(String data)
								{
									showAddPostDialog(PostCallBackType.QUOTE, data);
								}
							});							
							window.addItem(quote);

							boolean quoteExists = quotes.get(postId) != null;
							QuickActionWindow.Item multiQuote = new QuickActionWindow.Item(PostsActivity.this, "",
									quoteExists ? R.drawable.ic_menu_multi_quote_moins : R.drawable.ic_menu_multi_quote_plus,
											new PostCallBack(quoteExists ? PostCallBackType.MULTIQUOTE_REMOVE : PostCallBackType.MULTIQUOTE_ADD, postId, false)
							{								
								@Override
								protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
								{
									String data = "";
									switch (type)
									{
										case MULTIQUOTE_ADD:
											quotes.put(postId, POST_LOADING);
											data = getDataRetriever().getQuote(p);
											break;
	
										case MULTIQUOTE_REMOVE:
											if (!POST_LOADING.equals(quotes.get(postId)))
											{
												quotes.remove(p.getId());
											}
											break;
	
										default:
											break;
									}
									return data;
								}

								@Override
								protected void onActionExecute(String data)
								{
									switch (type)
									{
										case MULTIQUOTE_ADD:
											if (data.equals(""))
											{
												quotes.remove(postId);
											}
											else
											{
												quotes.put(postId, data);
											}
											break;
	
										default:
											break;
									}
								}								
							});
							window.addItem(multiQuote);
							
							QuickActionWindow.Item addFavorite = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_star, new PostCallBack(PostCallBackType.FAVORITE, postId, true)
							{									
								@Override
								protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
								{
									return getMessageSender().addFavorite(p);
								}

								@Override
								protected void onActionExecute(String data)
								{
									Toast.makeText(PostsActivity.this, data, Toast.LENGTH_SHORT).show();
								}
							});							
							if (isLoggedIn()) window.addItem(addFavorite);
							
							QuickActionWindow.Item copyLink = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_copy, new QuickActionWindow.Item.Callback()
							{	
								public void onClick(QuickActionWindow window, Item item, View anchor)
								{
									ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
									clipboard.setText(postLink);
									Toast.makeText(PostsActivity.this, getText(R.string.copy_post_link), Toast.LENGTH_SHORT).show();
								}
							});					
							window.addItem(copyLink);
							
							QuickActionWindow.Item shareLink = new QuickActionWindow.Item(PostsActivity.this, "", android.R.drawable.ic_menu_share, new QuickActionWindow.Item.Callback()
							{	
								public void onClick(QuickActionWindow window, Item item, View anchor)
								{
									Intent sendIntent = new Intent(Intent.ACTION_SEND); 
									sendIntent.setType("text/plain");
									sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, postLink.toString());
									startActivity(Intent.createChooser(sendIntent, getString(R.string.share_post_link_title))); 
								}
							});					
							window.addItem(shareLink);
							
							window.show(((LinearLayout) findViewById(R.id.TopicTitle).getParent()), Math.round(yOffset * webView.getScale()));
						}
					});
				}
			}

			@SuppressWarnings("unused")
			public void handleQuote(String url)
			{
				try
				{
					MDUrlParser urlParser = new HFRUrlParser(getDataRetriever());
					if (urlParser.parseUrl(getDataRetriever().getBaseUrl() + url))
					{
						BasicElement element = urlParser.getElement();
						if (element == null)
						{
							loadCats(false);
						}
						else if (element instanceof Category)
						{
							loadTopics((Category) element, urlParser.getType(), urlParser.getPage(), false);
						}
						else if (element instanceof Topic)
						{
							Topic t = (Topic) element;
							if (t.getId() == topic.getId())
							{
								if (urlParser.getPage() == currentPageNumber)
								{
									// C'est le m�me topic et la m�me page, on scroll
									webView.loadUrl("javascript:scrollToElement(" + t.getLastReadPost() + ")");
								}
								else
								{
									// Page diff�rente, on change de page
									topic.setLastReadPost(t.getLastReadPost());
									loadPosts(topic, urlParser.getPage());
								}
							}
							else
							{
								// Topic diff�rent, on change de topic
								loadPosts(t, urlParser.getPage());
							}
						}
					}
				}
				catch (DataRetrieverException e)
				{
					error(getString(R.string.error_dispatching_url), e, true, false);
				}
			}

			@SuppressWarnings("unused")
			public void editKeywords(final String code)
			{
				final ProgressDialog progressDialog = new ProgressDialog(PostsActivity.this);
				progressDialog.setMessage(getString(R.string.getting_keywords, code));
				progressDialog.setIndeterminate(true);
				new AsyncTask<String, Void, String>()
				{
					@Override
					protected void onPreExecute() 
					{
						progressDialog.show();
					}

					@Override
					protected String doInBackground(String... params)
					{
						String keywords = "";
						try
						{
							keywords = getDataRetriever().getKeywords(params[0]);
						}
						catch (DataRetrieverException e)
						{
							keywords = null;
							error(e, true, true);
						}
						return keywords;
					}

					@Override
					protected void onPostExecute(String keywords)
					{
						if (keywords == null)
						{
							progressDialog.dismiss();
							return;
						}
						Context mContext = getApplicationContext();
						LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
						View layout = inflater.inflate(R.layout.keywords, null);

						AlertDialog.Builder builder = new AlertDialog.Builder(PostsActivity.this);
						builder.setTitle(getString(R.string.keywords_title, code)); 
						builder.setView(layout);
						final EditText keywordsView = (EditText) layout.findViewById(R.id.inputKeywords);
						keywordsView.setText(keywords);

						builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener()
						{  
							public void onClick(DialogInterface dialog, int whichButton)
							{
								final ProgressDialog progressDialog = new ProgressDialog(PostsActivity.this);
								progressDialog.setMessage(getString(R.string.keywords_loading));
								progressDialog.setIndeterminate(true);
								new AsyncTask<Void, Void, String>()
								{
									@Override
									protected void onPreExecute() 
									{
										progressDialog.show();
									}

									@Override
									protected String doInBackground(Void... params)
									{
										String strResponse = null;
										try
										{
											strResponse = getMessageSender().setKeywords(getDataRetriever().getHashCheck(), code, keywordsView.getText().toString());
										} 
										catch (HFR4droidException e) // MessageSenderException, DataRetrieverException
										{
											error(e, true, true);
										}
										return strResponse;
									}

									@Override
									protected void onPostExecute(String strResponse)
									{
										progressDialog.dismiss();
										if (strResponse != null)
										{
											Toast.makeText(PostsActivity.this, strResponse, Toast.LENGTH_SHORT).show();
										}
									}
								}.execute();
							}
						});
						builder.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which){}
						});
						
						progressDialog.dismiss();
						builder.create().show();
					}
				}.execute(code);
			}
		}, "HFR4Droid");

		final WebView loading = (WebView) findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		if (!refresh)
		{
			loading.loadData("<html><body style=\"text-align: center; margin-top: 150px; background-color:#F7F7F7;\"><img src=\"data:image/gif;base64,R0lGODlhKwALAPEAAP%2F%2F%2FwAAAIKCggAAACH%2FC05FVFNDQVBFMi4wAwEAAAAh%2FhpDcmVhdGVkIHdpdGggYWpheGxvYWQuaW5mbwAh%2BQQJCgAAACwAAAAAKwALAAACMoSOCMuW2diD88UKG95W88uF4DaGWFmhZid93pq%2BpwxnLUnXh8ou%2BsSz%2BT64oCAyTBUAACH5BAkKAAAALAAAAAArAAsAAAI9xI4IyyAPYWOxmoTHrHzzmGHe94xkmJifyqFKQ0pwLLgHa82xrekkDrIBZRQab1jyfY7KTtPimixiUsevAAAh%2BQQJCgAAACwAAAAAKwALAAACPYSOCMswD2FjqZpqW9xv4g8KE7d54XmMpNSgqLoOpgvC60xjNonnyc7p%2BVKamKw1zDCMR8rp8pksYlKorgAAIfkECQoAAAAsAAAAACsACwAAAkCEjgjLltnYmJS6Bxt%2Bsfq5ZUyoNJ9HHlEqdCfFrqn7DrE2m7Wdj%2F2y45FkQ13t5itKdshFExC8YCLOEBX6AhQAADsAAAAAAAAAAAA%3D\" alt=\"loading\" /></body></html>", "text/html", "UTF-8");
		}

		StringBuffer js = new StringBuffer("<script type=\"text/javascript\">");
		js.append("function swap_spoiler_states(obj){var div=obj.getElementsByTagName('div');if(div[0]){if(div[0].style.visibility==\"visible\"){div[0].style.visibility='hidden';}else if(div[0].style.visibility==\"hidden\"||!div[0].style.visibility){div[0].style.visibility='visible';}}}");
		js.append("function scrollToElement(id) {var elem = document.getElementById(id); var x = 0; var y = 0; while (elem != null) { x += elem.offsetLeft; y += elem.offsetTop; elem = elem.offsetParent; } window.scrollTo(x, y); }");
		js.append("function removePost(id) { var header = document.getElementById(id); header.parentNode.removeChild(header.nextSibling); if (header.nextSibling.className == 'HFR4droid_post') header.parentNode.removeChild(header.nextSibling); header.parentNode.removeChild(header); };");
		js.append("function openQuickActionWindow(postId, isMine) {var elem = document.getElementById(postId); var yOffset = 0; while (elem != null) { yOffset += elem.offsetTop; elem = elem.offsetParent; } window.HFR4Droid.openQuickActionWindow(postId, isMine, yOffset - window.scrollY); }");
		js.append("var loadDynamicCss = function(width) { var headID = document.getElementsByTagName('head')[0]; var styles = headID.getElementsByTagName('style'); for (var i=1;i<styles.length;i++) headID.removeChild(styles[i]); var cssNode = document.createElement('style'); cssNode.type = 'text/css'; cssNode.appendChild(document.createTextNode('");
		js.append("ol { width:' + (Math.round(width * 0.80) - 40) + 'px; }");
		js.append(".citation p, .oldcitation p, .quote p, .oldquote p, .fixed p, .code p, .spoiler p, .oldspoiler p { width:' + Math.round(width * 0.80) + 'px; }");
		js.append(".HFR4droid_post { width:' + width + 'px; word-wrap: break-word; padding-top: 5px; }");
		js.append(".HFR4droid_content img { max-width: ' + (width - 30) + 'px; }");
		js.append(".citation img, .oldcitation img, .quote img, .oldquote img, .fixed img, .code img, .spoiler img, .oldspoiler img { max-width: ' + (Math.round(width * 0.80) - 15) + 'px; }");
		js.append("')); headID.appendChild(cssNode); };");
		if (topic.getLastReadPost() != -1 || topic.getStatus() == TopicStatus.NEW_MP)
		{
			js.append("window.onload = function () { scrollToElement(\'" + (topic.getStatus() == TopicStatus.NEW_MP ? BOTTOM_PAGE_ID : topic.getLastReadPost()) + "\'); }");
			topic.setLastReadPost(-1);
		}
		js.append("</script>");

		StringBuffer css = new StringBuffer("<style type=\"text/css\">");
		css.append(".u { text-decoration:underline; }");
		css.append("a.cLink { color:#000080; text-decoration:none; }");
		css.append("a.cLink:hover, a.cLing:active  { color:#000080; text-decoration:underline; }");
		css.append(".citation, .oldcitation, .quote, .oldquote, .fixed, .code, .spoiler, .oldspoiler { padding:3px; text-align:left; width:90%; }");
		css.append(".citation, .oldcitation, .quote, .oldquote, .spoiler, .oldspoiler { margin: 8px auto; }");
		css.append(".code, .fixed { background-color:#FFF; border:1px solid #000; color:#000; font-family:'Courier New',Courier,monospace; margin:8px 5px; }");
		css.append(".oldcitation, .oldquote { border:0; }");
		css.append(".quote, .oldquote { font-style:italic; }");
		css.append("table { font-size: 1em; }");
		css.append(".spoiler, .oldspoiler, .citation, .quote { border:1px solid #C0C0C0; background-color:#FFF }");
		css.append("div.masque { visibility:hidden; }");
		css.append(".container { text-align:center; width:100%; }");
		css.append(".s1, .s1Topic { font-size: " + getTextSize(10) + "px; }");
		css.append("p { margin:0; padding:0; }");
		css.append("p, ul { font-size: 0.8em; margin-bottom: 0; margin-top: 0; }");
		css.append("pre { font-size: 0.7em; white-space: pre-wrap }");
		css.append("ol.olcode { font-size: 0.7em; }");
		css.append("body { margin:0; padding:0; background-color:#F7F7F7; }");
		css.append(".HFR4droid_header { width:100%; background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAyCAIAAAASmSbdAAAACXBIWXMAAAsSAAALEgHS3X78AAAAr0lEQVR42i3D61IBYQCA4fc%2B%2FMNiR2qVU7sxzbgFHZaEQU27qG6hf7ElRDmMQ5juilvp%2B%2BKZeQibL5w%2F%2F5J6WpN6XO02liTFs%2FrPf6O%2BwKgt0O05ujXj1JqSkB%2BmxO8nxOS7MVExUh0RqQw5KX9zXP5Ck0sDtGKfo8Inh4UeodseB%2Fmu2CF4I%2BY%2BUHNt1KxovhMw3%2FBfO%2FjkKwflsoVy0cQrZ17x7LszTVxpm8128wedbTsQqibZlwAAAABJRU5ErkJggg%3D%3D\"); height: 50px; text-align: right; }");
		css.append(".HFR4droid_header div { position: absolute; margin: 5px 0 0 5px; width:90%; text-align: left; }");
		css.append(".HFR4droid_header div img { float: left; max-width:60px; max-height:40px; margin-right:5px; }");
		css.append(".HFR4droid_header span.pseudo { color:#FFF; font-size: " + getTextSize(16) + "px; font-weight:bold; }");
		css.append(".HFR4droid_header span.date { display: block; font-style:italic; color:#CDCDCD; font-size: " + getTextSize(12) + "px; margin: ");
		switch (getPoliceSize())
		{
			case 2:
				css.append("2");
				break;

			case 3:
				css.append("0");
				break;
				
			default:
				css.append("5");
				break;
		}
		css.append("px; margin-left:0; }");
		css.append(".HFR4droid_edit_quote { margin-bottom: 5px; padding: 4px; padding-bottom: 3px; background-color: #DEDFDE;  font-style:italic; color:#555; font-size: " + getTextSize(9) + "px; }");
		css.append(".HFR4droid_content { padding: 10px; padding-top: 5px; font-size: " + getTextSize(16) + "px}");
		css.append(".modo_post { background-color: #FFEEEE; }");
		css.append(".HFR4droid_footer { height: 10px; width:100%; background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAA%2FCAMAAAAWu1JmAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAgY0hSTQAAeiYAAICEAAD6AAAAgOgAAHUwAADqYAAAOpgAABdwnLpRPAAAAwBQTFRFhYWFs7SzysrKy8vLyszKzMzMzc3Nzs7Oz8%2FP0NDQ0dHR0dLR0tLS09PT1NTU1dXV1tbW19fX2NjY2dnZ2tna2tra29rb3Nvc3Nzc3dzdAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWKfi1AAAABh0RVh0U29mdHdhcmUAUGFpbnQuTkVUIHYzLjM2qefiJQAAAEFJREFUGFddwkcOgDAQBMHBhCWDCQb%2B%2F1Fai8TBqlKhSoOiDiVdejK3PgkndrchYsXiZkwY0bsO7c9kalCjdAF6ARIIA4Sqnjr8AAAAAElFTkSuQmCC\"); }");
		css.append("</style>");

		Display display = getWindowManager().getDefaultDisplay(); 
		int width = Math.round(display.getWidth() / webView.getScale());
		StringBuffer js2 = new StringBuffer("<script type=\"text/javascript\">");
		js2.append("loadDynamicCss(" + width + ");");
		js2.append("</script>");

		StringBuffer postsContent = new StringBuffer("");
		for (Post p : posts)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy � HH:mm:ss");
			SimpleDateFormat todaySdf = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat check = new SimpleDateFormat("ddMMyyyy");
			boolean today = check.format(new Date()).equals(check.format(p.getDate()));
			String date = today ? todaySdf.format(p.getDate()) : "Le " + sdf.format(p.getDate());
			String avatar = p.getAvatarUrl() != null && isAvatarsEnable() ? "<img alt=\"avatar\" title=\"" + p.getPseudo() + "\" src=\"" + p.getAvatarUrl() + "\" />" : "";
			String pseudoSpan = "<span class=\"pseudo\">" + p.getPseudo() + "</span>";
			String dateSpan = "<span class=\"date\">" + date + "</span>";
			StringBuilder editQuoteDiv = new StringBuilder("");
			if (p.getLastEdition() != null || p.getNbCitations() > 0)
			{
				editQuoteDiv.append("<div class=\"HFR4droid_edit_quote\">");
				if (p.getLastEdition() != null)
				{
					today = check.format(new Date()).equals(check.format(p.getLastEdition()));
					editQuoteDiv.append("Edit� " + (today ? "� " + todaySdf.format(p.getLastEdition()) : "le " + sdf.format(p.getLastEdition())));
				}
				if (p.getNbCitations() > 0)
				{
					editQuoteDiv.append(p.getLastEdition() != null ? ", cit� " + p.getNbCitations() + " fois" : "Cit� " + p.getNbCitations() + " fois");
				}
				editQuoteDiv.append("</div>");
			}
			String header = "<div id=\"" + p.getId() + "\" class=\"HFR4droid_header\" onclick=\"openQuickActionWindow(" + p.getId() + ", " + p.isMine() + ")\"><div>" + avatar + pseudoSpan + "<br />" + dateSpan + "</div></div>";

			String content = "";
			content = "<div class=\"HFR4droid_post";
			if (p.isModo()) content += " modo_post";
			content += "\">" + editQuoteDiv + "<div class=\"HFR4droid_content\"";
			content += ">" + p.getContent() + "</div></div>";
			content = content.replaceAll("<b\\s*class=\"s1\"><a href=\"(.*?)\".*?>(.*?)</a></b>", "<b onclick=\"window.HFR4Droid.handleQuote('$1');\" class=\"s1\">$2</b>");
			content = content.replaceAll("<img\\s*src=\"http://forum\\-images\\.hardware\\.fr/images/perso/(.*?)\"\\s*alt=\"(.*?)\"", "<img onclick=\"window.HFR4Droid.editKeywords('$2');\" src=\"http://forum-images.hardware.fr/images/perso/$1\" alt=\"$2\"");
			if (!isSmileysEnable()) content = content.replaceAll("<img\\s*src=\"http://forum\\-images\\.hardware\\.fr.*?\"\\s*alt=\"(.*?)\".*?/>", "$1");
			if (!isImgsEnable()) content = content.replaceAll("<img\\s*src=\"http://[^\"]*?\"\\s*alt=\"http://[^\"]*?\"\\s*title=\"(http://.*?)\".*?/>", "<a href=\"$1\" target=\"_blank\" class=\"cLink\">$1</a>");
			content = content.replaceAll("ondblclick=\".*?\"", "");
			postsContent.append(header);
			postsContent.append(content);
		}
		postsContent.append("<div id=\"" + BOTTOM_PAGE_ID + "\" class=\"HFR4droid_footer\" />");
		WebSettings settings = webView.getSettings();
		settings.setDefaultTextEncodingName("UTF-8");
		settings.setJavaScriptEnabled(true);
		webView.setBackgroundColor(0);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

		// PAS DE CACHE ���
		//getApplicationContext().deleteDatabase("webview.db");
		//getApplicationContext().deleteDatabase("webviewCache.db");
		//webView.clearCache(true);
		//settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.PostsProgressBar);
		progressBar.setVisibility(View.VISIBLE);
		webView.setWebChromeClient(new WebChromeClient()
		{
			public void onProgressChanged(WebView view, int progress)
			{
				progressBar.setProgress(progress);
				if (progress > 15 && loading.getVisibility() == View.VISIBLE)
				{
					loading.setVisibility(View.GONE);
					parent.addView(webView);
				}
				if (progress == 100)
				{
					if (currentScrollY != -1)
					{
						view.scrollTo(0, currentScrollY);
						currentScrollY = -1;
					}
					lockQuickAction = false;
					Animation anim = AnimationUtils.loadAnimation(PostsActivity.this, R.anim.hide_progress_bar);
					anim.setAnimationListener(new AnimationListener()
					{
						public void onAnimationStart(Animation animation) {}

						public void onAnimationRepeat(Animation animation) {}

						public void onAnimationEnd(Animation animation)
						{
							progressBar.setVisibility(View.GONE);
						}
					});
					progressBar.startAnimation(anim);
				}
			}
		});
		//webView.loadData("<html><head>" + fixHTML(js.toString()) + css + fixHTML(js2.toString()) + "</head><body>" + fixHTML(postsContent.toString()) + "</body></html>", "text/html", "UTF-8");
		webView.loadDataWithBaseURL(getDataRetriever().getBaseUrl(), "<html><head>" + js.toString() + css.toString() + js2.toString() + "</head><body>" + postsContent.toString() + "</body></html>", "text/html", "UTF-8", null);
		if (oldWebView != null)
		{
			oldWebView.destroy();
			parent.removeView(oldWebView);
		}
		if (refresh) updateButtonsStates();
	}

	private void showAddPostDialog(PostCallBackType type, String data)
	{
		showAddPostDialog(type, data, -1);
	}

	private void showAddPostDialog(final PostCallBackType type, String data, long postId)
	{
		if (postDialog == null)
		{
			postDialog = new Dialog(this);
			postDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			postDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.new_post_content, null);
			postDialog.setContentView(layout);
			addPostButtons(layout);
			((EditText) postDialog.findViewById(R.id.InputPostContent)).setTextSize(getTextSize(14));

			postDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
			{
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
				{
					if (keyCode == KeyEvent.KEYCODE_BACK)
					{
						View firstChild = layout.getChildAt(0);
						if (postDialog.isShowing() && firstChild instanceof WebView)
						{
							hideWikiSmiliesResults(layout);
							return true;
						}
					}
					return false;
				}
			});
			postDialog.setOnDismissListener(new OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					if (type == PostCallBackType.EDIT)
					{
						EditText postContent = (EditText) postDialog.findViewById(R.id.InputPostContent);
						postContent.setText("");
					}
				}
			});
		}

		this.postId = postId;
		final EditText postContent = (EditText) postDialog.findViewById(R.id.InputPostContent);
		if (data != null) postContent.setText(data);
		postContent.requestFocus();
		Selection.setSelection(postContent.getText(), postContent.length());
		EditText smileyTag = (EditText) postDialog.findViewById(R.id.inputSmileyTag);
		smileyTag.setText("");
		postDialog.show();		
	}
	
	protected ViewGroup getSmiliesLayout()
	{
		return postDialog != null ? (ViewGroup) postDialog.findViewById(R.id.PostContainer).getParent() : null;
	}
	
	protected void showWikiSmiliesResults(ViewGroup layout)
	{
		layout.findViewById(R.id.PostContainer).setVisibility(View.GONE);
	}
	
	@Override
	protected void hideWikiSmiliesResults(ViewGroup layout)
	{
		super.hideWikiSmiliesResults(layout);
		layout.findViewById(R.id.PostContainer).setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ImagePicker.CHOOSE_PICTURE && data != null)
		{
			Bundle extras = data.getExtras();
			if (extras != null)
			{
				String url = (String) extras.get(ImagePicker.FINAL_URL);
				insertBBCode((EditText) postDialog.findViewById(R.id.InputPostContent), url, "");
				postDialog.show();
			}
		}
	}

	@Override
	protected void setOkButtonClickListener(Button okButton)
	{
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				final EditText postContent = (EditText) postDialog.findViewById(R.id.InputPostContent);
				new ValidateMessageAsynckTask()
				{
					@Override
					protected boolean canExecute()
					{
						if (postContent.getText().length() == 0)
						{
							Toast.makeText(PostsActivity.this, R.string.missing_post_content, Toast.LENGTH_SHORT).show();
							return false;
						}

						return true;
					}

					@Override
					protected ResponseCode validateMessage() throws MessageSenderException, DataRetrieverException
					{
						if (postId != -1)
						{
							Post p = new Post(postId);
							p.setTopic(topic);
							return getMessageSender().editMessage(p, getDataRetriever().getHashCheck(), postContent.getText().toString(), isSignatureEnable());
						}
						else
						{
							return getMessageSender().postMessage(topic, getDataRetriever().getHashCheck(), postContent.getText().toString(), isSignatureEnable());
						}
					}

					@Override
					protected boolean handleCodeResponse(ResponseCode code)
					{
						if (!super.handleCodeResponse(code))
						{
							switch (code)
							{	
								case POST_EDIT_OK: // Edit ok
									postContent.setText("");
									postDialog.dismiss();
									topic.setLastReadPost(postId);
									reloadPage();
									return true;									
		
								case POST_ADD_OK: // New post ok
									postContent.setText("");
									postDialog.dismiss();
									topic.setLastReadPost(BOTTOM_PAGE_ID);
									if (currentPageNumber == topic.getNbPages()) reloadPage();
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
				}.execute();
			}
		});
	}

	@Override
	protected void setCancelButtonClickListener(Button cancelButton)
	{
		cancelButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				postDialog.dismiss();	
			}
		});
	}

	/* Classes internes */

	private abstract class PostCallBack extends AsyncTask<Void, Void, String> implements QuickActionWindow.Item.Callback
	{
		protected PostCallBackType type;
		protected long postId;
		private boolean progress;
		private boolean confirm;
		private ProgressDialog progressDialog;

		public PostCallBack(PostCallBackType type, long postId, boolean progress)
		{
			this.type = type;
			this.postId = postId;
			this.progress = progress;
			this.confirm = false;
			progressDialog = null;
		}

		public PostCallBack(PostCallBackType type, long postId, boolean progress, boolean confirm)
		{
			this(type, postId, progress);
			this.confirm = confirm;
		}

		protected abstract String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException;

		protected abstract void onActionExecute(String data);

		public void onClick(QuickActionWindow window, Item item, View anchor)
		{
			if (confirm)
			{
				new AlertDialog.Builder(PostsActivity.this)
				.setTitle(getString(type.getKey() + "_title"))
				.setMessage(getString(type.getKey() + "_message"))
				.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1)
					{
						execute();
					}

				})
				.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) {}
				})
				.show();			
			}
			else
			{
				execute();
			}
		}

		@Override
		protected void onPreExecute()
		{
			if (progress)
			{
				progressDialog = ProgressDialog.show(PostsActivity.this, null, getString(type.getKey() + "_loading"), true);
			}
		}

		@Override
		protected String doInBackground(Void... params)
		{
			String data = "";
			try
			{
				Post p = new Post(postId);
				p.setTopic(topic);
				data = doActionInBackground(p);
			}
			catch (final Exception e) // DataRetrieverException, MessageSenderException
			{
				data = null;
				error(e, true, true);
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result)
		{
			if (result != null)
			{
				onActionExecute(result);
			}
			if (progress) progressDialog.dismiss();
		}
	}
}