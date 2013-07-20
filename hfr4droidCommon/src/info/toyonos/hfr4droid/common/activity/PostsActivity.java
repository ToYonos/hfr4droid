package info.toyonos.hfr4droid.common.activity;

import info.toyonos.hfr4droid.common.HFR4droidApplication;
import info.toyonos.hfr4droid.common.HFR4droidException;
import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.core.bean.AlertQualitay;
import info.toyonos.hfr4droid.common.core.bean.BasicElement;
import info.toyonos.hfr4droid.common.core.bean.Category;
import info.toyonos.hfr4droid.common.core.bean.Post;
import info.toyonos.hfr4droid.common.core.bean.Profile;
import info.toyonos.hfr4droid.common.core.bean.Theme;
import info.toyonos.hfr4droid.common.core.bean.Topic;
import info.toyonos.hfr4droid.common.core.bean.Topic.TopicStatus;
import info.toyonos.hfr4droid.common.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.common.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.common.core.data.HFRUrlParser;
import info.toyonos.hfr4droid.common.core.data.MDUrlParser;
import info.toyonos.hfr4droid.common.core.message.HFRMessageResponse;
import info.toyonos.hfr4droid.common.core.message.HFRMessageSender.ResponseCode;
import info.toyonos.hfr4droid.common.core.message.MessageSenderException;
import info.toyonos.hfr4droid.common.core.utils.HttpClient;
import info.toyonos.hfr4droid.common.core.utils.PatchInputStream;
import info.toyonos.hfr4droid.common.core.utils.TransformStreamException;
import info.toyonos.hfr4droid.common.service.MpNotifyService;
import info.toyonos.hfr4droid.common.util.asynctask.DataRetrieverAsyncTask;
import info.toyonos.hfr4droid.common.util.asynctask.MessageResponseAsyncTask;
import info.toyonos.hfr4droid.common.util.asynctask.PreLoadingAsyncTask;
import info.toyonos.hfr4droid.common.util.asynctask.ProgressDialogAsyncTask;
import info.toyonos.hfr4droid.common.util.asynctask.ValidateMessageAsynckTask;
import info.toyonos.hfr4droid.common.util.dialog.PageNumberDialog;
import info.toyonos.hfr4droid.common.util.helper.NewPostUIHelper;
import info.toyonos.hfr4droid.common.util.listener.OnScreenChangeListener;
import info.toyonos.hfr4droid.common.util.view.DragableSpace;
import info.toyonos.hfr4droid.common.util.view.NonLeakingWebView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.Selection;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.naholyr.android.ui.HFR4droidQuickActionWindow;
import com.naholyr.android.ui.QuickActionWindow;
import com.naholyr.android.ui.QuickActionWindow.Item;

/**
 * <p>Activity listant les posts</p>
 * 
 * @author ToYonos
 *
 */

@SuppressWarnings("deprecation")
public class PostsActivity extends HFR4droidMultiListActivity<List<Post>>
{
	private static final String POST_LOADING 	= ">¤>¤>¤>¤>¤...post_loading...<¤<¤<¤<¤<¤";
	private static final String DOWNLOAD_DIR 	= "/HFR4droid/";
	
	private static Boolean oldCitation = null;

	public static enum PostCallBackType
	{
		ADD("add"),
		EDIT("edit"),
		QUOTE("quote"),
		DELETE("delete"),
		MULTIQUOTE_ADD("multiquote_add"),
		MULTIQUOTE_REMOVE("multiquote_remove"),
		FAVORITE("favorite"),
		COPY_CONTENT("copy_content");

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
	private TopicType fromType;
	private boolean fromAllCats;

	private GestureDetector gestureDetector;
	private int currentScrollY;

	protected Dialog postDialog;
	private NewPostUIHelper uiHelper;
	private long postId;

	protected Map<Long, String> quotes;

	protected DrawableDisplayType currentAvatarsDisplayType = null;
	protected DrawableDisplayType currentSmileysDisplayType = null;
	protected DrawableDisplayType currentImgsDisplayType = null;
	
	private Boolean isAvatarsEnable = true;
	private Boolean isSmileysEnable = true;
	private Boolean isImgsEnable = true;
	
	private HFR4droidQuickActionWindow currentQAwindow = null;
	
	protected PreLoadingPostsAsyncTask preLoadingPostsAsyncTask = null;
	private int pageToBeSetToRead = -1;
	private AsyncTask<String, Void, Profile> profileTask = null;
	private Timer timerOnPause = null;
	
	private List<AlertQualitay> alertsQualitay = null;
	
	private HttpClient<Bitmap> imgBitmapHttpClient = null;
	
	private HttpClient<ByteArrayInputStream> imgHttpClient = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posts);
		space = (DragableSpace) findViewById(R.id.Space);
		space.setSwipeSensibility(getHFR4droidApplication().getSwipe());
		applyTheme(currentTheme);
		
		attachEvents();

		currentScrollY = -1;
		postDialog = null;
		quotes = Collections.synchronizedMap(new HashMap<Long, String>());
		uiHelper = getNewPostUIHelper();

		currentAvatarsDisplayType = getAvatarsDisplayType();
		currentSmileysDisplayType = getSmileysDisplayType();
		currentImgsDisplayType = getImgsDisplayType();
		
		Bundle bundle = this.getIntent().getExtras();
		fromType =  bundle != null && bundle.getSerializable("fromTopicType") != null ? (TopicType) bundle.getSerializable("fromTopicType") : TopicType.ALL;
		fromAllCats = bundle != null ? bundle.getBoolean("fromAllCats", false) : false;
		onCreateInit(bundle);

		if (topic != null)
		{
			setTitle();
			if (topic.getCategory().equals(Category.MPS_CAT))
			{
				clearNotifications();
				synchronized (MpNotifyService.class)
				{
					if (MpNotifyService.currentNewMps > 0 && topic.getStatus() == TopicStatus.NEW_MP) MpNotifyService.currentNewMps--;	
				}
			}
		}

		gestureDetector = new GestureDetector(new SimpleOnGestureListener()
		{
			@Override
			public boolean onDoubleTap(MotionEvent e)
			{
				if (!isDblTapEnable()) return false;
				reloadPage();
				return true;
			}
		});
		
		imgBitmapHttpClient = new HttpClient<Bitmap>(getHFR4droidApplication().getHttpClientHelper())
		{
			@Override
			protected Bitmap transformStream(InputStream is) throws TransformStreamException
			{
				return BitmapFactory.decodeStream(new PatchInputStream(is));
			}
		};

		imgHttpClient = new HttpClient<ByteArrayInputStream>(getHFR4droidApplication().getHttpClientHelper())
		{		
			@Override
			protected ByteArrayInputStream transformStream(InputStream is) throws TransformStreamException
			{
				try
				{
				    ByteArrayOutputStream baos = new ByteArrayOutputStream();
				    byte[] buffer = new byte[1024];
				    int len;
				    while ((len = is.read(buffer)) > 0 )
				    {
				        baos.write(buffer, 0, len);
				    }
				    baos.flush();
				    return new ByteArrayInputStream(baos.toByteArray());
				}
				catch (IOException e)
				{
					throw new TransformStreamException(e);
				}
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	protected void onCreateInit(Bundle bundle)
	{
		if (bundle != null && bundle.getSerializable("posts") != null)
		{
			List<Post> posts = setDatasource((List<Post>) bundle.getSerializable("posts"));
			if (posts != null && posts.size() > 0)
			{
				topic = posts.get(0).getTopic();
				displayPosts(posts);
			}
			preloadPosts();
		}
		else
		{
			if (bundle != null && bundle.getSerializable("topic") != null)
			{
				topic = (Topic) bundle.getSerializable("topic");
			}
			if (topic != null) loadPosts(topic, currentPageNumber);
		}
		
		// Listener pour le changement de view dans le composant DragableSpace
		space.setOnScreenChangeListener(new OnScreenChangeListener()
		{
			public void onScreenChange(final int oldIndex, final int newIndex)
			{
				if (oldIndex == newIndex) return;
				
				if (preLoadingPostsAsyncTask != null)
				{
					if (preLoadingPostsAsyncTask.getStatus() != Status.FINISHED)
					{
						Log.d(HFR4droidApplication.TAG, "Cancelling preLoadingPostsAsyncTask...");
						preLoadingPostsAsyncTask.cancel(true);
					}
					preLoadingPostsAsyncTask = null;
				}
				findViewById(R.id.PostsProgressBar).setVisibility(View.GONE);
				
				boolean forward = oldIndex < newIndex;
				int targetPageNumber = -1;
				if (forward)
				{
					currentPageNumber++;
					if (currentPageNumber != topic.getNbPages() && (newIndex != 1 || getView(2) == null || getDatasource(2) == null))
					{
						targetPageNumber = currentPageNumber + 1;
					}
				}
				else
				{
					currentPageNumber--;
					if (currentPageNumber != 1 && (newIndex != 1 || getView(0) == null || getDatasource(0) == null))
					{
						targetPageNumber = currentPageNumber - 1;
					}
				}
				
				// On marque comme lu la page courante si elle a été marqué ainsi
				if (currentPageNumber == pageToBeSetToRead)
				{
					pageToBeSetToRead = -1;
					new AsyncTask<Void, Void, Boolean>()
					{
						@Override
						protected Boolean doInBackground(Void... params)
						{
							try
							{
								return getDataRetriever().setPostsAsRead(topic, currentPageNumber);
							}
							catch (DataRetrieverException e)
							{
								error(e, true, true);
								return false;
							}
						}

						@Override
						protected void onPostExecute(Boolean ok)
						{
							topic.setLastReadPage(pageToBeSetToRead);
							if (ok) Log.i(HFR4droidApplication.TAG, getString(R.string.set_posts_as_read_ok, currentPageNumber));
							else Log.w(HFR4droidApplication.TAG, getString(R.string.set_posts_as_read_ko, currentPageNumber));
						}
					}.execute();
				}
				
				if (targetPageNumber != -1)
				{
					preLoadingPostsAsyncTask = new PreLoadingPostsAsyncTask(PostsActivity.this);
					preLoadingPostsAsyncTask.execute(targetPageNumber, topic);
				}

				// Pour éviter une surchage du cpu, on n'utilise qu'une seule webview, que l'on supprime et recrée dynamiquement
				if (getHFR4droidApplication().isLightMode())
				{
					new Timer().schedule(new TimerTask()
					{
						public void run()
						{
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									removeView(newIndex);
									restoreView(newIndex);
									
									removeView(oldIndex);
									restoreView(oldIndex);
								}
							});
						}
					}, 500);
				}

				supportInvalidateOptionsMenu();
				setTitle();
			}
			
			public void onFailForward()
			{
				if (currentPageNumber == topic.getNbPages())
				{
					reloadPage();
				}
				else
				{
					inverseTask(currentPageNumber + 1);
					displayPreloadingToast(preLoadingPostsAsyncTask);
				}
			}

			public void onFailRearward()
			{
				if (currentPageNumber == 1)
				{
					reloadPage();
				}
				else
				{
					inverseTask(currentPageNumber - 1);
					displayPreloadingToast(preLoadingPostsAsyncTask);
				}
			}

			/**
			 * Permet de changer l'ordre de chargement des pages
			 * @param targetPageNumber
			 */
			private void inverseTask(int targetPageNumber)
			{
				// Si une tâche est en cours
				if (preLoadingPostsAsyncTask != null && preLoadingPostsAsyncTask.getStatus() == Status.RUNNING)
				{
					// si la page précédente n'est en cours de chargement
					if (preLoadingPostsAsyncTask.getPageNumber() != targetPageNumber)
					{
						// Sinon on force le chargement de la page précédente
						int interruptedPageNumber = preLoadingPostsAsyncTask.getPageNumber();
						preLoadingPostsAsyncTask.cancel(true);
						preLoadingPostsAsyncTask = new PreLoadingPostsAsyncTask(PostsActivity.this, interruptedPageNumber);
						preLoadingPostsAsyncTask.execute(targetPageNumber, topic);
					}
				}
			}
		});
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		timerOnPause.cancel();
		uiHelper.destroyWikiSmiliesResults(uiHelper.getSmiliesLayout());
		if (preLoadingPostsAsyncTask != null)
		{
			preLoadingPostsAsyncTask.cancel(true);
			preLoadingPostsAsyncTask = null;
		}
		System.gc();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (postDialog != null) postDialog.dismiss();

