package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.HFR4droidException;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.BasicElement;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.Theme;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicStatus;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.core.data.HFRUrlParser;
import info.toyonos.hfr4droid.core.data.MDUrlParser;
import info.toyonos.hfr4droid.core.message.HFRMessageResponse;
import info.toyonos.hfr4droid.core.message.HFRMessageSender.ResponseCode;
import info.toyonos.hfr4droid.core.message.MessageSenderException;
import info.toyonos.hfr4droid.core.utils.HttpClient;
import info.toyonos.hfr4droid.core.utils.PatchInputStream;
import info.toyonos.hfr4droid.service.MpNotifyService;
import info.toyonos.hfr4droid.util.asynctask.DataRetrieverAsyncTask;
import info.toyonos.hfr4droid.util.asynctask.MessageResponseAsyncTask;
import info.toyonos.hfr4droid.util.asynctask.ValidateMessageAsynckTask;
import info.toyonos.hfr4droid.util.dialog.PageNumberDialog;
import info.toyonos.hfr4droid.util.listener.SimpleNavOnGestureListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.text.Selection;
import android.text.TextUtils.TruncateAt;
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

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
	private static final String POST_LOADING 	= ">¤>¤>¤>¤>¤...post_loading...<¤<¤<¤<¤<¤";
	private static final String DOWNLOAD_DIR 	= "/HFR4droid/";

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

	protected Topic topic;
	protected List<Post> posts;
	private TopicType fromType;
	private boolean fromAllCats;

	private GestureDetector gestureDetector;
	private int currentScrollY;

	private Dialog postDialog;

	protected Map<Long, String> quotes;
	protected boolean lockQuickAction;
	
	protected DrawableDisplayType currentAvatarsDisplayType = null;
	protected DrawableDisplayType currentSmileysDisplayType = null;
	protected DrawableDisplayType currentImgsDisplayType = null;
	
	private boolean isAvatarsEnable = true;
	private boolean isSmileysEnable = true;
	private boolean isImgsEnable = true;
	
	private final HttpClient<Bitmap> imgHttpClient = new HttpClient<Bitmap>()
	{		
		@Override
		protected Bitmap transformStream(InputStream is) throws IOException
		{
			return BitmapFactory.decodeStream(new PatchInputStream(is));
		}
	};	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posts);
		applyTheme(currentTheme);
		attachEvents();

		currentScrollY = -1;
		postDialog = null;
		quotes = Collections.synchronizedMap(new HashMap<Long, String>());
		lockQuickAction = true;

		currentAvatarsDisplayType = getAvatarsDisplayType();
		currentSmileysDisplayType = getSmileysDisplayType();
		currentImgsDisplayType = getImgsDisplayType();
		
		Bundle bundle = this.getIntent().getExtras();
		onCreateInit(bundle);

		if (topic != null)
		{
			setTitle();
			updateButtonsStates();
			if (topic.getCategory().equals(Category.MPS_CAT))
			{
				clearNotifications();
				synchronized (MpNotifyService.class)
				{
					if (MpNotifyService.currentNewMps > 0 && topic.getStatus() == TopicStatus.NEW_MP) MpNotifyService.currentNewMps--;	
				}
			}
		}

		gestureDetector = new GestureDetector(new SimpleNavOnGestureListener(this)
		{
			@Override
			protected void onLeftToRight(MotionEvent e1, MotionEvent e2)
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
			protected void onRightToLeft(MotionEvent e1, MotionEvent e2)
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
	
	@SuppressWarnings("unchecked")
	protected void onCreateInit(Bundle bundle)
	{
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
	protected void onRestart()
	{
		super.onRestart();
		LinearLayout searchPanel = (LinearLayout) findViewById(R.id.SearchPostsPanel);
		searchPanel.setVisibility(View.GONE);
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_SEARCH)
		{
			toggleSearchPosts();
		}
		return super.onKeyDown(keyCode, event);
	}

	private WebView getWebView()
	{
		LinearLayout parent = ((LinearLayout) findViewById(R.id.PostsLayout));
		return ((WebView) parent.getChildAt(4));
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
			
            MenuItem menuNavSubCats =  menuNav.getSubMenu().findItem(R.id.MenuNavSubCats);
            menuNavSubCats.setVisible(false);
            menuNavSubCats.setEnabled(false);

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
		new PageNumberDialog(this, currentPageNumber, topic.getNbPages())
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
		currentScrollY = getWebView().getScrollY();
		loadPosts(topic, currentPageNumber);
	}
	
	@Override
	protected void redrawPage()
	{
		currentScrollY = getWebView().getScrollY();
		displayPosts(posts);
	}
	
	@Override
	protected void goBack()
	{
		Category cat = fromAllCats ? Category.ALL_CATS : topic.getCategory();
		loadTopics(cat, fromType, 1, false);
	}

	protected void updateButtonsStates()
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

	protected void attachEvents()
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
		
		topicTitle.setOnLongClickListener(new OnLongClickListener()
		{
			public boolean onLongClick(View v)
			{
				toggleSearchPosts();
				return true;
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
		
		Button buttonSearchPosts = (Button) findViewById(R.id.ButtonSearchPosts);
		buttonSearchPosts.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				EditText pseudo = (EditText) findViewById(R.id.SearchPostsPseudo);
				EditText word = (EditText) findViewById(R.id.SearchPostsWord);
				Post fromPost = PostsActivity.this instanceof PostsSearchActivity ? null : posts.get(0);
				searchPosts(topic, pseudo.getText().toString(), word.getText().toString(), fromPost, false);
			}
		});
	}

	public void refreshPosts(List<Post> posts)
	{
		innerDisplayPosts(posts, true);
	}

	protected void displayPosts(List<Post> posts)
	{
		innerDisplayPosts(posts, false);
	}

	private void innerDisplayPosts(List<Post> posts, boolean refresh)
	{
		final LinearLayout parent = ((LinearLayout) findViewById(R.id.PostsLayout));
		WebView oldWebView = getWebView();

	    final WebView webView = new WebView(this)
        {
            @Override
            public boolean onTouchEvent(MotionEvent ev)
            {
                boolean result = false;
                try
                {
                	result = ev != null ? gestureDetector.onTouchEvent(ev) : false;
                	if (!result)
                	{
                		result = super.onTouchEvent(ev);
                	}
                }
                catch (NullPointerException e)
                {
                	error(e);
                }
                return result;
            }
        };

        registerForContextMenu(webView);
        webView.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
        	abstract class ImageCallback implements Runnable
        	{
        		protected File image = null;  		
        			
				public void run(File image)
				{
					this.image = image;
					run();
				} 
        	}
        	
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
			{
                HitTestResult result = ((WebView) v).getHitTestResult();
                if (result.getType() == HitTestResult.IMAGE_TYPE || result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
                {
                	final String url = result.getExtra();
                	if (url.indexOf("http://forum-images.hardware.fr") != -1) return;

                	MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener()
                    {
                		private void saveImage(final String url, final ImageCallback callback)
                		{                    		
    						final ProgressDialog progressDialog = new ProgressDialog(PostsActivity.this);
    						progressDialog.setMessage(getString(R.string.save_image));
    						progressDialog.setIndeterminate(true);
    						new AsyncTask<String, Void, File>()
    						{
    							@Override
    							protected void onPreExecute() 
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

    							@Override
    							protected File doInBackground(String... url)
    							{
    								try
									{
										Bitmap imgBitmap = imgHttpClient.getResponse(url[0]);
	                        			File dir = new File(Environment.getExternalStorageDirectory() + DOWNLOAD_DIR);
	                        			if (!dir.exists()) dir.mkdirs();
	                        			String originalFileName = url[0].substring(url[0].lastIndexOf('/') + 1, url[0].length());
	                        			String newFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + ".png";

	                        			File f = new File(Environment.getExternalStorageDirectory() + DOWNLOAD_DIR, newFileName);
	                        	        OutputStream os = new FileOutputStream(f);
	                        	        imgBitmap.compress(CompressFormat.PNG, 90, os);
	                        	        os.close();
	                        	        
	                        	        return f;
									}
									catch (Exception e)
									{
										error(getString(R.string.save_image_failed), e, true, false);
										return null;
									}
    							}

    							@Override
    							protected void onPostExecute(File imageFile)
    							{
    								progressDialog.dismiss();
                        	        if (callback != null) callback.run(imageFile);
    							}
    						}.execute(url);
                		}
                		
                        public boolean onMenuItemClick(MenuItem item)
                        {
                			switch (item.getItemId())
                			{
                				case R.id.SaveImage:
                					saveImage(url, new ImageCallback()
									{
										public void run()
										{
											if (image != null)
											{
												Toast.makeText(PostsActivity.this, getString(R.string.save_image_ok, image.getParent()), Toast.LENGTH_LONG).show();
											}
										}
									});
                					break;

                				case R.id.ShareImage:
                					saveImage(url, new ImageCallback()
									{
										public void run()
										{
											if (image != null)
											{
			                        	        Intent share = new Intent(Intent.ACTION_SEND);
			                        	        share.setType("image/*");
			                        	        Uri uri = Uri.fromFile(image);
			                        	        share.putExtra(Intent.EXTRA_STREAM, uri);
			                        	        startActivity(Intent.createChooser(share, getString(R.string.share_image)));
											}
										}
									});
                					break;
                					
                				case R.id.OpenImage:
                					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                					startActivity(intent);
                					break;
                			}
                			return true;
                        }
                    };

                	menu.setHeaderTitle(url);
                    menu.add(0, R.id.SaveImage, 0, R.string.save_image_item).setOnMenuItemClickListener(handler);
                    menu.add(0, R.id.ShareImage, 0, R.string.share_image_item).setOnMenuItemClickListener(handler);
                    menu.add(0, R.id.OpenImage, 0, R.string.open_image_item).setOnMenuItemClickListener(handler);
                }
			}
		});
        

		webView.setFocusable(true);
		webView.setFocusableInTouchMode(false); 
		webView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		webView.addJavascriptInterface(new Object()
		{
			@SuppressWarnings("unused")
			public void openQuickActionWindow(final long postId, final boolean isMine, final int yOffset)
			{
				if (yOffset >= 0 && !lockQuickAction)
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
							
							addQuickActionWindowItems(window, postId, isMine);
							View spp = findViewById(R.id.SearchPostsPanel);
							View anchor = spp.getVisibility() == View.VISIBLE ? spp : ((LinearLayout) findViewById(R.id.TopicTitle).getParent());
							window.show(anchor, Math.round(yOffset * webView.getScale()));
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
									// C'est le même topic et la même page, on scroll
									webView.loadUrl("javascript:scrollToElement(" + t.getLastReadPost() + ")");
								}
								else
								{
									// Page différente, on change de page
									topic.setLastReadPost(t.getLastReadPost());
									loadPosts(topic, urlParser.getPage());
								}
							}
							else
							{
								// Topic différent, on change de topic
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
								new MessageResponseAsyncTask(PostsActivity.this, getString(R.string.keywords_loading))
								{						
									@Override
									protected HFRMessageResponse executeInBackground()	throws HFR4droidException
									{
										return getMessageSender().setKeywords(getDataRetriever().getHashCheck(), code, keywordsView.getText().toString());
									}
									
									@Override
									protected void onActionFinished(String message)
									{
										Toast.makeText(PostsActivity.this, message, Toast.LENGTH_SHORT).show();
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
		//loading.setBackgroundColor(currentTheme.getListBackgroundColor());
		loading.setVisibility(View.VISIBLE);
		if (!refresh) loadLoadingWebView(loading);

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
		css.append("a.cLink { color:" + currentTheme.getPostLinkColorAsString() + "; text-decoration:none; }");
		css.append("a.cLink:hover, a.cLing:active  { color:" + currentTheme.getPostLinkColorAsString() + "; text-decoration:underline; }");
		css.append(".citation, .oldcitation, .quote, .oldquote, .fixed, .code, .spoiler, .oldspoiler { padding:3px; text-align:left; width:90%; }");
		css.append(".citation, .oldcitation, .quote, .oldquote, .spoiler, .oldspoiler { margin: 8px auto; }");
		css.append(".code, .fixed { background-color:" + currentTheme.getPostBlockBackgroundColorAsString() + "; border:1px solid " + currentTheme.getPostTextColorAsString() + "; color:" + currentTheme.getPostTextColorAsString() + "; font-family:'Courier New',Courier,monospace; margin:8px 5px; }");
		css.append(".oldcitation, .oldquote { border:0; }");
		css.append(".quote, .oldquote { font-style:italic; }");
		css.append("table { font-size: 1em; }");
		css.append(".spoiler, .oldspoiler, .citation, .quote { border:1px solid " + currentTheme.getListDividerColorAsString() + "; background-color:" + currentTheme.getPostBlockBackgroundColorAsString() + " }");
		css.append("div.masque { visibility:hidden; }");
		css.append(".container { text-align:center; width:100%; }");
		css.append(".s1, .s1Topic { font-size: " + getTextSize(10) + "px; }");
		css.append("p { margin:0; padding:0; }");
		css.append("p, ul { font-size: 0.8em; margin-bottom: 0; margin-top: 0; }");
		css.append("pre { font-size: 0.7em; white-space: pre-wrap }");
		css.append("ol.olcode { font-size: 0.7em; }");
		css.append("body { margin:0; padding:0; background-color:" + currentTheme.getListBackgroundColorAsString() + "; }");
		css.append(".HFR4droid_header { width:100%; background: url(\"" + currentTheme.getPostHeaderData() + "\"); height: 50px; text-align: right; }");
		css.append(".HFR4droid_header div { position: absolute; margin: 5px 0 0 5px; width:90%; text-align: left; }");
		css.append(".HFR4droid_header div img { float: left; max-width:60px; max-height:40px; margin-right:5px; }");
		css.append(".HFR4droid_header span.pseudo { color:" + currentTheme.getPostPseudoColorAsString() + "; font-size: " + getTextSize(16) + "px; font-weight:bold; }");
		css.append(".HFR4droid_header span.date { display: block; font-style:italic; color:" + currentTheme.getPostDateColorAsString() + "; font-size: " + getTextSize(12) + "px; margin: ");
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
		css.append(".HFR4droid_edit_quote { margin-bottom: 5px; padding: 4px; padding-bottom: 3px; background-color: " + currentTheme.getPostEditQuoteBackgroundColorAsString() + ";  font-style:italic; color:" + currentTheme.getPostEditQuoteTextColorAsString() + "; font-size: " + getTextSize(9) + "px; }");
		css.append(".HFR4droid_content { padding: 10px; padding-top: 5px; font-size: " + getTextSize(16) + "px; }");
		css.append(".HFR4droid_content p, .HFR4droid_content div, .HFR4droid_content ul, .HFR4droid_content b { color:" + currentTheme.getPostTextColorAsString() + "}");
		css.append(".modo_post { background-color: " + currentTheme.getModoPostBackgroundColorAsString() + "; }");
		css.append(".HFR4droid_footer { height: 10px; width:100%; background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAA%2FCAMAAAAWu1JmAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAgY0hSTQAAeiYAAICEAAD6AAAAgOgAAHUwAADqYAAAOpgAABdwnLpRPAAAAwBQTFRFhYWFs7SzysrKy8vLyszKzMzMzc3Nzs7Oz8%2FP0NDQ0dHR0dLR0tLS09PT1NTU1dXV1tbW19fX2NjY2dnZ2tna2tra29rb3Nvc3Nzc3dzdAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWKfi1AAAABh0RVh0U29mdHdhcmUAUGFpbnQuTkVUIHYzLjM2qefiJQAAAEFJREFUGFddwkcOgDAQBMHBhCWDCQb%2B%2F1Fai8TBqlKhSoOiDiVdejK3PgkndrchYsXiZkwY0bsO7c9kalCjdAF6ARIIA4Sqnjr8AAAAAElFTkSuQmCC\"); }");
		css.append("</style>");

		Display display = getWindowManager().getDefaultDisplay(); 
		int width = Math.round(display.getWidth() / webView.getScale());
		StringBuffer js2 = new StringBuffer("<script type=\"text/javascript\">");
		js2.append("loadDynamicCss(" + width + ");");
		js2.append("</script>");

		setDrawablesToggles();
		StringBuffer postsContent = new StringBuffer("");
		for (Post p : posts)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
			SimpleDateFormat todaySdf = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat check = new SimpleDateFormat("ddMMyyyy");
			boolean today = check.format(new Date()).equals(check.format(p.getDate()));
			String date = today ? todaySdf.format(p.getDate()) : "Le " + sdf.format(p.getDate());
			String avatar = p.getAvatarUrl() != null && isAvatarsEnable ? "<img alt=\"avatar\" title=\"" + p.getPseudo() + "\" src=\"" + p.getAvatarUrl() + "\" />" : "";
			String pseudoSpan = "<span class=\"pseudo\">" + p.getPseudo() + "</span>";
			String dateSpan = "<span class=\"date\">" + date + "</span>";
			StringBuilder editQuoteDiv = new StringBuilder("");
			if (p.getLastEdition() != null || p.getNbCitations() > 0)
			{
				editQuoteDiv.append("<div class=\"HFR4droid_edit_quote\">");
				if (p.getLastEdition() != null)
				{
					today = check.format(new Date()).equals(check.format(p.getLastEdition()));
					editQuoteDiv.append("Edité " + (today ? "à " + todaySdf.format(p.getLastEdition()) : "le " + sdf.format(p.getLastEdition())));
				}
				if (p.getNbCitations() > 0)
				{
					editQuoteDiv.append(p.getLastEdition() != null ? ", cité " + p.getNbCitations() + " fois" : "Cité " + p.getNbCitations() + " fois");
				}
				editQuoteDiv.append("</div>");
			}
			String header = "<div id=\"" + p.getId() + "\" class=\"HFR4droid_header\" onclick=\"openQuickActionWindow(" + p.getId() + ", " + p.isMine() + ")\"><div>" + avatar + pseudoSpan + "<br />" + dateSpan + "</div></div>";

			String content = "";
			content = "<div class=\"HFR4droid_post";
			if (p.isModo()) content += " modo_post";
			content += "\">" + editQuoteDiv + "<div class=\"HFR4droid_content\"";
			content += ">" + p.getContent() + "</div></div>";
			content = content.replaceAll("onload=\"md_verif_size\\(this,'Cliquez pour agrandir','[0-9]+','[0-9]+'\\)\"", "onclick=\"return true;\"");
			content = content.replaceAll("<b\\s*class=\"s1\"><a href=\"(.*?)\".*?>(.*?)</a></b>", "<b onclick=\"window.HFR4Droid.handleQuote('$1');\" class=\"s1\">$2</b>");
			if (!isSmileysEnable) content = content.replaceAll("<img\\s*src=\"http://forum\\-images\\.hardware\\.fr.*?\"\\s*alt=\"(.*?)\".*?/>", "$1");
			content = content.replaceAll("<img\\s*src=\"http://forum\\-images\\.hardware\\.fr/images/perso/(.*?)\"\\s*alt=\"(.*?)\"", "<img onclick=\"window.HFR4Droid.editKeywords('$2');\" src=\"http://forum-images.hardware.fr/images/perso/$1\" alt=\"$2\"");
			if (!isImgsEnable) content = content.replaceAll("<img\\s*src=\"http://[^\"]*?\"\\s*alt=\"http://[^\"]*?\"\\s*title=\"(http://.*?)\".*?/>", "<a href=\"$1\" target=\"_blank\" class=\"cLink\">$1</a>");
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

		// PAS DE CACHE §§§
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
	
	protected void addQuickActionWindowItems(HFR4droidQuickActionWindow window, final long currentPostId, boolean isMine)
	{
		final StringBuilder postLink = new StringBuilder(getDataRetriever().getBaseUrl() + "/forum2.php?config=hfr.inc");
		postLink.append("&cat=").append(topic.getCategory().getId());
		postLink.append("&post=").append(topic.getId());
		postLink.append("&page=").append(currentPageNumber);
		postLink.append("#t").append(currentPostId);
		
		if (topic.getStatus() != TopicStatus.LOCKED)
		{
			if (isMine)
			{
				QuickActionWindow.Item edit = new QuickActionWindow.Item(PostsActivity.this, "", android.R.drawable.ic_menu_edit, new PostCallBack(PostCallBackType.EDIT, currentPostId, true) 
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

				QuickActionWindow.Item delete = new QuickActionWindow.Item(PostsActivity.this, "", android.R.drawable.ic_menu_delete, new PostCallBack(PostCallBackType.DELETE, currentPostId, true, true)
				{									
					@Override
					protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
					{
						return getMessageSender().deleteMessage(p, getDataRetriever().getHashCheck()).isSuccess() ? "1" : "0";
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

			QuickActionWindow.Item quote = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_quote, new PostCallBack(PostCallBackType.QUOTE, currentPostId, true)
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

			boolean quoteExists = quotes.get(currentPostId) != null;
			QuickActionWindow.Item multiQuote = new QuickActionWindow.Item(PostsActivity.this, "",
					quoteExists ? R.drawable.ic_menu_multi_quote_moins : R.drawable.ic_menu_multi_quote_plus,
							new PostCallBack(quoteExists ? PostCallBackType.MULTIQUOTE_REMOVE : PostCallBackType.MULTIQUOTE_ADD, currentPostId, false)
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
		}
		
		QuickActionWindow.Item addFavorite = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_star, new PostCallBack(PostCallBackType.FAVORITE, currentPostId, true)
		{									
			@Override
			protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
			{
				return getMessageSender().addFavorite(p).getMessage();
			}

			@Override
			protected void onActionExecute(String data)
			{
				Toast.makeText(PostsActivity.this, data, Toast.LENGTH_SHORT).show();
			}
		});							
		if (isLoggedIn()) window.addItem(addFavorite);
		
		if (!isMine)
		{
			QuickActionWindow.Item sendMP = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_messages, new QuickActionWindow.Item.Callback()
			{	
				public void onClick(QuickActionWindow window, Item item, View anchor)
				{
					Post p = getPostById(currentPostId);
					Intent intent = new Intent(PostsActivity.this, NewTopicActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("cat", Category.MPS_CAT);
					bundle.putString("pseudo", p.getPseudo());
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});					
			window.addItem(sendMP);
		}
		
		QuickActionWindow.Item copyLink = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_link, new QuickActionWindow.Item.Callback()
		{	
			public void onClick(QuickActionWindow window, Item item, View anchor)
			{
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				clipboard.setText(postLink);
				Toast.makeText(PostsActivity.this, getText(R.string.copy_post_link), Toast.LENGTH_SHORT).show();
			}
		});					
		window.addItem(copyLink);
		
		QuickActionWindow.Item copyContent = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_copy, new QuickActionWindow.Item.Callback()
		{	
			public void onClick(QuickActionWindow window, Item item, View anchor)
			{
				Post p = getPostById(currentPostId);
				String bbCode;
				try
				{
					bbCode = getDataRetriever().getPostContent(p);
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
					clipboard.setText(cleanBBCode(bbCode));
					Toast.makeText(PostsActivity.this, getText(R.string.copy_post_content), Toast.LENGTH_SHORT).show();
				}
				catch (DataRetrieverException e)
				{
					error(e, true);
				}
			}
		});					
		window.addItem(copyContent);
		
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
	}
	
	private void loadLoadingWebView(WebView loading)
	{
		loading.loadData("<html><body style=\"text-align: center; margin-top: 150px; background-color:" + currentTheme.getListBackgroundColorAsString() + ";\"><img src=\"data:image/gif;base64,R0lGODlhKwALAPEAAP%2F%2F%2FwAAAIKCggAAACH%2FC05FVFNDQVBFMi4wAwEAAAAh%2FhpDcmVhdGVkIHdpdGggYWpheGxvYWQuaW5mbwAh%2BQQJCgAAACwAAAAAKwALAAACMoSOCMuW2diD88UKG95W88uF4DaGWFmhZid93pq%2BpwxnLUnXh8ou%2BsSz%2BT64oCAyTBUAACH5BAkKAAAALAAAAAArAAsAAAI9xI4IyyAPYWOxmoTHrHzzmGHe94xkmJifyqFKQ0pwLLgHa82xrekkDrIBZRQab1jyfY7KTtPimixiUsevAAAh%2BQQJCgAAACwAAAAAKwALAAACPYSOCMswD2FjqZpqW9xv4g8KE7d54XmMpNSgqLoOpgvC60xjNonnyc7p%2BVKamKw1zDCMR8rp8pksYlKorgAAIfkECQoAAAAsAAAAACsACwAAAkCEjgjLltnYmJS6Bxt%2Bsfq5ZUyoNJ9HHlEqdCfFrqn7DrE2m7Wdj%2F2y45FkQ13t5itKdshFExC8YCLOEBX6AhQAADsAAAAAAAAAAAA%3D\" alt=\"loading\" /></body></html>", "text/html", "UTF-8");
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
			applyTheme(currentTheme, (ViewGroup) postDialog.findViewById(R.id.PostContainer).getParent());
			addPostButtons(layout);
			((EditText) postDialog.findViewById(R.id.InputPostContent)).setTextSize(getTextSize(14));

			postDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
			{
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
				{
					if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
					{
						View firstChild = layout.getChildAt(0);
						if (postDialog.isShowing())
						{
							if (firstChild instanceof WebView)
							{
								hideWikiSmiliesResults(layout);
								return true;
							}
							else if (postDialog.findViewById(R.id.SmileySearch).getVisibility() == View.VISIBLE)
							{
								hideWikiSmiliesSearch(layout);
								return true;
							}
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
		EditText smileyTag = (EditText) postDialog.findViewById(R.id.InputSmileyTag);
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
	protected void onRehostOk(String url)
	{
		insertBBCode((EditText) postDialog.findViewById(R.id.InputPostContent), url, "");
		postDialog.show();
	}

	@Override
	protected void setOkButtonClickListener(Button okButton)
	{
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				final EditText postContent = (EditText) postDialog.findViewById(R.id.InputPostContent);
				new ValidateMessageAsynckTask(PostsActivity.this, postId)
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
									onPostingOk(code, postId);
									return true;									
		
								case POST_ADD_OK: // New post ok
									postContent.setText("");
									postDialog.dismiss();
									onPostingOk(code, postId);
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
	
	protected void onPostingOk(ResponseCode code, long postId)
	{
		switch (code)
		{	
			case POST_EDIT_OK: // Edit ok
				reloadPage();
				break;

			case POST_ADD_OK: // New post ok
				topic.setLastReadPost(BOTTOM_PAGE_ID);
				if (currentPageNumber == topic.getNbPages()) reloadPage();
				break;
		}
	}
	
	private void setDrawablesToggles()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		boolean isWifiEnable = info != null && (info.getType() == ConnectivityManager.TYPE_WIFI);
		
		setToggleFromPref("isAvatarsEnable", getAvatarsDisplayType(), isWifiEnable);
		setToggleFromPref("isSmileysEnable", getSmileysDisplayType(), isWifiEnable);
		setToggleFromPref("isImgsEnable", getImgsDisplayType(), isWifiEnable);
	}
	
	private void setToggleFromPref(String drawableToggle, DrawableDisplayType type, boolean isWifiEnable)
	{
		try
		{
			boolean drawMe;
			switch (type)
			{
				case NEVER_SHOW:
					drawMe = false;
					break;
					
				case SHOW_WHEN_WIFI:
					drawMe = isWifiEnable;
					break;
					
				default:
					drawMe = true;
					break;
			}
			PostsActivity.class.getDeclaredField(drawableToggle).setBoolean(this, drawMe);
		}
		catch (Exception e)
		{
			error(e);
		}
	}
	
	@Override
	protected void applyTheme(Theme theme)
	{
		LinearLayout postLayout = (LinearLayout) findViewById(R.id.PostsLayout);
		FrameLayout root = (FrameLayout) postLayout.getParent();
		root.setBackgroundColor(theme.getListBackgroundColor());
		
		WebView loading = (WebView) findViewById(R.id.loading);
		if (loading != null)
		{
			loading.destroy();
			postLayout.removeView(loading);
		}
		loading = new WebView(this);
		loading.setId(R.id.loading);
		loading.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		loading.setBackgroundColor(currentTheme.getListBackgroundColor());
		loading.setVisibility(View.VISIBLE);
		postLayout.addView(loading, 2);
		
		if (postDialog != null)
		{
			applyTheme(theme, (ViewGroup) postDialog.findViewById(R.id.PostContainer).getParent());
		}
	}
	
	protected void toggleSearchPosts()
	{
		final LinearLayout searchPanel = (LinearLayout) findViewById(R.id.SearchPostsPanel);
		if (searchPanel.getVisibility() == View.VISIBLE)
		{
			Animation anim = AnimationUtils.loadAnimation(PostsActivity.this, R.anim.search_panel_exit);
			anim.setAnimationListener(new AnimationListener()
			{
				public void onAnimationStart(Animation animation) {}

				public void onAnimationRepeat(Animation animation) {}

				public void onAnimationEnd(Animation animation)
				{
					searchPanel.setVisibility(View.GONE);
				}
			});
			searchPanel.startAnimation(anim);					
		}
		else
		{
			Animation anim = AnimationUtils.loadAnimation(PostsActivity.this, R.anim.search_panel_enter);
			searchPanel.setVisibility(View.VISIBLE);
			searchPanel.startAnimation(anim);
		}
	}
	
	protected void searchPosts(Topic topic, String pseudo, String word, Post fromPost)
	{
		searchPosts(topic, pseudo, word, fromPost, true);
	}

	protected void searchPosts(final Topic topic, final String pseudo, final String word, final Post fromPost, final boolean sameActivity)
	{
		String progressTitle = topic.toString();
		String paginationSuffix = sameActivity && ((PostsSearchActivity) this).getCurrentFromPost().getId() < fromPost.getId() ? "_up" : "_down";
		String pagination = sameActivity ? "_with_pagination" + paginationSuffix : "";
		
		String progressContent = pseudo != null && word != null  && pseudo.length() > 0  && word.length() > 0 ?
		getString("searching_posts_pseudo_word" + pagination, pseudo, word) : (pseudo != null && pseudo.length() > 0 ?
		getString("searching_posts_pseudo" + pagination, pseudo) :
		getString("searching_posts_word" + pagination, word));
		String noElement = getString(R.string.no_post);
		
		new DataRetrieverAsyncTask<Post, Topic>(this)
		{			
			@Override
			protected List<Post> retrieveDataInBackground(Topic... topics) throws DataRetrieverException
			{
				return getDataRetriever().searchPosts(topics[0], pseudo, word, fromPost);
			}

			@Override
			protected void onPostExecuteSameActivity(List<Post> posts) throws ClassCastException
			{
				PostsSearchActivity activity = (PostsSearchActivity) PostsActivity.this;
				activity.setPosts(posts);
				
				Post lastPost = posts.get(posts.size() - 1);
				if (lastPost.getId() > activity.getLastFromPost().getId())
				{
					activity.addFromPost(lastPost);
				}
				activity.setPageNumberFromPost(fromPost);
				setTitle();
				activity.refreshPosts(posts);
			}

			@Override
			protected void onPostExecuteOtherActivity(List<Post> posts)
			{
				Intent intent = new Intent(PostsActivity.this, PostsSearchActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
				Bundle bundle = new Bundle();
				bundle.putSerializable("posts", new ArrayList<Post>(posts));
				bundle.putSerializable("fromPost", fromPost);
				bundle.putString("pseudo", pseudo);
				bundle.putString("word", word);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}.execute(progressTitle, progressContent, noElement, sameActivity, topic);
	}

	protected Post getPostById(long postId)
	{
		for (Post p : posts)
		{
			if (p.getId() == postId) return p;
		}
		return null;
	}
	
	private String cleanBBCode(String bbCode)
	{
		return bbCode.replaceAll("\\[\\/?(?:b|i|u|strike|quote|fixed|code|url|img|\\*|spoiler)*?.*?\\]", "");
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
				getConfirmDialog(
				getString(type.getKey() + "_title"),
				getString(type.getKey() + "_message"),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1)
					{
						execute();
					}
				}).show();
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
				progressDialog = new ProgressDialog(PostsActivity.this);
				progressDialog.setMessage(getString(type.getKey() + "_loading"));
				progressDialog.setIndeterminate(true);
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