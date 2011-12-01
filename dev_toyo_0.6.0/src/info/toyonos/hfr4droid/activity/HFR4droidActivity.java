package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.HFR4droidApplication;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.auth.AuthenticationException;
import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.SubCategory;
import info.toyonos.hfr4droid.core.bean.Theme;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.core.data.MDDataRetriever;
import info.toyonos.hfr4droid.core.message.HFRMessageSender;
import info.toyonos.hfr4droid.service.MpCheckService;
import info.toyonos.hfr4droid.service.MpTimerCheckService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.PullToRefreshListView;

/**
 * <p>Activity générique de l'application. Gère entre autres :
 * <ul>
 * <li>L'accès au préférences</li>
 * <li>Le chargement des données via des <code>DataRetrieverAsyncTask</code></li>
 * </ul>
 * </p>
 * 
 * @author ToYonos
 *
 */
public abstract class HFR4droidActivity extends Activity
{
	public static final String PREF_WELCOME_SCREEN			= "PrefWelcomeScreen";
	public static final String PREF_CHECK_MPS_ENABLE		= "PrefCheckMpsEnable";
	public static final String PREF_NOTIFICATION_TYPE		= "PrefNotificationType";
	public static final String PREF_TYPE_DRAPEAU			= "PrefTypeDrapeau";
	public static final String PREF_SIGNATURE_ENABLE		= "PrefSignatureEnable";
	public static final String PREF_DBLTAP_ENABLE			= "PrefDblTapEnable";
	public static final String PREF_PRELOADING_ENABLE		= "PrefPreloadingEnable";
	public static final String PREF_SWIPE					= "PrefSwipe";
	public static final String PREF_FULLSCREEN_ENABLE		= "PrefFullscreenEnable";
	public static final String PREF_THEME					= "PrefTheme";
	public static final String PREF_POLICE_SIZE				= "PrefPoliceSize";
	public static final String PREF_AVATARS_DISPLAY_TYPE	= "PrefAvatarsDisplayType";
	public static final String PREF_SMILEYS_DISPLAY_TYPE	= "PrefSmileysDisplayType";
	public static final String PREF_IMGS_DISPLAY_TYPE		= "PrefImgsDisplayType";
	public static final String PREF_SRV_MPS_ENABLE			= "PrefSrvMpsEnable";
	public static final String PREF_SRV_MPS_FREQ			= "PrefSrvMpsFreq";
	
	public static enum DrawableDisplayType
	{
		ALWAYS_SHOW(1),
		SHOW_WHEN_WIFI(2),
		NEVER_SHOW(3);
		
		private final int value;

		private DrawableDisplayType(int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return this.value;
		}
		
		public static DrawableDisplayType fromInt(int anInt) 
		{
			for (DrawableDisplayType type : DrawableDisplayType.values())
			{
				if (anInt == type.getValue()) return type;
			}
			return null;
		}
	};
	
	protected AlertDialog loginDialog;
	protected int currentPageNumber;

	protected Theme currentTheme = null;
	private int currentPoliceSize = -1;

	private PreLoadingPostsAsyncTask preLoadingPostsAsyncTask;
	private Map<Integer, List<Post>> preLoadedPosts;
	private boolean navForward;
	
	protected abstract void setTitle();

	protected void error(Exception e, boolean toast, boolean onUiThread)
	{
		error(null, e, toast, onUiThread);
	}
	
	protected void error(Exception e, boolean toast)
	{
		error(null, e, toast, false);
	}
	
	protected void error(Exception e)
	{
		error(null, e, false, false);
	}
	