		timerOnPause = new Timer();
		timerOnPause.schedule(new TimerTask()
		{
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						// On detruit les webviews pour éviter qu'elle consomme du CPU quand l'appli est en background
						Log.d(HFR4droidApplication.TAG, "Destroying webviews...");
						if (preLoadingPostsAsyncTask != null)
						{
							preLoadingPostsAsyncTask.cancel(true);
							preLoadingPostsAsyncTask = null;
						}
						WebView webView = getWebView();
						if (webView != null) currentScrollY = webView.getScrollY();
						removeViews();
					}
				});
			}
		}, 1000);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (timerOnPause != null) timerOnPause.cancel();
		if (isViewsHasToBeRestore())
		{
			Log.d(HFR4droidApplication.TAG, "Restoring webviews...");
			restoreViews();
			preloadPosts(true);
		}
		if (currentQAwindow != null) currentQAwindow.dismiss();
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
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
		return (WebView) getView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.posts, menu);
		getSupportMenuInflater().inflate(R.menu.nav, menu);
		getSupportMenuInflater().inflate(R.menu.misc, menu);
		getSupportMenuInflater().inflate(R.menu.common, menu);
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
		}

		MenuItem addPost = menu.findItem(R.id.MenuAddPost);
		addPost.setVisible(isLoggedIn() && topic.getStatus() != TopicStatus.LOCKED);
		addPost.setEnabled(isLoggedIn() && topic.getStatus() != TopicStatus.LOCKED);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean result = super.onOptionsItemSelected(item);
		if (!result)
		{
			if (item.getItemId() == R.id.MenuAddPost)
			{
				String postContent = null;
				if (!quotes.isEmpty())
				{
					boolean lockAddPost = false;
					for(String q : quotes.values()) if (lockAddPost = POST_LOADING.equals(q)) break;
					if (lockAddPost)
					{
						Toast.makeText(this, R.string.multiquote_loading, Toast.LENGTH_SHORT).show();
						return true;
					}

					StringBuffer data = new StringBuffer("");
					for (Long postId : new TreeSet<Long>(quotes.keySet())) data.append(quotes.get(postId) + "\n");
					postContent = data.substring(0, data.length() - 1);
					quotes.clear();
				}
				showAddPostDialog(PostCallBackType.ADD, postContent);
				return true;
			}
			else if (item.getItemId() == R.id.MenuSearchPost)
			{
				toggleSearchPosts();
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

	public void setPosts(List<Post> posts)
	{
		setDatasource(posts);
	}
	
	@Override
	protected void setTitle()
	{
		final TextView topicTitle = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.TopicTitle);
		String topicName = topic.toString();
		if (topicName == null) topicName = "";
		int index =  topicName.indexOf(']');
		if (topicName.indexOf('[') == 0 && index != -1)
		{
			topicName = topicName.substring(index + 1).trim() + " " + topicName.substring(0, index + 1);
		}
		topicTitle.setText(topicName);
		topicTitle.setSelected(true);
		final TextView topicPageNumber = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.TopicPageNumber);
		topicPageNumber.setText((topic.getNbPages() != -1 ? "P." + currentPageNumber + "/" + topic.getNbPages() + " " : ""));
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
			loadPosts(topic, 1, false);
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
		new PageNumberDialog(this, currentPageNumber, topic.getNbPages())
		{
			protected void onValidate(int pageNumber)
			{
				if (Math.abs(pageNumber - currentPage) == 1)
				{
					space.snapToScreen(getCurrentIndex() + (pageNumber - currentPage));
				}
				else
				{
					loadPosts(topic, pageNumber, false);
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
	protected void loadLastPage()
	{
		if (currentPageNumber == (topic.getNbPages() - 1))
		{
			space.snapToScreen(getCurrentIndex() + 1);
		}
		else
		{
			loadPosts(topic, topic.getNbPages(), false);
		}
	}

	@Override
	protected void reloadPage()
	{
		WebView webView = getWebView();
		if (webView != null) currentScrollY = webView.getScrollY();
		loadPosts(topic, currentPageNumber);
	}
	
	@Override
	protected void goBack()
	{
		Category cat = fromAllCats ? Category.ALL_CATS : topic.getCategory();
		loadTopics(cat, fromType, 1, false);
	}

	protected void attachEvents()
	{		
		Button buttonSearchPosts = (Button) findViewById(R.id.ButtonSearchPosts);
		buttonSearchPosts.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				EditText pseudo = (EditText) findViewById(R.id.SearchPostsPseudo);
				EditText word = (EditText) findViewById(R.id.SearchPostsWord);
				Post fromPost = PostsActivity.this instanceof PostsSearchActivity ? null : getDatasource().get(0);
				searchPosts(topic, pseudo.getText().toString().trim(), word.getText().toString().trim(), fromPost, false);
			}
		});
	}
	
	public void preloadPosts()
	{
		preloadPosts(false);
	}
	
	public void preloadPosts(boolean verify)
	{
		boolean forceLoadingPreviousPage = false;
		if (verify)
		{
			if (isNextPageLoaded())
			{
				Log.d(HFR4droidApplication.TAG, "Next page in Dragable space is loaded");
				if (currentPageNumber != 1) forceLoadingPreviousPage = true;
				if (isPreviousPageLoaded() || currentPageNumber == 1)
				{
					Log.d(HFR4droidApplication.TAG, "Previous page in Dragable space is loaded");
					return;
				}
			}
			else if (currentPageNumber == topic.getNbPages() && isPreviousPageLoaded())
			{
				Log.d(HFR4droidApplication.TAG, "Previous page in Dragable space is loaded");
				return;
			}
		}
		// Préchargement de la page suivante dans le composant DragableSpace 
		if (topic.getNbPages() > 1)
		{			
			int targetPageNumber = currentPageNumber + 1;
			Integer previousPageNumber = currentPageNumber != 1 ? currentPageNumber - 1 : null;
			if (forceLoadingPreviousPage || currentPageNumber == topic.getNbPages())
			{
				targetPageNumber = currentPageNumber - 1;
				previousPageNumber = null;
			}
			
			preLoadingPostsAsyncTask = previousPageNumber != null ?
					new PreLoadingPostsAsyncTask(this, previousPageNumber) :
					new PreLoadingPostsAsyncTask(this);
			preLoadingPostsAsyncTask.execute(targetPageNumber, topic);
		}
	}

	protected WebView displayPosts(List<Post> posts)
	{
		return displayPosts(posts, false);
	}

	protected WebView displayPosts(List<Post> posts, final boolean preloading)
	{
		final WebView webView = new NonLeakingWebView(this)
        {
			//private boolean actionBarVisible = true;
			
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
                	// NullPointerException ignorée car le double tap est malgré tout bien supporté
                	// E/webview(16784): Got null mVelocityTracker when mPreventDefault = 0 mDeferTouchProcess = false mTouchMode = 1
                }
                return result;
            }
            
            /* Feature sympa visuellement mais useless, on permet l'accès au menu quand on est en bas de page donc chiant à l'usage
             
            @Override
            protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt)
            {
            	super.onScrollChanged(l, t, oldl, oldt);
            	Log.v(HFR4droidApplication.TAG, "l " + l + ", t " + t + ", oldl " + oldl + ", oldt " + oldt);
            	if (t == 0 && !actionBarVisible)
            	{
            		PostsActivity.this.getSupportActionBar().show();
            		actionBarVisible = true;
            	}
            	else if (t > 0 && actionBarVisible)
            	{
            		PostsActivity.this.getSupportActionBar().hide();
            		actionBarVisible = false;
            	}
            }*/
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

                	android.view.MenuItem.OnMenuItemClickListener handler = new android.view.MenuItem.OnMenuItemClickListener()
                    {
                		private void saveImage(final String url, final boolean compressToPng, final ImageCallback callback)
                		{                    		
    						new ProgressDialogAsyncTask<String, Void, File>(PostsActivity.this)
    						{
    							@Override
    							protected void onPreExecute() 
    							{
    								super.onPreExecute();
    	    						progressDialog.setMessage(getString(R.string.save_image));
									progressDialog.show();
    							}

    							@Override
    							protected File doInBackground(String... url)
    							{
    								setThreadId();
    								try
									{
	                        			File dir = new File(Environment.getExternalStorageDirectory() + DOWNLOAD_DIR);
	                        			if (!dir.exists()) dir.mkdirs();
	                        			String originalFileName = url[0].substring(url[0].lastIndexOf('/') + 1, url[0].length());
	                        			File imgFile = null;
	                        			
    									if (compressToPng)
										{
											Bitmap imgBitmap = imgBitmapHttpClient.doGet(url[0]);
											if (imgBitmap == null) return null;
		                        			String newFileName = originalFileName + ".png";
		                        			if (originalFileName.lastIndexOf('.') != -1)
		                        			{
		                        				newFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + ".png";
		                        			}
		                        			imgFile = new File(Environment.getExternalStorageDirectory() + DOWNLOAD_DIR, newFileName);
		                        	        OutputStream fos = new FileOutputStream(imgFile);
		                        	        imgBitmap.compress(CompressFormat.PNG, 90, fos);
		                        	        fos.close();
										}
										else
										{											
											ByteArrayInputStream input = imgHttpClient.doGet(url[0]);
											if (input == null) return null;
											ByteArrayBuffer baf = new ByteArrayBuffer(50);
											int current = 0;
											while ((current = input.read()) != -1)
											{
												baf.append((byte) current);
											}
		                        			if (originalFileName.lastIndexOf('.') == -1) originalFileName += ".jpg";
											imgFile = new File(Environment.getExternalStorageDirectory() + DOWNLOAD_DIR, originalFileName);
											FileOutputStream fos = new FileOutputStream(imgFile);
											fos.write(baf.toByteArray());
											fos.close();  
											input.close();
										}

	                        	        return imgFile;
									}
									catch (Exception e)
									{
										error(getString(R.string.save_image_failed), e, true, true);
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
                		
                		private String[] getExif(String path)
                		{
                			if (Build.VERSION.SDK_INT < 5) return null;
                			
                			String[] result = null;
                			try
                			{
	                			ExifInterface exif = new ExifInterface(path);
	                			List<String> data = new ArrayList<String>();
	                			
	                			// Date du cliché
	                			String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
	                			if (date != null) data.add(getString(R.string.exif_date, date));

	                			// Résolution du cliché
	                			String width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
	                			String lenght = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
	                			if (width != null && lenght != null)
	                			{
	                				int mp = (Integer.parseInt(width) * Integer.parseInt(lenght) / 1000000);
	                				data.add(getString(R.string.exif_reso,
	                				width + " x " + lenght + " (" + (mp == 0 ? "< 1 Mp" : mp + " Mp") + ")"));
	                			}

	                			// Orientation
	                			String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
	                			if (orientation != null)
	                			{
	                				String orientationLbl = getString(R.string.exif_orientation_undefined);
	                				switch (Integer.valueOf(orientation))
									{
										case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
											orientationLbl = getString(R.string.exif_orientation_flip_horizontal);
											break;

										case ExifInterface.ORIENTATION_FLIP_VERTICAL:
											orientationLbl = getString(R.string.exif_orientation_flip_vertical);
											break;

										case ExifInterface.ORIENTATION_NORMAL:
											orientationLbl = getString(R.string.exif_orientation_normal);
											break;

										case ExifInterface.ORIENTATION_ROTATE_180:
											orientationLbl = getString(R.string.exif_orientation_rotate_180);
											break;
											
										case ExifInterface.ORIENTATION_ROTATE_270:
											orientationLbl = getString(R.string.exif_orientation_rotate_270);
											break;

										case ExifInterface.ORIENTATION_ROTATE_90:
											orientationLbl = getString(R.string.exif_orientation_rotate_90);
											break;

										case ExifInterface.ORIENTATION_TRANSPOSE:
											orientationLbl = getString(R.string.exif_orientation_transpose);
											break;
											
										case ExifInterface.ORIENTATION_TRANSVERSE:
											orientationLbl = getString(R.string.exif_orientation_transverse);
											break;

										default:
											orientationLbl = getString(R.string.exif_orientation_undefined);
											break;
									}
	                				data.add(getString(R.string.exif_orientation, orientationLbl));
	                			}

	                			// Pris avec Flash ?
	                			String flash = exif.getAttribute(ExifInterface.TAG_FLASH);
	                			if (flash != null)
	                			{
	                				data.add(getString(R.string.exif_flash, flash.equals("1") ? getString(R.string.button_yes) : getString(R.string.button_no)));
	                			}

	                			// Model de l'appareil 
	                			String make = exif.getAttribute(ExifInterface.TAG_MAKE);
	                			String model = exif.getAttribute(ExifInterface.TAG_MODEL);
	                			if (make != null && (model == null || !model.toLowerCase().contains(make.toLowerCase()))) data.add(getString(R.string.exif_make, make));
	                			if (model != null) data.add(getString(R.string.exif_model, model));
	                			
	                			if (Build.VERSION.SDK_INT >= 8)
	                			{
		                			// Focale
		                			String focal = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
		                			if (focal != null) data.add(getString(R.string.exif_focal, focal));
		                			
		                			if (Build.VERSION.SDK_INT >= 11)
		                			{
			                			// Ouverture
			                			String aperture = exif.getAttribute(ExifInterface.TAG_APERTURE);
			                			if (aperture != null) data.add(getString(R.string.exif_aperture, aperture));

			                			// Temps d'exposition
			                			String exposure = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
			                			try
			                			{
			                				float exposureAsF = Float.parseFloat(exposure);
			                				if (exposureAsF < 0.33)
			                				{
			                					exposure = "1/" + String.valueOf(((int) (1 / exposureAsF)));
			                				}
			                			}
			                			catch (Exception e) {}
			                			 
			                			if (exposure != null) data.add(getString(R.string.exif_exposure, exposure));
			                			
			                			// ISO
			                			String iso = exif.getAttribute(ExifInterface.TAG_ISO);
			                			if (iso != null) data.add(getString(R.string.exif_iso, iso));
		                			}
	                			}
	                			
	                			result = new String[data.size()];
	                			data.toArray(result);
                			}
                			catch (IOException e)
                			{
								error(e, true);
							}
                			return result;
                		}
                		
                        public boolean onMenuItemClick(android.view.MenuItem item)
                        {
                			if (item.getItemId() == R.id.SaveImage)
							{
								saveImage(url, false, new ImageCallback()
								{
									public void run()
									{
										if (image != null)
										{
											Toast.makeText(PostsActivity.this, getString(R.string.save_image_ok, image.getParent()), Toast.LENGTH_LONG).show();
										}
									}
								});
							}
							else if (item.getItemId() == R.id.SaveImagePng)
							{
								saveImage(url, true, new ImageCallback()
								{
									public void run()
									{
										if (image != null)
										{
											Toast.makeText(PostsActivity.this, getString(R.string.save_image_ok, image.getParent()), Toast.LENGTH_LONG).show();
										}
									}
								});
							}
							else if (item.getItemId() == R.id.ShareImage)
							{
								saveImage(url, false, new ImageCallback()
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
							}
							else if (item.getItemId() == R.id.OpenImage)
							{
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
								startActivity(intent);
							}
							else if (item.getItemId() == R.id.GetExif)
							{
								if (Build.VERSION.SDK_INT < 5)
								{
									Toast.makeText(PostsActivity.this, getString(R.string.exif_version_too_low), Toast.LENGTH_LONG).show();
								}
								else
								{
									saveImage(url, false, new ImageCallback()
									{
										public void run()
										{
											if (image != null)
											{
												String[] exifData = getExif(image.getAbsolutePath());
												if (exifData != null)
												{
													if (exifData.length > 0)
													{
														final StringBuilder message = new StringBuilder("");
														for(String data : exifData) message.append(data + "\n");
														new AlertDialog.Builder(PostsActivity.this)
														.setTitle(R.string.exif_data)
														.setMessage(message)
														.setPositiveButton(R.string.button_copy_to_clip, new DialogInterface.OnClickListener()
														{
															public void onClick(DialogInterface dialog,	int which)
															{
																ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
																clipboard.setText(message);
																Toast.makeText(PostsActivity.this, getString(R.string.copy_exif_data), Toast.LENGTH_LONG).show();
															}
														})
														.setNegativeButton(R.string.button_close, new DialogInterface.OnClickListener()
														{
															public void onClick(DialogInterface dialog, int which) {}
														})
														.create()
														.show();
													}
													else
													{
														Toast.makeText(PostsActivity.this, getString(R.string.exif_no_data), Toast.LENGTH_LONG).show();
													}
												}
											}
										}
									});
								}
							}
                			return true;
                        }
                    };

                	menu.setHeaderTitle(url);
                    menu.add(0, R.id.SaveImage, 0, R.string.save_image_item).setOnMenuItemClickListener(handler);
                    menu.add(0, R.id.SaveImagePng, 0, R.string.save_image_png_item).setOnMenuItemClickListener(handler);
                    menu.add(0, R.id.ShareImage, 0, R.string.share_image_item).setOnMenuItemClickListener(handler);
                    menu.add(0, R.id.OpenImage, 0, R.string.open_image_item).setOnMenuItemClickListener(handler);
                    menu.add(0, R.id.GetExif, 0, R.string.get_exif_item).setOnMenuItemClickListener(handler);
                }
			}
		});
        

		webView.setFocusable(true);
		webView.setFocusableInTouchMode(false); 
		webView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		webView.addJavascriptInterface(new Object()
		{
			@SuppressWarnings("unused")
			public void log(String s)
			{
				Log.d(HFR4droidApplication.TAG, "Javascript : " + s);
			}
			
			@SuppressWarnings("unused")
			public void openQuickActionWindow(final long postId, final boolean isMine, final int yOffset)
			{
				if (yOffset >= 0 && (currentQAwindow == null || !currentQAwindow.isShowing()))
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
							if (currentQAwindow != null) currentQAwindow.dismiss();
							currentQAwindow = HFR4droidQuickActionWindow.getWindow(PostsActivity.this, configuration);
							
							addQuickActionWindowItems(currentQAwindow, postId, isMine);
							View spp = findViewById(R.id.SearchPostsPanel);
							View anchor = spp.getVisibility() == View.VISIBLE ? spp : findViewById(R.id.Anchor);
							
							getSupportActionBar().getCustomView().measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
							int actionBarHeight = getSupportActionBar().getCustomView().getMeasuredHeight();
							
							currentQAwindow.show(anchor, Math.round(yOffset * webView.getScale()), actionBarHeight);
						}
					});
				}
			}
			
			@SuppressWarnings("unused")
			public void openProfileWindow(final String pseudo)
			{
				runOnUiThread(new Runnable()
				{
					class ProfileDismissListenner implements PopupWindow.OnDismissListener
					{
						public void onDismiss()
						{
							if (profileTask != null)
							{
								profileTask.cancel(true);
								profileTask = null;
							}
						}
					}
					
					private void displayProfile(final PopupWindow pw, Profile profile)
					{
						Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
						final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						final LinearLayout profileView =  (LinearLayout) inflater.inflate(R.layout.profile_popup, null, false);
						pw.setContentView(profileView);
						profileView.setBackgroundResource(getKeyByTheme(getThemeKey(), R.drawable.class, "profile_popup_background"));
						float scale = getResources().getDisplayMetrics().density;
						float dip = 5;
						int pixel = (int) (dip * scale + 0.5f);
						profileView.setPadding(pixel, pixel, pixel, pixel);

						ImageView avatar = (ImageView) profileView.findViewById(R.id.ProfileAvatar);
						avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
						if (profile.getAvatarUrl() != null && profile.getAvatarBitmap() != null)
						{
							avatar.setImageBitmap(profile.getAvatarBitmap());
							int newWidth = Math.min(display.getWidth(), display.getHeight()) / 4;
							int orgWidth = avatar.getDrawable().getIntrinsicWidth();
							if (newWidth < orgWidth)
							{
								int orgHeight = avatar.getDrawable().getIntrinsicHeight();
								int newHeight = (int) Math.floor((orgHeight * newWidth) / orgWidth);

								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(newWidth, newHeight);
								avatar.setLayoutParams(params);
							}
						}
						else
						{
							avatar.setImageResource(R.drawable.no_avatar);
						}
						
						TextView pseudo = (TextView) profileView.findViewById(R.id.ProfilePseudo);
						pseudo.setText(profile.getPseudo());
						pseudo.setTextColor(currentTheme.getProfileText1Color());
						pseudo.setTextSize(getTextSize(18));

						TextView type = (TextView) profileView.findViewById(R.id.ProfileType);
						type.setTextColor(currentTheme.getProfileText2Color());
						type.setTextSize(getTextSize(12));
						type.setText(profile.getType().getLabel());
						
						TextView sexAndAge = (TextView) profileView.findViewById(R.id.ProfileSexAndAge);
						sexAndAge.setTextColor(currentTheme.getProfileText2Color());
						sexAndAge.setTextSize(getTextSize(12));
						if (profile.getBirthDate() != null)
						{
							Calendar now = Calendar.getInstance();
							Calendar birth = Calendar.getInstance();
							birth.setTime(profile.getBirthDate());
							boolean birthday = now.get(Calendar.DAY_OF_YEAR) == birth.get(Calendar.DAY_OF_YEAR);
							int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
							now.add(Calendar.YEAR, -age);
							if(birth.after(now)) age--;
							sexAndAge.setText(Html.fromHtml(profile.getGender().getLabel() + ", " +
							(birthday ? "<font color=\"red\">" + getString(R.string.profile_age_birthday, age) + "</font>" : getString(R.string.profile_age, age))));
						}
						else
						{
							sexAndAge.setText(profile.getGender().getLabel() + ", " + getString(R.string.profile_default_age));
						}
						
						TextView location = (TextView) profileView.findViewById(R.id.ProfileLocation);
						String city = profile.getCity() != null ? profile.getCity() : getString(R.string.profile_default_location);
						String locationStr = "";
						for (int i = 0; i < (profile.getLocation().length < 2 ? profile.getLocation().length : 2); i++)
						{
							locationStr += i != 0 ? ", " + profile.getLocation()[i] : profile.getLocation()[i];
						}
						location.setText(city + " (" + locationStr + ")");
						location.setTextColor(currentTheme.getProfileText2Color());
						location.setTextSize(getTextSize(10));
					
						TextView seniorityAndPosts = (TextView) profileView.findViewById(R.id.ProfileSeniorityAndPosts);
						seniorityAndPosts.setTextColor(currentTheme.getProfileText2Color());
						seniorityAndPosts.setTextSize(getTextSize(10));
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						String  seniority = getString("profile_seniority_" + profile.getGender().toString().toLowerCase(), sdf.format(profile.getRegistrationDate()));
						String posts = getResources().getQuantityString(R.plurals.profile_posts, profile.getNbPosts(), profile.getNbPosts());
						seniorityAndPosts.setText(seniority + ", " + posts);
						
						if (profile.getLastPostDate() != null)
						{
							long seconds = (new Date().getTime() - profile.getLastPostDate().getTime()) / 1000;
							String delta = "";
							if (seconds <= 300) delta = getString(R.string.profile_last_post_inf5min);
							else if (seconds < 3600) delta =  getString(R.string.profile_last_post_min, ((int) seconds / 60)); // Moins d'une heure
							else if (seconds < 86400) delta = getString(R.string.profile_last_post_hour, ((int) seconds / 3600)); // Moins d'une journée
							else delta = getResources().getQuantityString(R.plurals.profile_last_post_day, ((int) seconds / 86400), ((int) seconds / 86400));
						
							TextView lastPost = (TextView) profileView.findViewById(R.id.ProfileLastPost);
							lastPost.setTextColor(currentTheme.getProfileText2Color());
							lastPost.setTextSize(getTextSize(10));
							lastPost.setText(getString(R.string.profile_last_post, delta));
						}

						if (profile.getSmileysUrls().length > 0)
						{
							//final WebView smileysWebView = new WebView(PostsActivity.this);
							final WebView smileysWebView = new NonLeakingWebView(PostsActivity.this);
							
							int maxWidth = display.getWidth() / 5 * 4;
							// Smiley 70px + (2 * 5px de margin) + 10 en plus , avec mise à l'échelle * nb de smileys
							int targetWidth = (int) (10 + (profile.getSmileysUrls().length * 80 * smileysWebView.getScale()));
							Log.d(HFR4droidApplication.TAG, "max : " + maxWidth + " / calculated : " + targetWidth);
							smileysWebView.setLayoutParams(new LayoutParams(targetWidth < maxWidth ? targetWidth : maxWidth, LayoutParams.WRAP_CONTENT));
							smileysWebView.setBackgroundColor(0);

							StringBuffer css = new StringBuffer("<style type=\"text/css\">");
							css.append("body { margin: 0; padding: 0; } img { margin: 5px }");
							css.append("</style>");
							StringBuffer smiliesData = new StringBuffer();
							for (String smiley : profile.getSmileysUrls())
							{
								smiliesData.append("<img src=\"")
								.append(getDataRetriever().getImgPersoUrl())
								.append(smiley)
								.append("\" />");
							}
							smileysWebView.setWebChromeClient(new WebChromeClient()
							{
								public void onProgressChanged(WebView view, int progress)
								{
									if (progress > 33)
									{
										pw.showAtLocation(findViewById(R.id.PostsLayout), Gravity.LEFT, 0, 0);
									}
								}
							});
							
							smileysWebView.loadData("<html><head>" + uiHelper.fixHTML(css.toString()) + "</head>" +
							"<body>" + uiHelper.fixHTML(smiliesData.toString()) + "</body></html>", "text/html", "UTF-8");
							profileView.addView(smileysWebView);
							
							pw.setOnDismissListener(new ProfileDismissListenner()
							{
								public void onDismiss()
								{
									smileysWebView.destroy();
								}
							});
						}
						else
						{	
							pw.showAtLocation(findViewById(R.id.PostsLayout), Gravity.LEFT, 0, 0);
						}
					}

					public void run()
					{
						final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						final View waitingView =  inflater.inflate(R.layout.profile_popup_loading, null, false);
						final PopupWindow pw = new PopupWindow(waitingView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
						pw.setBackgroundDrawable(new BitmapDrawable());
						pw.setOutsideTouchable(true);
						pw.setAnimationStyle(R.style.Animation_ProfilPopup);
						
						waitingView.setBackgroundResource(getKeyByTheme(getThemeKey(), R.drawable.class, "profile_popup_background"));
						float scale = getResources().getDisplayMetrics().density;
						float dip = 20;
						int pixel = (int) (dip * scale + 0.5f);
						waitingView.setPadding(pixel, pixel, pixel, pixel);
						

						Profile profile = getHFR4droidApplication().getProfile(pseudo);
						if (profile != null)
						{
							displayProfile(pw, profile);
						}
						else
						{
							pw.setOnDismissListener(new ProfileDismissListenner());
							profileTask = new AsyncTask<String, Void, Profile>()
							{
								private ImageView wait = null;
								private long backgroundThreadId = -1;
								
								@Override
								protected void onPreExecute()
								{
									RotateAnimation anim = new RotateAnimation(0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
									anim.setInterpolator(new LinearInterpolator());
									anim.setRepeatCount(Animation.INFINITE);
									anim.setDuration(700);
	
									pw.showAtLocation(findViewById(R.id.PostsLayout), Gravity.LEFT, 0, 0);
									wait = (ImageView) waitingView.findViewById(R.id.WaitAnimation);
									wait.setBackgroundResource(getDrawableKey(currentTheme.getProfileSpinner()));
									wait.startAnimation(anim);
								}
	
								@Override
								protected void onCancelled()
								{
									getHFR4droidApplication().getHttpClientHelper().abortRequest(backgroundThreadId);
								}
								
								@Override
								protected Profile doInBackground(String... pseudo)
								{
									backgroundThreadId = Thread.currentThread().getId();
									Profile profile = null;
									try
									{
										profile = getDataRetriever().getProfile(pseudo[0]);
										if (profile != null && profile.getAvatarUrl() != null)
										{
											profile.setAvatarBitmap(imgBitmapHttpClient.doGet(profile.getAvatarUrl()));
										}
										getHFR4droidApplication().setProfile(pseudo[0], profile);
									}
									catch (Exception e)
									{
										error(e, true, true);
									}
									return profile;
								}
								
								@Override
								protected void onPostExecute(final Profile profile)
								{	
									//wait.clearAnimation();
									pw.setOnDismissListener(null);
									profileTask = null;
									pw.dismiss();
	
									if (profile != null)
									{
										displayProfile(pw, profile);
									}
								}
							}.execute(pseudo);
						}
					}
				});
			}

			@SuppressWarnings("unused")
			public void handleUrl(String url)
			{
				try
				{
					MDUrlParser urlParser = new HFRUrlParser(getDataRetriever());
					if (urlParser.parseUrl(url))
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
									keepNavigationHistory = true;
									topic.setLastReadPost(t.getLastReadPost());
									loadPosts(topic, urlParser.getPage(), false);
								}
							}
							else
							{
								// Topic différent, on change de topic
								keepNavigationHistory = true;
								loadPosts(t, urlParser.getPage(), false);
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
				new ProgressDialogAsyncTask<String, Void, String>(PostsActivity.this)
				{
					@Override
					protected void onPreExecute() 
					{
						super.onPreExecute();
						progressDialog.setMessage(getString(R.string.getting_keywords, code));
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

		StringBuffer js = new StringBuffer("<script type=\"text/javascript\">");
		js.append("function swap_spoiler_states(obj){var div=obj.getElementsByTagName('div');if(div[0]){if(div[0].style.visibility==\"visible\"){div[0].style.visibility='hidden';}else if(div[0].style.visibility==\"hidden\"||!div[0].style.visibility){div[0].style.visibility='visible';}}}");
		js.append("function scrollToElement(id) {var elem = document.getElementById(id); var x = 0; var y = 0; while (elem != null) { x += elem.offsetLeft; y += elem.offsetTop; elem = elem.offsetParent; } window.scrollTo(x, y); }");
		js.append("function removePost(id) { var header = document.getElementById(id); header.parentNode.removeChild(header.nextSibling); if (header.nextSibling.className == 'HFR4droid_post') header.parentNode.removeChild(header.nextSibling); header.parentNode.removeChild(header); };");
		js.append("function openQuickActionWindow(postId, isMine) {var elem = document.getElementById(postId); var yOffset = 0; while (elem != null) { yOffset += elem.offsetTop; elem = elem.offsetParent; } window.HFR4Droid.openQuickActionWindow(postId, isMine, yOffset - window.scrollY); }");
		js.append("function openProfileWindow(pseudo) { event.stopPropagation(); window.HFR4Droid.openProfileWindow(pseudo); }");
		js.append("function jumpToFirstPost() { scrollToElement(" + posts.get(0).getId() + "); }");
		js.append("function jumpToLastPost() { scrollToElement(" + NewPostUIHelper.BOTTOM_PAGE_ID + "); }");
		js.append("var loadDynamicCss = function(width) { var headID = document.getElementsByTagName('head')[0]; var styles = headID.getElementsByTagName('style'); for (var i=1;i<styles.length;i++) headID.removeChild(styles[i]); var cssNode = document.createElement('style'); cssNode.type = 'text/css'; cssNode.appendChild(document.createTextNode('");
		js.append("ol { width:' + (Math.round(width * 0.80) - 40) + 'px; }");
		js.append(".citation p, .oldcitation p, .quote p, .oldquote p, .fixed p, .code p, .spoiler p, .oldspoiler p { width:' + Math.round(width * 0.80) + 'px; }");
		js.append(".HFR4droid_post { width:' + width + 'px; word-wrap: break-word; padding-top: 5px; }");
		js.append(".HFR4droid_content img { max-width: ' + (width - 30) + 'px; }");
		js.append(".citation img, .oldcitation img, .quote img, .oldquote img, .fixed img, .code img, .spoiler img, .oldspoiler img { max-width: ' + (Math.round(width * 0.80) - 15) + 'px; }");
		js.append("')); headID.appendChild(cssNode); };");
		js.append("var scrollFct = function () { document.getElementById('top').style.visibility = window.pageYOffset == 0 ? 'hidden' : 'visible'; document.getElementById('bottom').style.visibility = window.pageYOffset == (document.height - window.innerHeight) ? 'hidden' : 'visible'; }; ");
		if (topic.getLastReadPost() != -1 || topic.getStatus() == TopicStatus.NEW_MP)
		{
			js.append("window.onload = function () { scrollToElement(\'" + (topic.getStatus() == TopicStatus.NEW_MP ? NewPostUIHelper.BOTTOM_PAGE_ID : topic.getLastReadPost()) + "\'); };");
			topic.setLastReadPost(-1);
		}
		js.append("window.setTimeout(function(){ scrollFct(); window.onscroll = scrollFct; }, 1000);");
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
		css.append(".HFR4droid_posts_container { min-height:100%; }");
		css.append(".HFR4droid_header { width:100%; ");
		switch (getPoliceSize())
		{
			case 3:
				css.append("background: " + currentTheme.getPostHeaderColorAsString() + " repeat-x bottom; height: 70px;");
				break;
				
			case 4:
				css.append("background: " + currentTheme.getPostHeaderColorAsString() + " repeat-x bottom; height: 90px;");
				break;
				
			default:
				css.append("background: url(\"" + currentTheme.getPostHeaderData() + "\") repeat-x bottom; height: 50px;");
				break;
		}
		css.append(" text-align: right; }");
		css.append(".HFR4droid_header div { position: absolute; margin: 5px 0 0 5px; width:90%; text-align: left; }");
		css.append(".HFR4droid_header div img { float: left; max-width:60px; max-height:");
		switch (getPoliceSize())
		{
			case 3:
				css.append("55");
				break;
				
			case 4:
				css.append("70");
				break;
				
			default:
				css.append("40");
				break;
		}
		css.append("px; margin-right:5px; }");
		css.append(".HFR4droid_header span.pseudo { color:" + currentTheme.getPostPseudoColorAsString() + "; font-size: " + getTextSize(16) + "px; font-weight:bold; }");
		css.append(".HFR4droid_header span.date { display: block; font-style:italic; color:" + currentTheme.getPostDateColorAsString() + "; font-size: " + getTextSize(12) + "px; margin: ");
		switch (getPoliceSize())
		{
			case 2:
				css.append("2");
				break;

			case 3:
			case 4:
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
		css.append(".HFR4droid_footer_space { height: 10px; }");
		css.append(".HFR4droid_footer { height: 10px; width:100%; margin-top: -10px; background: url(\"data:image/ng;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAA%2FCAMAAAAWu1JmAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAgY0hSTQAAeiYAAICEAAD6AAAAgOgAAHUwAADqYAAAOpgAABdwnLpRPAAAAwBQTFRFhYWFs7SzysrKy8vLyszKzMzMzc3Nzs7Oz8%2FP0NDQ0dHR0dLR0tLS09PT1NTU1dXV1tbW19fX2NjY2dnZ2tna2tra29rb3Nvc3Nzc3dzdAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWKfi1AAAABh0RVh0U29mdHdhcmUAUGFpbnQuTkVUIHYzLjM2qefiJQAAAEFJREFUGFddwkcOgDAQBMHBhCWDCQb%2B%2F1Fai8TBqlKhSoOiDiVdejK3PgkndrchYsXiZkwY0bsO7c9kalCjdAF6ARIIA4Sqnjr8AAAAAElFTkSuQmCC\"); }");
		css.append(".deleted_post { background-image: url(" + currentTheme.getModoPostDeletedData() + "); filter: alpha(opacity=40); -moz-opacity:0.4; -khtml-opacity: 0.4; opacity:0.4; }");
		css.append(".jumper { position: fixed; alpha(opacity=5); opacity:0.05; right: 10px; width: 48px; height: 48px; }");
		css.append("#bottom { visibility: hidden; bottom: 10px; background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A%2FwD%2FoL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB90HBA8ZHaxsbOoAAAN8SURBVGje7Zo7TCNXFIa%2FuWODTTAESBAP4QZWZpBoQ0tavA0SDUVAAoUKSgokxKPCu5CWAiFS8BASEuTlgABhBLIpAKE06VIgpaIjkaJFkDkpsl6xu6w9Y9%2BxMtH%2B0pWs8Z0z57%2F%2FOfeOzhmDf6GAr4B%2B4DPyIwBEKC0egD%2BB34FvgNPHf64A4qPxN%2FB11vkvfeZ8drwCGhXwHH%2BiHHiugBr8i4gCPvUxgQr1egfyK8IKMHRavL6%2BRkSeHN3d3boJqJKufllZmXab2hUwDCPHcilPCJQMHhAw%2FhcKGD5WQH8IlVgB46MCH3NAQwjh9xDKqYBlWfT29mpx0imBaDTKwMCAnhwIh8NsbGzQ09PjyKBpmgXlRxYNDQ3s7u4Si8X0KGCaJqFQiO3tbeLxeFEE8inQ0tLC6ekpHR0dTt%2Bb8udA1qHy8nJ2dnbo6%2BvzRIFoNEoqlaKtrQ2AYDCoR4FAIPDmdzAYZHNzk8HBQa0KxGIxMpkMra2tbz1LSw68%2B1DTNFlZWWF4eNh1mDylgGVZpFIpmpub37pu27Z3B5lSiqWlJUZGRopSoLOzk%2BPjYxobG9%2Bbe39%2F7%2B1JrJRicXGR%2Fv7%2BD4ZcLgXa29vZ29ujvr7%2B6SrWw4NjAlIoCdM0WVtbY2hoyNU5YFkWR0dHNDU1fXCuGwVyEri7u8urxPLyMmNjYyilcu40hmHQ1dVFJpN5Mmwe4%2Bbmxon%2FArBLnirY7Oys5INt2zI%2BPp5zzurqqtze3ua1tb6%2BLqZpOqnOvQT42cFEmZmZkVJga2tLAoGA0%2FKicwKALCwseOr8%2Fv6%2BhEIhN%2FXRlwBJFzd4RuLw8FDC4bDbAu8LgJ9c3iTz8%2FNanU%2Bn01JZWVlIhbowAjpJnJ2dSSQSKbTE%2FgLgxwJvLprE1dWV1NbWFtMjSAD8UIQBmZycLHjlq6uri21yFE8AkEQi4cr5y8tLqaur09GlSQB8r8GQTE9PO3L%2B4uKi2LB5j8B3mozJ1NRUTufPz8%2BlpqZGZ59sTiuBXDmRTqelqqpKd6NvDmBHs1EZHR0V27bfOH9wcCAVFRVedCrnADY9MCwTExMiInJyclLMPp9vTAN865Fxicfjbt9t3I7xAPCXV5W4ZDLpdbHvlQJ%2Bwb%2B4AggDv%2BG%2FTw0OHjN59pqNX5xPArW8U9QygC%2BAz4Fq4JP%2FWLj8AdwC18Cv2Yv%2FAG7AaSkoJTUWAAAAAElFTkSuQmCC\"); }");
		css.append("#top { visibility: hidden; top: 10px; background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAABmJLR0QA%2FwD%2FAP%2BgvaeTAAADoUlEQVRoge2YT0gjVxzHPxkjRjGKEYSFwp72spelp0CoYDwpnvQk4klcBVE8FA%2BC%2BOdUW715ExRUxIKC%2By8W1IMIIq6KB0ENKAVFI7ZgZWnVqPP20B0xu8nMm8lLjKUfeJDM%2B8173%2B9835tkxkUsL4HnQCFQQGbxN3AB%2FAF8BMTDTh8Q%2BnLwKbQt4MVDAwsZIMpuOwByAX7IADFOW5MGfM%2FT5ZUGeFI1usfjoaqqKlXDA%2BQB9JCCeL1er1heXhZCCNHZ2ZmqJfQrwE%2BqB87LyxMLCwvCQNd10dramgoDs8oNFBQUiJWVFRGPrq4u1QbeKDVQVFQk1tfX44o36O7uVm6gX8VgPp9PbGxsmIo36OnpUWXgrRIDxcXFYnNzU0q8QX9%2FvwoD75I2UFhYKFZXV22JN1CwJ5Iz4PP5xNbWliPxBgMDA8kYeA%2Fws5OTvV6v4yuv0MQHRwby8%2FMT3irTbMK%2BgdzcXLG4uKhUvMHg4KBdAyGAX2RP8Hg8Yn5%2BPiXiHZqYkzbgdrvF9PR0SsUb9Pb2qjWQlZUlJicnLSe%2BuLgQExMTpjUdHR1C13XLsfr6%2BmQM%2FCZloL293XLC8%2FNz4ff7RXNzs2mdpmmira1NyoTf77c0oAEuLCgpKTHtj0QiBAIB1tbWEEIkrBNCoOs6Q0NDNDY2ouu66bg5OTlW0lyaVQVAdnZ2wr6TkxPKy8vZ3d0FMBX1sG90dJT6%2Bnru7u5kJCRCzoDb7Y57%2FOzsjIqKCvb29u6PmSVwe3sb831qaoqWlhbLJMxwnEAkEqGsrIzt7e2Y42Zi4l3t4eFhmpqaHJuQMqBpsWXHx8cEg8H7ZfMQswQSiRwZGaGhoeEbgzKmpAzc3Nzcfz44OCAQCBAOh22JhPgJGIyNjVFbWxsz19dLLg7CloH9%2FX2CwSCHh4eJRzRJwGrDzszMUF1dzfX1tVQ9%2FJtA4hm%2FEI1G2dnZobS0lKOjI9NapwkYhEIhampquLq6kqmXSyAcDlNZWcnp6allbTIJGMzNzVFXV8fl5aVlbfz741eMj49LTQzyvwNWzM7OypTJJWCHZO7pTpDaA3ZIs4H0JmC2P5zy1JeQSOsS%2Bj%2BBb3nyeyC9SygV%2FCcSUEo0GlU9pCluQGnmS0tLuFyWj9mqEBpg%2FY8pc9E14J%2FHVpEEf2nAp8dWkQTnLuAZ8Dtg%2BRImAyk3PrwG7rD%2Fevsx2yjEvpUrBX4EvgO8SD7sKOQTYPkUD%2FwJTAETgP4ZmQ5UTPRxaekAAAAASUVORK5CYII%3D\"); }");
		css.append("</style>");

		Display display = getWindowManager().getDefaultDisplay(); 
		int width = Math.round(display.getWidth() / webView.getScale());
		StringBuffer js2 = new StringBuffer("<script type=\"text/javascript\">");
		js2.append("loadDynamicCss(" + width + ");");
		js2.append("</script>");

		setDrawablesToggles();
		StringBuffer postsContent = new StringBuffer("");
		postsContent.append("<div onclick=\"jumpToLastPost()\" id=\"bottom\" class=\"jumper\"></div>");
		postsContent.append("<div onclick=\"jumpToFirstPost()\" id=\"top\" class=\"jumper\"></div>");
		postsContent.append("<div class=\"HFR4droid_posts_container\">");
		for (Post p : posts)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("'Le' dd/MM/yyyy à HH:mm:ss");
			SimpleDateFormat todaySdf = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat todaySdf2 = new SimpleDateFormat("à HH:mm:ss");
			String date = formatDate(todaySdf, sdf, p.getDate());
			String avatar = p.getAvatarUrl() != null && isAvatarsEnable ? "<img alt=\"avatar\" title=\"" + p.getPseudo() + "\" src=\"" + p.getAvatarUrl() + "\" onclick=\"openProfileWindow('" + p.getPseudo().replace("'", "\\'") + "')\" />" : "";
			String pseudoSpan = "<span class=\"pseudo\" onclick=\"openProfileWindow('" + p.getPseudo().replace("'", "\\'") + "')\">" + p.getPseudo() + "</span>";
			String dateSpan = "<span class=\"date\" onclick=\"openQuickActionWindow(" + p.getId() + ", " + p.isMine() + ")\">" + date + "</span>";
			StringBuilder editQuoteDiv = new StringBuilder("");
			if (p.getLastEdition() != null || p.getNbCitations() > 0)
			{
				editQuoteDiv.append("<div class=\"HFR4droid_edit_quote\">");
				if (p.getLastEdition() != null)
				{
					editQuoteDiv.append("Edité " + formatDate(todaySdf2, sdf, p.getLastEdition()));
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
			content += "\">";
			if (getQuotedEditedDisplayType() != QuotedEditedDisplayType.BOTTOM) content += editQuoteDiv;
			content += "<div class=\"HFR4droid_content\"";
			String postContent = p.getContent();
			if (preloading && oldCitation != null && !oldCitation)
			{
				postContent = postContent.replaceAll("</p><hr size=\"1\" />", "<br /><br />");
				postContent = postContent.replaceAll("<hr size=\"1\" />", "");
				postContent = postContent.replaceAll("\"oldcitation\">", "\"citation\">");
			}
			content += ">" + postContent + "</div>";
			if (getQuotedEditedDisplayType() != QuotedEditedDisplayType.TOP) content += editQuoteDiv;
			content += "</div>";
			content = content.replaceAll("onload=\"md_verif_size\\(this,'Cliquez pour agrandir','[0-9]+','[0-9]+'\\)\"", "onclick=\"return true;\"");
			content = content.replaceAll("<b\\s*class=\"s1\"><a href=\"(.*?)\".*?>(.*?)</a></b>", "<b onclick=\"window.HFR4Droid.handleUrl('" + getDataRetriever().getBaseUrl() + "$1');\" class=\"s1\">$2</b>");
			content = content.replaceAll("<a\\s*href=\"(http://forum\\.hardware\\.fr.*?)\"\\s*target=\"_blank\"\\s*class=\"cLink\">", "<a onclick=\"window.HFR4Droid.handleUrl('$1');\" class=\"cLink\">");			
			if (!isSmileysEnable) content = content.replaceAll("<img\\s*src=\"http://forum\\-images\\.hardware\\.fr.*?\"\\s*alt=\"(.*?)\".*?/>", "$1");
			content = content.replaceAll("<img\\s*src=\"http://forum\\-images\\.hardware\\.fr/images/perso/(.*?)\"\\s*alt=\"(.*?)\"", "<img onclick=\"window.HFR4Droid.editKeywords('$2');\" src=\"http://forum-images.hardware.fr/images/perso/$1\" alt=\"$2\"");
			if (!isImgsEnable) content = content.replaceAll("<img\\s*src=\"https?://[^\"]*?\"\\s*alt=\"https?://[^\"]*?\"\\s*title=\"(https?://.*?)\".*?/>", "<a href=\"$1\" target=\"_blank\" class=\"cLink\">$1</a>");
			content = content.replaceAll("ondblclick=\".*?\"", "");
			if (p.isDeleted()) postsContent.append("<div class=\"deleted_post\">");
			postsContent.append(header);
			postsContent.append(content);
			if (p.isDeleted()) postsContent.append("</div>");
			if (oldCitation == null)
			{
				// Si citation il y a...
				if (p.getContent().indexOf("citation\">") != -1)
				{
					// On mémorise le type de citation
					oldCitation = p.getContent().indexOf("\"oldcitation\">") != -1;
				}
			}
		}
		postsContent.append("<div class=\"HFR4droid_footer_space\"></div></div><div id=\"" + NewPostUIHelper.BOTTOM_PAGE_ID + "\" class=\"HFR4droid_footer\" />");
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
		if (!preloading)
		{
			final ProgressBar progressBar = (ProgressBar) findViewById(R.id.PostsProgressBar);
			progressBar.setVisibility(View.VISIBLE);
			webView.setWebChromeClient(new WebChromeClient()
			{				
				public void onProgressChanged(WebView view, int progress)
				{
					progressBar.setProgress(progress);
					if (progress == 100)
					{
						if (currentQAwindow != null) currentQAwindow.dismiss();
						scrollToLastPos();
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
		}
		else
		{
			webView.setWebChromeClient(new WebChromeClient()
			{				
				public void onProgressChanged(WebView view, int progress)
				{
					if (progress == 100) scrollToLastPos();
				}
			});
		}
		
		if (!preloading) setView(webView);
		String meta = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no\">";
		webView.loadDataWithBaseURL(getDataRetriever().getBaseUrl(), "<html><head>" + meta + js.toString() + css.toString() + js2.toString() + "</head><body>" + postsContent.toString() + "</body></html>", "text/html", "UTF-8", null);
		supportInvalidateOptionsMenu();

		return webView;
	}
	
	private void scrollToLastPos()
	{
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						if (currentScrollY != -1)
						{
							if (getWebView() != null) getWebView().scrollTo(0, currentScrollY);
							currentScrollY = -1;
						}
					}
				});
			}
		}, 500);
	}
	
	private String getPostUrl(long postId)
	{
		final StringBuilder postLink = new StringBuilder(getDataRetriever().getBaseUrl() + "/forum2.php?config=hfr.inc");
		postLink.append("&cat=").append(topic.getCategory().getId());
		postLink.append("&post=").append(topic.getId());
		postLink.append("&page=").append(currentPageNumber);
		postLink.append("#t").append(postId);
		return postLink.toString();
	}

	protected void addQuickActionWindowItems(HFR4droidQuickActionWindow window, final long currentPostId, boolean isMine)
	{
		final String postLink = getPostUrl(currentPostId);

		if (topic.getStatus() != TopicStatus.LOCKED)
		{
			if (isMine)
			{
				QuickActionWindow.Item edit = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_edit, new PostCallBack(PostCallBackType.EDIT, currentPostId, true) 
				{
					@Override
					protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
					{
						String content = getDataRetriever().getPostContent(p);
						return content != null ? content.substring(0, content.length() - 1) : null;
					}

					@Override
					protected void onActionExecute(String data)
					{
						showAddPostDialog(PostCallBackType.EDIT, data, postId);	
					}
				});
				window.addItem(edit);	

				QuickActionWindow.Item delete = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_delete, new PostCallBack(PostCallBackType.DELETE, currentPostId, true, true)
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
							if (webView != null) webView.loadUrl("javascript:removePost(" + postId + ")");
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
			if (isLoggedIn()) window.addItem(quote);

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
			if (isLoggedIn()) window.addItem(multiQuote);
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
		if (isLoggedIn() && !isMpsCat(topic.getCategory())) window.addItem(addFavorite);
		
		QuickActionWindow.Item aqLink = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_aq, new QuickActionWindow.Item.Callback()
		{
			private void showAlerts()
			{
				if (alertsQualitay != null)
				{					
					final List<AlertQualitay> alerts = new ArrayList<AlertQualitay>(alertsQualitay);
					alerts.add(0, new AlertQualitay(-1, getString(R.string.aq_new), null, null, new Long[0]));
					ArrayAdapter<AlertQualitay> adapter = new ArrayAdapter<AlertQualitay>(PostsActivity.this, R.layout.select_dialog_singlechoice, alerts);

					int preselectedAlert = 0;
					for (AlertQualitay alert : alerts)
					{
						if (Arrays.asList(alert.getPostIds()).indexOf(currentPostId) != -1)
						{
							preselectedAlert = alerts.indexOf(alert);
							break;
						}
					}
					new AlertDialog.Builder(PostsActivity.this)
					.setTitle(R.string.aq_list_title)
					.setSingleChoiceItems(adapter, preselectedAlert, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							final AlertQualitay selectedAlert = alerts.get(which);
							
							LayoutInflater inflater = (LayoutInflater) PostsActivity.this.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
							final View layout = inflater.inflate(R.layout.alerte_qualitay, null);
							if (selectedAlert.getAlertQualitayId() != -1) layout.findViewById(R.id.AQ_name).setVisibility(View.GONE);

							final AlertDialog d = new AlertDialog.Builder(PostsActivity.this)
							.setTitle(R.string.aq_new_title)
							.setView(layout)
							.setPositiveButton(R.string.aq_sent, new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which) {}
							})
							.setNegativeButton(PostsActivity.this.getString(R.string.button_cancel), new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which) {}
							})
							.create();

							d.setOnShowListener(new DialogInterface.OnShowListener()
							{
							    public void onShow(DialogInterface dialog)
							    {
							    	d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
							        {
							            public void onClick(View view)
							            {
											final String name = ((EditText) layout.findViewById(R.id.AQ_name)).getText().toString().trim();
											final String comment =  ((EditText) layout.findViewById(R.id.AQ_comment)).getText().toString().trim();
											if (selectedAlert.getAlertQualitayId() == -1 && name.length() == 0)
											{
												Toast.makeText(PostsActivity.this, getString(R.string.aq_name_mandatory), Toast.LENGTH_SHORT).show();
												return;
											}

											new MessageResponseAsyncTask(PostsActivity.this, getString(R.string.aq_sent_loading))
											{
												@Override
												protected HFRMessageResponse executeInBackground() throws HFR4droidException
												{
													Post p = new Post(currentPostId);
													p.setTopic(topic);
													
													return getMessageSender().alertPost(
														selectedAlert.getAlertQualitayId(),
														selectedAlert.getAlertQualitayId() == -1 ? name : null,
														p,
														postLink,
														comment.length() > 0 ? comment : null);
												}

												@Override
												protected void onActionFinished(String message)
												{
													Toast.makeText(PostsActivity.this, message, Toast.LENGTH_SHORT).show();	
												}	
											}.execute();
											d.dismiss();
							            }
							        });
							    }
							});
							
							d.show();
						}
					})
					.show();
				}
			}
			
			public void onClick(QuickActionWindow window, Item item, View anchor)
			{
				getConfirmDialog(
				PostsActivity.this,
				getString(R.string.aq_new_title),
				getString(R.string.aq_r_u_sure),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1)
					{
						if (alertsQualitay == null)
						{
							new ProgressDialogAsyncTask<Topic, Void, List<AlertQualitay>>(PostsActivity.this)
							{
								@Override
								protected void onPreExecute() 
								{
									super.onPreExecute();
									progressDialog.setMessage(getString(R.string.aq_loading));
									progressDialog.show();
								}
			
								@Override
								protected List<AlertQualitay> doInBackground(Topic... params)
								{
									setThreadId();
									try
									{
										return getDataRetriever().getAlertsByTopic(params[0]);
									}
									catch (DataRetrieverException e)
									{
										error(e, true, true);
										return null;
									}
								}
			
								@Override
								protected void onPostExecute(List<AlertQualitay> alerts)
								{
									progressDialog.dismiss();
									if (alerts != null)
									{
										alertsQualitay = new ArrayList<AlertQualitay>();
										alertsQualitay.addAll(alerts);
										showAlerts();
									}
								}
							}.execute(topic);
						}
						else
						{
							showAlerts();
						}
					}
				}).show();
			}
		});					
		if (isLoggedIn() && Build.VERSION.SDK_INT >= 8 && !isMpsCat(topic.getCategory())) window.addItem(aqLink);
		
		QuickActionWindow.Item modoLink = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_modo, new QuickActionWindow.Item.Callback()
		{
			public void onClick(QuickActionWindow window, Item item, View anchor)
			{
				getConfirmDialog(
				PostsActivity.this,
				getString(R.string.warm_modo_title),
				getString(R.string.modo_r_u_sure),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(PostsActivity.this);
						builder.setTitle(R.string.warm_modo_title); 
						final EditText reason = new EditText(PostsActivity.this);
						reason.setHint(R.string.modo_reason_hint);
						reason.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
						builder.setView(reason);

						builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener()
						{  
							public void onClick(DialogInterface dialog, int whichButton)
							{
								final Post p = new Post(currentPostId);
								p.setTopic(topic);
								new MessageResponseAsyncTask(PostsActivity.this, getString(R.string.warm_modo_loading))
								{						
									@Override
									protected HFRMessageResponse executeInBackground() throws HFR4droidException
									{
										return getMessageSender().warmModeration(p, reason.getText().toString(), getDataRetriever().getHashCheck());
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

						builder.create().show();
					}
				}).show();
			}
		});					
		if (!isMpsCat(topic.getCategory())) window.addItem(modoLink);
		
		if (!isMine)
		{
			QuickActionWindow.Item sendMP = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_message_to, new QuickActionWindow.Item.Callback()
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
		
		QuickActionWindow.Item copyContent = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_copy, new PostCallBack(PostCallBackType.COPY_CONTENT, currentPostId, true)
		{
			@Override
			protected String doActionInBackground(Post p) throws DataRetrieverException, MessageSenderException
			{
				return getDataRetriever().getPostContent(p);
			}

			@Override
			protected void onActionExecute(String data)
			{
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				clipboard.setText(cleanBBCode(data));
				Toast.makeText(PostsActivity.this, getText(R.string.copy_post_content), Toast.LENGTH_SHORT).show();
			}
		});
				
		window.addItem(copyContent);
		
		QuickActionWindow.Item shareLink = new QuickActionWindow.Item(PostsActivity.this, "", R.drawable.ic_menu_share, new QuickActionWindow.Item.Callback()
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
			uiHelper.addPostButtons(this, layout);
			uiHelper.applyTheme(currentTheme, (ViewGroup) postDialog.findViewById(R.id.PostContainer).getParent());
			((EditText) postDialog.findViewById(R.id.InputPostContent)).setTextSize(getTextSize(14));
			((EditText) postDialog.findViewById(R.id.InputSmileyTag)).setTextSize(getTextSize(14));

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
								uiHelper.hideWikiSmiliesResults(layout);
								return true;
							}
							else if (postDialog.findViewById(R.id.SmileySearch).getVisibility() == View.VISIBLE)
							{
								uiHelper.hideWikiSmiliesSearch(layout);
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
	
	private NewPostUIHelper getNewPostUIHelper()
	{
		return new NewPostUIHelper()
		{
			@Override
			protected void showWikiSmiliesResults(ViewGroup layout)
			{
				layout.findViewById(R.id.PostContainer).setVisibility(View.GONE);	
			}
			
			@Override
			public void hideWikiSmiliesResults(ViewGroup layout)
			{
				destroyWikiSmiliesResults(layout);
				if (layout != null) layout.findViewById(R.id.PostContainer).setVisibility(View.VISIBLE);	
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
								InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(postContent.getWindowToken(), 0);
								if (!super.handleCodeResponse(code))
								{
									switch (code)
									{	
										case POST_EDIT_OK: // Edit ok
											topic.setLastReadPost(postId);
											onPostingOk(code, postId);
											return true;									
				
										case POST_ADD_OK: // New post ok
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
			
			@Override
			protected void onRehostOk(String url)
			{
				insertBBCode((EditText) postDialog.findViewById(R.id.InputPostContent), url, "");
				postDialog.show();	
			}
			
			@Override
			public ViewGroup getSmiliesLayout()
			{
				return postDialog != null ? (ViewGroup) postDialog.findViewById(R.id.PostContainer).getParent() : null;
			}
		};
	}
	
	protected void showWikiSmiliesResults(ViewGroup layout)
	{
		layout.findViewById(R.id.PostContainer).setVisibility(View.GONE);
	}
	
	protected void onPostingOk(ResponseCode code, long postId)
	{
		EditText postContent = (EditText) postDialog.findViewById(R.id.InputPostContent);
		postContent.setText("");
		postDialog.dismiss();

		switch (code)
		{	
			case POST_EDIT_OK: // Edit ok
				reloadPage();
				break;

			case POST_ADD_OK: // New post ok
				topic.setLastReadPost(NewPostUIHelper.BOTTOM_PAGE_ID);
				if (currentPageNumber == topic.getNbPages()) reloadPage();
				if (currentPageNumber == topic.getNbPages() - 1 // On est sur la page n-1, on rafraichit la dernière page
				&& getCurrentIndex() < 2 && getView(getCurrentIndex() + 1) != null && getDatasource(getCurrentIndex() + 1) != null)
				{
					topic.setLastReadPage(-1); // On marque la dernière page en lue car un message a été posté et on ne doit pas utilisé le fake account
					removeView(getCurrentIndex() + 1);
					preLoadingPostsAsyncTask = new PreLoadingPostsAsyncTask(PostsActivity.this);
					preLoadingPostsAsyncTask.execute(topic.getNbPages(), topic);
				}
				break;
		}
	}
	
	private void setDrawablesToggles()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		boolean isWifiEnable = info != null && (info.getType() == ConnectivityManager.TYPE_WIFI);
		
		isAvatarsEnable = getToggleFromPref(getAvatarsDisplayType(), isWifiEnable);
		isSmileysEnable = getToggleFromPref(getSmileysDisplayType(), isWifiEnable);
		isImgsEnable = getToggleFromPref(getImgsDisplayType(), isWifiEnable);
	}
	
	private boolean getToggleFromPref(DrawableDisplayType type, boolean isWifiEnable)
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
		return drawMe;
	}
	
	@Override
	protected void applyTheme(Theme theme)
	{
		LinearLayout postLayout = (LinearLayout) findViewById(R.id.PostsLayout);
		postLayout.setBackgroundColor(theme.getListBackgroundColor());
		postLayout.setBackgroundResource(getDrawableKey(currentTheme.getPostLoading()));
		
		if (postDialog != null)
		{
			uiHelper.applyTheme(theme, (ViewGroup) postDialog.findViewById(R.id.PostContainer).getParent());
		}
		
		EditText searchPostsWord = (EditText) findViewById(R.id.SearchPostsWord);
		searchPostsWord.setTextColor(theme.getPostTextColor());
		
		EditText searchPostsPseudo = (EditText) findViewById(R.id.SearchPostsPseudo);
		searchPostsPseudo.setTextColor(theme.getPostTextColor());
		
		Button buttonSearchPosts = (Button) findViewById(R.id.ButtonSearchPosts);
		buttonSearchPosts.setTextColor(theme.getPostTextColor());
	}
	
	@Override
	protected void customizeActionBar()
	{
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		LayoutInflater inflator = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.posts_title, null);
		getSupportActionBar().setCustomView(v);
		
		final TextView topicTitle = (TextView) v.findViewById(R.id.TopicTitle);
		topicTitle.setOnClickListener(new OnClickListener()
		{	
			public void onClick(View v)
			{
				 TextView topicTitle = (TextView) v;
				 topicTitle.setEllipsize(topicTitle.getEllipsize() == TruncateAt.MARQUEE ? TruncateAt.END : TruncateAt.MARQUEE);
			}
		});
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

	protected void searchPosts(Topic topic, final String pseudo, final String word, final Post fromPost, boolean sameActivity)
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
				activity.displayPosts(posts);
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
				bundle.putSerializable("fromTopicType", fromType);
				bundle.putBoolean("fromAllCats", fromAllCats);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}.execute(progressTitle, progressContent, noElement, sameActivity, topic);
	}

	protected Post getPostById(long postId)
	{
		if (getDatasource() == null) return null;
		for (Post p : getDatasource())
		{
			if (p.getId() == postId) return p;
		}
		return null;
	}
	
	protected TopicType getFromType()
	{
		return fromType;
	}
	
	protected boolean isFromAllCats()
	{
		return fromAllCats;
	}
	
	private String cleanBBCode(String bbCode)
	{
		return bbCode.replaceAll("\\[\\/?(?:b|i|u|strike|quote|fixed|code|url|img|\\*|spoiler)*?.*?\\]", "");
	}
	
	@Override
	public void destroyView(View v)
	{
		if (v != null && v instanceof WebView)
		{
			((WebView) v).destroy();
		}
	}

	/* Classes internes */

	private abstract class PostCallBack extends ProgressDialogAsyncTask<Void, Void, String> implements QuickActionWindow.Item.Callback
	{
		protected PostCallBackType type;
		protected long postId;
		private boolean progress;
		private boolean confirm;

		public PostCallBack(PostCallBackType type, long postId, boolean progress)
		{
			super(PostsActivity.this);
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
				PostsActivity.this,
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
				super.onPreExecute();
				progressDialog.setMessage(getString(type.getKey() + "_loading"));
				progressDialog.show();
			}
		}

		@Override
		protected String doInBackground(Void... params)
		{
			setThreadId();
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
	
	@Override
	public View buildView(List<Post> posts)
	{
		return getHFR4droidApplication().isLightMode() ? new View(this) : displayPosts(posts, true);
	}
	
	@Override
	protected void restoreView(int index)
	{
		if (getDatasource(index) != null)
		{
			View v = index == getCurrentIndex() ? displayPosts(getDatasource(index), true) : buildView(getDatasource(index));
			setView(index, v);
			space.addView(v, index);
		}
	}
	
	protected class PreLoadingPostsAsyncTask extends PreLoadingAsyncTask<Post, Topic, List<Post>>
	{
		public PreLoadingPostsAsyncTask(HFR4droidMultiListActivity<List<Post>> context)
		{
			super(context);
		}

		public PreLoadingPostsAsyncTask(HFR4droidMultiListActivity<List<Post>> context, int... otherPageNumbers)
		{
			super(context, otherPageNumbers);
		}

		@Override
		protected List<Post> getDatasource(List<Post> posts)
		{
			return posts;
		}

		@Override
		protected void loadAnotherPage(int pageNumber)
		{
			preLoadingPostsAsyncTask = new PreLoadingPostsAsyncTask(PostsActivity.this);
			preLoadingPostsAsyncTask.execute(pageNumber, topic);
		}

		@Override
		protected List<Post> retrieveDataInBackground(Topic... topics) throws DataRetrieverException
		{
			// Si on s'apprête à précharger une page encore jamais lu, on la note, uniquement si on est connecté et si ce n'est pas la cat modo
			boolean useFakeAccount = false;
			if (topic.getLastReadPage() != -1 && getPageNumber() > topic.getLastReadPage() && isLoggedIn() && !isModoCat(topics[0].getCategory()))
			{
				pageToBeSetToRead = getPageNumber();
				useFakeAccount = true;
			}

			return getDataRetriever().getPosts(topics[0],getPageNumber(), useFakeAccount);
		}

	}
}