	protected void error(String msg, Exception e, boolean toast, boolean onUiThread)
	{
		final String logMsg = getMessage(e, msg);
		Log.e(HFR4droidApplication.TAG, logMsg, e);

		if (toast)
		{
			if (onUiThread)
			{
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						Toast.makeText(HFR4droidActivity.this, logMsg, Toast.LENGTH_LONG).show();
					}
				});
			}
			else
			{
				Toast.makeText(HFR4droidActivity.this, logMsg, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public static String getMessage(Exception e, String msg)
	{
		StringBuilder logMsg = new StringBuilder("");

		if (msg != null) logMsg.append(msg).append(", ");
		logMsg.append(e.getClass().getSimpleName())
		.append(" : ")
		.append(e.getMessage());

		if (e.getCause() != null)
		{
			logMsg.append(" (")
			.append(e.getCause().getClass().getSimpleName())
			.append(" : ")
			.append(e.getCause().getMessage())
			.append(")");
		}
		return logMsg.toString();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		loadTheme(getThemeKey());
		currentPoliceSize = getPoliceSize();
		
		Bundle bundle = this.getIntent().getExtras();
		loginDialog = null;
		currentPageNumber = bundle != null ? bundle.getInt("pageNumber") : -1;
		preLoadingPostsAsyncTask = null;
		preLoadedPosts = new HashMap<Integer, List<Post>>();
		navForward = true;
		loginFromCache();		
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		setTitle();
		if (isFullscreenEnable())
		{
			getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,   
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else
		{
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}		
	}
	
	@Override
	protected void onRestart()
	{
		super.onRestart();
		boolean redrawPage = false;
		
		if (currentTheme.getKey() != getThemeKey())
		{
			loadTheme(getThemeKey());
			applyTheme(currentTheme);
			redrawPage = true;
		}
		
		if (currentPoliceSize != getPoliceSize())
		{
			currentPoliceSize = getPoliceSize();
			redrawPage = true;
		}
		
		if (this instanceof PostsActivity)
		{
			PostsActivity pa = (PostsActivity) this;
			if (pa.currentAvatarsDisplayType != getAvatarsDisplayType())
			{
				pa.currentAvatarsDisplayType = getAvatarsDisplayType();
				redrawPage = true;
			}
			
			if (pa.currentSmileysDisplayType != getSmileysDisplayType())
			{
				pa.currentSmileysDisplayType = getSmileysDisplayType();
				redrawPage = true;
			}
			
			if (pa.currentImgsDisplayType != getImgsDisplayType())
			{
				pa.currentImgsDisplayType = getImgsDisplayType();
				redrawPage = true;
			}
		}
		
		if (redrawPage) redrawPage();
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (preLoadingPostsAsyncTask != null) preLoadingPostsAsyncTask.cancel(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			SlidingDrawer navDrawer = (SlidingDrawer) findViewById(R.id.Nav);
			if (navDrawer != null && navDrawer.isOpened())
			{
				navDrawer.close();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem loginLogout = menu.findItem(R.id.MenuLoginLogout);
		if (loginLogout != null)
		{
			boolean isLoggedIn = isLoggedIn();
			loginLogout.setTitle(isLoggedIn ? R.string.menu_logout : R.string.menu_login);

			MenuItem mps = menu.findItem(R.id.MenuMps);
			if (mps != null)
			{
				mps.setVisible(isLoggedIn());
				mps.setEnabled(isLoggedIn());
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{	        
			case R.id.MenuPrefs :
				Intent intent = new Intent(HFR4droidActivity.this, HFR4droidPrefs.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
				startActivity(intent);
				return true;
	
			case R.id.MenuMps :
				loadTopics(Category.MPS_CAT, TopicType.ALL, 1, false);
				return true; 
	
			case R.id.MenuLoginLogout :
				setTitle();
				if (!isLoggedIn())
				{
					showLoginDialog();
				}
				else
				{
					logout();
					stopMpTimerCheckService();
					onLogout();
				}
				return true;
	
			case R.id.MenuNavRefresh:
				reloadPage();
				return true;
	
			case R.id.MenuNavFirstPage:
				loadFirstPage();
				return true;
	
			case R.id.MenuNavPreviousPage :
				loadPreviousPage();
				return true;
	
			case R.id.MenuNavUserPage :
				loadUserPage();
				return true;    		        	
	
			case R.id.MenuNavNextPage :
				loadNextPage();
				return true;
	
			case R.id.MenuNavLastPage :
				loadLastPage();
				return true;
	
			case R.id.MenuRefresh :
				reloadPage();
				return true;
	
			case R.id.MenuBack :
				goBack();
				return true;		    	
	
			default:
				return super.onOptionsItemSelected(item);
		}		    	
	}

	protected abstract void applyTheme(Theme theme);

	@SuppressWarnings("rawtypes")
	protected int getKeyByTheme(String themeKey, Class type, String key)
	{
		try
		{
			return type.getField(themeKey + "_" + key).getInt(null);
		}
		catch (Exception e)
		{
			error(e);
			return -1;
		}
	}

	private int getColorByKey(String themeKey, String key)
	{
		int colorKey = getKeyByTheme(themeKey, R.color.class, key);
		return getResources().getColor(colorKey);
	}
	
	private void loadTheme(String themeKey)
	{
		currentTheme = new Theme(themeKey);
		currentTheme.setListBackgroundColor(getColorByKey(themeKey, "list_background"));
		currentTheme.setListDividerColor(getColorByKey(themeKey, "list_divider"));
		currentTheme.setPostHeaderData(getString(getKeyByTheme(themeKey, R.string.class, "post_header_data"))); 
		currentTheme.setPostPseudoColor(getColorByKey(themeKey, "post_pseudo"));
		currentTheme.setPostDateColor(getColorByKey(themeKey, "post_date"));
		currentTheme.setPostTextColor(getColorByKey(themeKey, "text1"));
		currentTheme.setPostLinkColor(getColorByKey(themeKey, "post_link"));
		currentTheme.setPostEditQuoteBackgroundColor(getColorByKey(themeKey, "post_edit_quote_background"));
		currentTheme.setPostEditQuoteTextColor(getColorByKey(themeKey, "post_edit_quote_text"));
		currentTheme.setPostBlockBackgroundColor(getColorByKey(themeKey, "post_block_background"));
		currentTheme.setModoPostBackgroundColor(getColorByKey(themeKey, "post_modo_background"));
		currentTheme.setProgressBarInversed(Boolean.parseBoolean(getString(getKeyByTheme(themeKey, R.string.class, "inverse_progress"))));
		currentTheme.setSplashTitleColor(getColorByKey(themeKey, "splash_title"));
	}

	protected HFR4droidApplication getHFR4droidApplication()
	{
		return (HFR4droidApplication) getApplication();
	}

	protected MDDataRetriever getDataRetriever()
	{
		return getHFR4droidApplication().getDataRetriever();
	}

	protected HFRMessageSender getMessageSender()
	{
		return ((HFR4droidApplication)getApplication()).getMessageSender();
	}

	protected boolean login(String user, String password) throws AuthenticationException
	{
		return getHFR4droidApplication().login(user, password);
	}

	protected boolean login() throws AuthenticationException
	{
		return getHFR4droidApplication().login();
	}

	protected void logout()
	{
		getHFR4droidApplication().logout();
	}

	protected boolean isLoggedIn()
	{
		return getHFR4droidApplication().isLoggedIn();
	}

	protected void loginFromCache()
	{
		if (!isLoggedIn() && this.getFileStreamPath(HFRAuthentication.COOKIES_FILE_NAME).exists())
		{
			try
			{
				login();
			}
			catch (final AuthenticationException e)
			{
				error(e, true);
			}
		}
	}

	protected void startMpTimerCheckService()
	{
		if (isLoggedIn() && isSrvMpEnable())
		{
			startService(new Intent(this, MpTimerCheckService.class));
		}
	}
	
	protected void startMpCheckService()
	{
		if (isLoggedIn() && isCheckMpsEnable())
		{
			startService(new Intent(this, MpCheckService.class));
		}
	}

	protected void stopMpTimerCheckService()
	{
		Intent intent = new Intent(this, MpTimerCheckService.class); 
		stopService(intent);
	}

	protected void clearNotifications()
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(MpTimerCheckService.NOTIFICATION_ID);
	}

	protected boolean isMpsCat(Category cat)
	{
		return Category.MPS_CAT.getId() == cat.getId();
	}

	protected boolean isAllCatsCat(Category cat)
	{
		return Category.ALL_CATS.getId() == cat.getId();
	}

	protected boolean isModoCat(Category cat)
	{
		return Category.MODO_CAT.getId() == cat.getId();
	}

	public void setPageNumber(int pageNumber)
	{
		currentPageNumber = pageNumber;
	}

	protected void showLoginDialog()
	{
		showLoginDialog(false);
	}
	
	protected void showLoginDialog(final boolean forceLogin)
	{
		if (loginDialog == null)
		{
			Context mContext = getApplicationContext();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.login, null);

			AlertDialog.Builder builder = new AlertDialog.Builder(HFR4droidActivity.this);
			builder.setTitle(getString(R.string.login_title)); 
			builder.setView(layout);
			final EditText user = (EditText) layout.findViewById(R.id.inputUser);
			final EditText pass = (EditText) layout.findViewById(R.id.inputPass);

			builder.setPositiveButton(getString(R.string.button_ok), new OnClickListener()
			{  
				public void onClick(DialogInterface dialog, int whichButton)
				{
					final ProgressDialog progressDialog = new ProgressDialog(HFR4droidActivity.this);
					progressDialog.setMessage(getString(R.string.login_loading));
					progressDialog.setIndeterminate(true);
					new AsyncTask<Void, Void, Boolean>()
					{
						@Override
						protected void onPreExecute() 
						{
							progressDialog.show();
						}

						@Override
						protected Boolean doInBackground(Void... params)
						{
							Boolean isLoggedIn = null;
							try
							{
								isLoggedIn = login(user.getText().toString(), pass.getText().toString());
							}
							catch (AuthenticationException e)
							{
								error(e, true, true);
							}
							return isLoggedIn;
						}

						@Override
						protected void onPostExecute(Boolean isLoggedIn)
						{
							if (isLoggedIn != null)
							{
								if (isLoggedIn)
								{
									startMpTimerCheckService();
									reloadPage();
								}
								else
								{
									Toast.makeText(HFR4droidActivity.this, getString(R.string.wrong_login_or_password), Toast.LENGTH_SHORT).show();	
								}
							}
							if ((isLoggedIn == null || !isLoggedIn) && forceLogin) showLoginDialog(true);
							progressDialog.dismiss();
						}
					}.execute();
				}
			});

			builder.setNegativeButton(getString(R.string.button_cancel), new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					if (forceLogin) finish();
				}
			});

			loginDialog = builder.create(); 
			
			if (forceLogin)
			{
				loginDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
				{
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
					{
						if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
						{
							return true;
						}
						return false;
					}
				});
			}
		}
		loginDialog.show();
	}

	protected void loadCats()
	{
		loadCats(true);
	}

	protected DataRetrieverAsyncTask<Category, Void> loadCats(final boolean sameActivity)
	{
		return loadCats(sameActivity, true);
	}
	
	protected DataRetrieverAsyncTask<Category, Void> loadCats(final boolean sameActivity, boolean displayLoading)
	{
		String progressTitle = getString(R.string.hfr);
		String progressContent = getString(R.string.getting_cats);
		String noElement = getString(R.string.no_cat);

		DataRetrieverAsyncTask<Category, Void> task = new DataRetrieverAsyncTask<Category, Void>()
		{	
			@Override
			protected List<Category> retrieveDataInBackground(Void... params) throws DataRetrieverException
			{
				return getDataRetriever().getCats();
			}

			@Override
			protected void onPostExecuteSameActivity(List<Category> cats) throws ClassCastException
			{
				CategoriesActivity activity = (CategoriesActivity) HFR4droidActivity.this;
				activity.refreshCats(cats);
			}

			@Override
			protected void onPostExecuteOtherActivity(List<Category> cats)
			{
				Intent intent = new Intent(HFR4droidActivity.this, CategoriesActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Bundle bundle = new Bundle();
				bundle.putSerializable("cats", new ArrayList<Category>(cats));
				intent.putExtras(bundle);
				startActivity(intent);
				if (HFR4droidActivity.this instanceof NewPostGenericActivity || HFR4droidActivity.this instanceof SplashActivity) finish();
			}
		};
		
		task.execute(progressTitle, progressContent, noElement, sameActivity, displayLoading);
		return task;
		
	}

	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(Category cat, final TopicType type)
	{
		return loadTopics(cat, type, 1, true);
	}

	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(Category cat, final TopicType type, final boolean sameActivity)
	{
		return loadTopics(cat, type, 1, sameActivity);
	}

	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(Category cat, final TopicType type, final int pageNumber)
	{
		return loadTopics(cat, type, pageNumber, true);
	}
	
	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(final Category cat, final TopicType type, final int pageNumber, final boolean sameActivity)
	{
		return loadTopics(cat, type, pageNumber, sameActivity, true);
	}

	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(final Category cat, final TopicType type, final int pageNumber, final boolean sameActivity, boolean displayLoading)
	{
		String progressTitle = cat.toString();
		String progressContent = type != TopicType.ALL ? getString("getting_topics_" + type.getKey() + "s")
				: (isMpsCat(cat) ? getString(R.string.getting_mps, pageNumber) : getString(R.string.getting_topics, pageNumber));
		String noElement = getString(R.string.no_topic);

		DataRetrieverAsyncTask<Topic, Category> task = new DataRetrieverAsyncTask<Topic, Category>()
		{	
			@Override
			protected List<Topic> retrieveDataInBackground(Category... cats) throws DataRetrieverException
			{
				// On charge les sous-cats pour les mettre en cache
				getDataRetriever().getSubCats(cats[0]);
				return getDataRetriever().getTopics(cats[0], type, pageNumber);
			}

			@Override
			protected void onPostExecuteSameActivity(List<Topic> topics) throws ClassCastException
			{
				TopicsActivity activity = (TopicsActivity) HFR4droidActivity.this;
				activity.setPageNumber(pageNumber);
				activity.refreshTopics(topics);
				setTitle();
				((PullToRefreshListView) activity.getListView()).onRefreshComplete();
			}

			@Override
			protected void onPostExecuteOtherActivity(List<Topic> topics)
			{
				// On passe par CategoriesActivity pour garder une navigation cohérente si on l'on vient de SplashActivity
				Class<?> dest = HFR4droidActivity.this instanceof SplashActivity ? CategoriesActivity.class : TopicsActivity.class;

				Intent intent = new Intent(HFR4droidActivity.this, dest);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Bundle bundle = new Bundle();
				bundle.putSerializable("topics", new ArrayList<Topic>(topics));
				bundle.putInt("pageNumber", pageNumber);
				if (cat.equals(Category.ALL_CATS)) bundle.putBoolean("allCats", true);
				if (type != null) bundle.putSerializable("topicType", type);
				intent.putExtras(bundle);
				startActivity(intent);
				
				if (HFR4droidActivity.this instanceof NewPostGenericActivity || HFR4droidActivity.this instanceof SplashActivity) finish();
			}

			@Override
			protected void onPostExecuteNoItem(boolean sameActivity, Toast t)
			{
				super.onPostExecuteNoItem(sameActivity, t);
				if (HFR4droidActivity.this instanceof SplashActivity && !sameActivity)
				{
					loadCats(false, false);
				}
				else if (HFR4droidActivity.this instanceof TopicsActivity && sameActivity && !(cat instanceof SubCategory))
				{
					TopicsActivity ta = (TopicsActivity) HFR4droidActivity.this;
					if (ta.getType() == TopicType.ALL)
					{
						loadCats(false);
					}
				}
				else if (HFR4droidActivity.this instanceof PostsActivity && !sameActivity)
				{
					loadCats(false);	
				}
			}
		};
		
		task.execute(progressTitle, progressContent, noElement, sameActivity, displayLoading, cat);
		return task;
	}

	protected void loadPosts(Topic topic, final int pageNumber)
	{
		loadPosts(topic, pageNumber, true);
	}

	protected void loadPosts(final Topic topic, final int pageNumber, final boolean sameActivity)
	{
		String progressTitle = topic.toString();
		String progressContent = topic.getNbPages() != -1 ?
		getString(R.string.getting_posts, pageNumber, topic.getNbPages()) :
		getString(R.string.getting_posts_simple, pageNumber, topic.getNbPages());
		String noElement = getString(R.string.no_post);
		
		new DataRetrieverAsyncTask<Post, Topic>()
		{			
			@Override
			protected List<Post> retrieveDataInBackground(Topic... topics) throws DataRetrieverException
			{
				List<Post> posts = new ArrayList<Post>();
				if (preLoadingPostsAsyncTask != null && preLoadingPostsAsyncTask.getPageNumber() == pageNumber)
				{
					switch (preLoadingPostsAsyncTask.getStatus()) 
					{
						case RUNNING:
							Log.d(HFR4droidApplication.TAG, "Page " + pageNumber + " deja en cours de recuperation...");
							try
							{
								posts = preLoadingPostsAsyncTask.waitAndGet();
								Log.d(HFR4droidApplication.TAG, "...page " + pageNumber + " recuperee !");
							}
							catch (Exception e)
							{
								preLoadingPostsAsyncTask = null;
							}
							break;

						case FINISHED:
							List<Post> tmpPosts = HFR4droidActivity.this.preLoadedPosts.get(pageNumber);
							if ( preLoadedPosts != null)
							{
								Log.d(HFR4droidApplication.TAG, "Page " + pageNumber + " recuperee dans le cache.");
								posts = tmpPosts;
								preLoadedPosts.clear(); // On ne conserve pas le cache des posts
							}
							else
							{
								posts = getDataRetriever().getPosts(topics[0], pageNumber);
							}
							break;
						
						default:
							break;
					}
				}
				else
				{	
					if (preLoadingPostsAsyncTask != null) preLoadingPostsAsyncTask.cancel(true);
					posts = getDataRetriever().getPosts(topics[0], pageNumber);
				}
				return posts;
			}

			@Override
			protected void onPostExecuteSameActivity(List<Post> posts) throws ClassCastException
			{
				PostsActivity activity = (PostsActivity) HFR4droidActivity.this;
				activity.setPosts(posts);
				navForward = currentPageNumber <= pageNumber;
				activity.setPageNumber(pageNumber);
				setTitle();
				activity.refreshPosts(posts);
				if (isPreloadingEnable()) preLoadPosts(topic, pageNumber);
			}

			@Override
			protected void onPostExecuteOtherActivity(List<Post> posts)
			{
				Intent intent = new Intent(HFR4droidActivity.this, PostsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
				Bundle bundle = new Bundle();
				bundle.putSerializable("posts", new ArrayList<Post>(posts));
				bundle.putInt("pageNumber", pageNumber);
				if (HFR4droidActivity.this instanceof TopicsActivity)
				{
					bundle.putSerializable("fromTopicType", ((TopicsActivity) HFR4droidActivity.this).getType());
					bundle.putBoolean("fromAllCats", ((TopicsActivity) HFR4droidActivity.this).isAllCatsCat());
				}
				else if (HFR4droidActivity.this instanceof HFR4droidDispatcher)
				{
					bundle.putSerializable("fromTopicType", ((HFR4droidDispatcher) HFR4droidActivity.this).getUrlParser().getType());	
				}
				else if (HFR4droidActivity.this instanceof NewPostActivity)
				{
					bundle.putSerializable("fromTopicType", ((NewPostActivity) HFR4droidActivity.this).getFromType());
					bundle.putBoolean("fromAllCats", ((NewPostActivity) HFR4droidActivity.this).isFromAllCats());
				}
				intent.putExtras(bundle);
				startActivity(intent);
				if (HFR4droidActivity.this instanceof NewPostGenericActivity) finish();
			}
		}.execute(progressTitle, progressContent, noElement, sameActivity, topic);
	}

	protected void preLoadPosts(final Topic topic, final int currentPageNumber)
	{
		final int tmpPageNumber;
		if (currentPageNumber == 1)
		{
			tmpPageNumber = 2;
		}
		else if (currentPageNumber == topic.getNbPages())
		{
			tmpPageNumber = topic.getNbPages() - 1;
		}
		else
		{
			tmpPageNumber = navForward ? currentPageNumber + 1 : currentPageNumber - 1;
		}
		final int targetPageNumber = tmpPageNumber;

		preLoadingPostsAsyncTask = new PreLoadingPostsAsyncTask(targetPageNumber);
		preLoadingPostsAsyncTask.execute(topic);
	}
		
	protected void loadFirstPage(){}

	protected void loadPreviousPage(){}

	protected void loadUserPage(){}

	protected void loadNextPage(){}

	protected void loadLastPage(){}

	protected void reloadPage(){}
	
	protected void redrawPage(){}

	protected void goBack(){}
	
	protected void onLogout()
	{
		reloadPage();
	}

	// Getter des préférences modifiables par l'utilisateur

	protected int getWelcomeScreen()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return Integer.parseInt(settings.getString(PREF_WELCOME_SCREEN, getString(R.string.pref_welcome_screen_default)));
	}

	protected boolean isCheckMpsEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_CHECK_MPS_ENABLE, Boolean.parseBoolean(getString(R.string.pref_check_mps_enable_default)));
	}
	
	protected int getTypeDrapeau()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return Integer.parseInt(settings.getString(PREF_TYPE_DRAPEAU, getString(R.string.pref_type_drapeau_default)));
	}

	protected boolean isSignatureEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_SIGNATURE_ENABLE, Boolean.parseBoolean(getString(R.string.pref_signature_enable_default)));
	}

	protected boolean isDblTapEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_DBLTAP_ENABLE, Boolean.parseBoolean(getString(R.string.pref_dbltap_enable_default)));
	}

	protected boolean isPreloadingEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_PRELOADING_ENABLE, Boolean.parseBoolean(getString(R.string.pref_preloading_enable_default)));
	}	

	protected int getSwipe()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return Integer.parseInt(settings.getString(PREF_SWIPE, getString(R.string.pref_swipe_default)));
	}
	
	protected boolean isFullscreenEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_FULLSCREEN_ENABLE, Boolean.parseBoolean(getString(R.string.pref_fullscreen_enable_default)));
	}
	
	protected String getThemeKey()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getString(PREF_THEME, getString(R.string.pref_theme_default));
	}
	
	protected int getPoliceSize()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return Integer.parseInt(settings.getString(PREF_POLICE_SIZE, getString(R.string.pref_police_size_default)));
	}

	protected DrawableDisplayType getAvatarsDisplayType()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(HFR4droidActivity.PREF_AVATARS_DISPLAY_TYPE, getString(R.string.pref_avatars_display_type_default));
		return DrawableDisplayType.fromInt(Integer.parseInt(value));
	}

	protected DrawableDisplayType getSmileysDisplayType()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(HFR4droidActivity.PREF_SMILEYS_DISPLAY_TYPE, getString(R.string.pref_smileys_display_type_default));
		return DrawableDisplayType.fromInt(Integer.parseInt(value));
	}

	protected DrawableDisplayType getImgsDisplayType()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String value = settings.getString(HFR4droidActivity.PREF_IMGS_DISPLAY_TYPE, getString(R.string.pref_imgs_display_type_default));
		return DrawableDisplayType.fromInt(Integer.parseInt(value));
	}

	protected boolean isSrvMpEnable()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(PREF_SRV_MPS_ENABLE, Boolean.parseBoolean(getString(R.string.pref_srv_mps_enable_default)));
	}

	protected String getString(String keyStr, Object... params)
	{
		int key = -1;
		try
		{
			key = R.string.class.getField(keyStr).getInt(null);
		}
		catch (Exception e){} // On ne fait rien, une RuntimeException se produira au getString
		return getString(key, params);
	}

	protected int getTextSize(int normalSize)
	{
		int newSize;
		switch (getPoliceSize())
		{
			case 2:
				newSize = (int)Math.round(normalSize * 1.2);
				break;
				
			case 3:
				newSize = (int)Math.round(normalSize * 1.4);
				break;				
	
			default:
				newSize = normalSize;
				break;
		}
		return newSize;
	}
	
	protected String getVersionName()
	{
		PackageInfo packageInfo;
		try
		{
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return packageInfo.versionName;
		}
		catch (NameNotFoundException e)
		{
			return "?";
		}
	}
	
	/* Classes internes */

	protected abstract class DataRetrieverAsyncTask<E, P> extends AsyncTask<P, Void, List<E>>
	{
		private ProgressDialog progressDialog;
		private boolean sameActivity;
		private boolean displayLoading;
		private String noElementMsg;

		protected abstract List<E> retrieveDataInBackground(P... params) throws DataRetrieverException;

		protected abstract void onPostExecuteSameActivity(List<E> elements) throws ClassCastException;

		protected abstract void onPostExecuteOtherActivity(List<E> elements);

		protected void onPostExecuteNoItem(boolean sameActivity, Toast t)
		{
			t.show();
		}

		public void execute(final String progressTitle, final String progressContent, final String noElementMsg, final boolean sameActivity, P... params)
		{
			execute(progressTitle, progressContent, noElementMsg, sameActivity, true, params);
		}
		
		public void execute(final String progressTitle, final String progressContent, final String noElementMsg, final boolean sameActivity, final boolean displayLoading, P... params)
		{
			progressDialog = new ProgressDialog(HFR4droidActivity.this);
			progressDialog.setTitle(progressTitle != null ? progressTitle : getString(R.string.loading));
			progressDialog.setMessage(progressContent);
			progressDialog.setIndeterminate(true);
			this.noElementMsg = noElementMsg;
			this.sameActivity = sameActivity;
			this.displayLoading = displayLoading;
			execute(params);
		}

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
			if (displayLoading) progressDialog.show();
		}

		@Override
		protected List<E> doInBackground(final P... params)
		{
			List<E> elements = null;
			try
			{
				elements = retrieveDataInBackground(params);
			}
			catch (DataRetrieverException e)
			{
				error(e, true, true);
			}
			return elements;
		}

		@Override
		protected void onPostExecute(final List<E> elements)
		{
			if (elements != null)
			{
				if (elements.size() > 0)					
				{
					if (sameActivity)
					{
						try
						{
							onPostExecuteSameActivity(elements);
						}
						catch (ClassCastException e)
						{
							error(e);
							throw new RuntimeException(e);
						}
					}
					else
					{
						onPostExecuteOtherActivity(elements);
					}
				}
				else
				{
					final Toast t = Toast.makeText(HFR4droidActivity.this, noElementMsg, Toast.LENGTH_SHORT);
					onPostExecuteNoItem(sameActivity, t);
				}
			}
			if (displayLoading) progressDialog.dismiss();				
		}
	}

	private class PreLoadingPostsAsyncTask extends DataRetrieverAsyncTask<Post, Topic>
	{
		private int targetPageNumber;
		private boolean isFinished;
		
		public PreLoadingPostsAsyncTask(int targetPageNumber)
		{
			this.targetPageNumber = targetPageNumber;
			isFinished = false;
		}
		
		public int getPageNumber()
		{
			return targetPageNumber;
		}
		
		public List<Post> waitAndGet() throws InterruptedException, ExecutionException
		{
			isFinished = true;
			return get();
		}

		@Override
		protected List<Post> retrieveDataInBackground(Topic... topics) throws DataRetrieverException
		{
			return getDataRetriever().getPosts(topics[0], targetPageNumber);
		}

		@Override
		protected void onPreExecute() {}
		
		@Override
		protected void onPostExecute(List<Post> elements)
		{
			if (!isFinished)
			{
				preLoadedPosts.put(targetPageNumber, elements);
				Log.d(HFR4droidApplication.TAG, "Page " + targetPageNumber + " chargee avec succes !");
			}
		}

		@Override
		protected void onPostExecuteSameActivity(List<Post> posts) throws ClassCastException {}

		@Override
		protected void onPostExecuteOtherActivity(List<Post> posts) {}
	}

	protected abstract class PageNumberDialog
	{
		protected AlertDialog dialog;
		protected int currentPage;
		protected int pageMax;

		public PageNumberDialog(int currentPage)
		{
			dialog = null;
			this.currentPage = currentPage;
			this.pageMax = -1;
		}

		public PageNumberDialog(int currentPage, int pageMax)
		{
			dialog = null;
			this.currentPage = currentPage;
			this.pageMax = pageMax;
		}

		protected abstract void onValidate(int pageNumber);

		protected int getPageNumber(EditText input)
		{
			String value = input.getText().toString();
			int pageNumber = 1;
			try
			{
				pageNumber = Integer.parseInt(value);
				if (pageMax != -1 && pageNumber > pageMax) pageNumber = pageMax;
				if (pageNumber < 1) pageNumber = 1;
			}
			catch (NumberFormatException e)
			{
				return -1;
			}
			return pageNumber;
		}
		
		public void show()
		{
			if (dialog == null)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(HFR4droidActivity.this);                 
				builder.setTitle(getString(R.string.nav_page_number));  
				
				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.goto_page_number, null);
				builder.setView(layout);

				final TextView infos = (TextView) layout.findViewById(R.id.CurrentPageInfos);
				infos.setText(pageMax != -1 ? (
				currentPage != -1 ?
						getString(R.string.nav_user_page_label1, currentPage, pageMax) :
						getString(R.string.nav_user_page_label2, pageMax)) :
				getString(R.string.nav_user_page_label3, currentPage));
				final EditText input = (EditText) layout.findViewById(R.id.PageNumber);
				builder.setPositiveButton(getString(R.string.button_ok), new OnClickListener()
				{  
					public void onClick(DialogInterface dialog, int whichButton)
					{  
						int pageNumber = getPageNumber(input);
						if (pageNumber != -1) onValidate(pageNumber);       
					}  
				});
				builder.setNegativeButton(getString(R.string.button_cancel), new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which){}
				});

				dialog = builder.create();
				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
			dialog.show();
		}
	}

	protected abstract class SimpleNavOnGestureListener extends SimpleOnGestureListener
	{
		private int getVelocity()
		{
			int swipeMinVelocity;
			switch (HFR4droidActivity.this.getSwipe())
			{
				case 2:
					swipeMinVelocity = 350;
					break;
					
				case 3:
					swipeMinVelocity = 200;
					break;					
	
				default:
					swipeMinVelocity = 500;
					break;			
			}
			return swipeMinVelocity;
		}
		
		private float getWidthPercent()
		{
			float widthPercent;
			switch (HFR4droidActivity.this.getSwipe())
			{
				case 2:
					widthPercent = (float)0.6;
					break;
					
				case 3:
					widthPercent = (float)0.4;
					break;					
	
				default:
					widthPercent = (float)0.75;
					break;			
			}
			return widthPercent;
		}
		
		protected abstract void onLeftToRight(MotionEvent e1, MotionEvent e2);

		protected abstract void onRightToLeft(MotionEvent e1, MotionEvent e2);
		
		public abstract boolean onDoubleTap(MotionEvent e);

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			if (Math.abs(velocityX) > getVelocity() && Math.abs(e1.getX() - e2.getX()) > (metrics.widthPixels * getWidthPercent()))
			{
				if (e1.getX() < e2.getX())
				{
					onLeftToRight(e1, e2);
				} 
				else if (e1.getX() > e2.getX())
				{
					onRightToLeft(e1, e2);
				}
				return true;
			}
			return false;
		}
	}
}