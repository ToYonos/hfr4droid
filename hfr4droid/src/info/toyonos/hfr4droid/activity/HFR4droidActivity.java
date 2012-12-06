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
import info.toyonos.hfr4droid.util.asynctask.PreLoadingAsyncTask.PreLoadingCompleteListener;
import info.toyonos.hfr4droid.util.asynctask.ProgressDialogAsyncTask;
import info.toyonos.hfr4droid.util.asynctask.DataRetrieverAsyncTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SlidingDrawer;
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
	
	protected static boolean keepNavigationHistory = false;
	
	protected AlertDialog loginDialog;
	protected int currentPageNumber;

	protected Theme currentTheme = null;
	private int currentPoliceSize = -1;
	
	protected abstract void setTitle();

	public void error(Exception e, boolean toast, boolean onUiThread)
	{
		error(null, e, toast, onUiThread);
	}
	
	public void error(Exception e, boolean toast)
	{
		error(null, e, toast, false);
	}
	
	public void error(Exception e)
	{
		error(null, e, false, false);
	}
	
	public void error(String msg, Exception e, boolean toast, boolean onUiThread)
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
		keepNavigationHistory = false;
		
		Bundle bundle = this.getIntent().getExtras();
		loginDialog = null;
		currentPageNumber = bundle != null ? bundle.getInt("pageNumber") : -1;
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
		
		if (this instanceof TopicsActivity)
		{
			TopicsActivity ta = (TopicsActivity) this;
			if (ta.moreTopicInfos != isTopicMoreInfosEnable())
			{
				ta.moreTopicInfos = isTopicMoreInfosEnable();
				redrawPage = true;
			}
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
		
		if (this instanceof HFR4droidMultiListActivity<?>)
		{
			HFR4droidMultiListActivity<?> mla = (HFR4droidMultiListActivity<?>) this;
			if (mla.getSpace().getSwipeSensibility() != getSwipe())
			{
				mla.getSpace().setSwipeSensibility(getSwipe());
			}
		}
		
		if (redrawPage) redrawPage();
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		getHFR4droidApplication().getHttpClientHelper().closeExpiredConnections();
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
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
					getConfirmDialog(
					getString(R.string.logout_title),
					getString(R.string.are_u_sure_message),
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface arg0, int arg1)
						{
							logout();
							stopMpTimerCheckService();
							onLogout();
						}
					}).show();
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
		currentTheme.setPostHeaderColor(getColorByKey(themeKey, "post_header"));
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
		currentTheme.setSpinner(getString(getKeyByTheme(themeKey, R.string.class, "spinner")));
		currentTheme.setPostLoading(getString(getKeyByTheme(themeKey, R.string.class, "post_loading")));
		currentTheme.setProfileSpinner(getString(getKeyByTheme(themeKey, R.string.class, "profile_spinner")));
		currentTheme.setProfileText1Color(getColorByKey(themeKey, "profile_text1"));
		currentTheme.setProfileText2Color(getColorByKey(themeKey, "profile_text2"));
	}

	public HFR4droidApplication getHFR4droidApplication()
	{
		return (HFR4droidApplication) getApplication();
	}

	public MDDataRetriever getDataRetriever()
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
					new ProgressDialogAsyncTask<Void, Void, Boolean>(HFR4droidActivity.this)
					{
						@Override
						protected void onPreExecute() 
						{
							super.onPreExecute();
							progressDialog.setMessage(getString(R.string.login_loading));
							progressDialog.show();
						}

						@Override
						protected Boolean doInBackground(Void... params)
						{
							setThreadId();
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

	protected DataRetrieverAsyncTask<Category, Void> loadCats(boolean sameActivity)
	{
		return loadCats(sameActivity, true);
	}
	
	protected DataRetrieverAsyncTask<Category, Void> loadCats(boolean sameActivity, boolean displayLoading)
	{
		String progressTitle = getString(R.string.hfr);
		String progressContent = getString(R.string.getting_cats);
		String noElement = getString(R.string.no_cat);

		DataRetrieverAsyncTask<Category, Void> task = new DataRetrieverAsyncTask<Category, Void>(this)
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
			
			@Override
			protected void onError(final Exception e)
			{
				//super.onError(e);
				if (HFR4droidActivity.this instanceof SplashActivity)
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							new AlertDialog.Builder(HFR4droidActivity.this)
							.setTitle(R.string.error_splash_title)
							.setMessage(getMessage(e, null))
							.setOnCancelListener(new OnCancelListener()
							{
								public void onCancel(DialogInterface dialog)
								{
									finish();
								}
							})
							.setPositiveButton(R.string.error_splash_retry,  new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which) 
								{
									((SplashActivity) HFR4droidActivity.this).run();
								}
							})
							.setNegativeButton(R.string.error_splash_cancel, new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which) 
								{
									finish();
								}
							}).show();
						}
					});
				}
			}
		};
		
		task.execute(progressTitle, progressContent, noElement, sameActivity, displayLoading);
		return task;
		
	}

	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(Category cat, TopicType type)
	{
		return loadTopics(cat, type, 1, true);
	}

	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(Category cat, TopicType type, boolean sameActivity)
	{
		return loadTopics(cat, type, 1, sameActivity);
	}

	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(Category cat, TopicType type, int pageNumber)
	{
		return loadTopics(cat, type, pageNumber, true);
	}
	
	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(Category cat, TopicType type, int pageNumber, boolean sameActivity)
	{
		return loadTopics(cat, type, pageNumber, sameActivity, true);
	}

	protected DataRetrieverAsyncTask<Topic, Category> loadTopics(final Category cat, final TopicType type, int pageNumber, boolean sameActivity, boolean displayLoading)
	{
		String progressTitle = cat.toString();
		String progressContent = type != TopicType.ALL ? getString("getting_topics_" + type.getKey() + "s")
				: (isMpsCat(cat) ? getString(R.string.getting_mps, pageNumber) : getString(R.string.getting_topics, pageNumber));
		String noElement = getString(R.string.no_topic);

		DataRetrieverAsyncTask<Topic, Category> task = new DataRetrieverAsyncTask<Topic, Category>(this)
		{
			@Override
			protected void onCancelled()
			{
				super.onCancelled();
				if (HFR4droidActivity.this instanceof TopicsActivity)
				{
					rollbackAction((TopicsActivity) HFR4droidActivity.this);
				}
			}

			@Override
			protected List<Topic> retrieveDataInBackground(Category... cats) throws DataRetrieverException
			{
				// On charge les sous-cats pour les mettre en cache
				getDataRetriever().getSubCats(cats[0]);
				return getDataRetriever().getTopics(cats[0], type, getPageNumber());
			}

			@Override
			protected void onPostExecuteSameActivity(List<Topic> topics) throws ClassCastException
			{
				TopicsActivity activity = (TopicsActivity) HFR4droidActivity.this;
				if (currentPageNumber != getPageNumber())
				{
					activity.setPageNumber(getPageNumber());
					activity.preloadTopics();
				}
				else
				{
					activity.setPageNumber(getPageNumber());
				}
				setTitle();
				activity.refreshTopics(topics);
				PullToRefreshListView list = ((PullToRefreshListView) activity.getListView());
				if (list != null) list.onRefreshComplete();
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
				bundle.putInt("pageNumber", getPageNumber());
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
				else if (HFR4droidActivity.this instanceof TopicsActivity && sameActivity)
				{
					TopicsActivity ta = (TopicsActivity) HFR4droidActivity.this;
					if (ta.getListView() != null) ((PullToRefreshListView) ta.getListView()).onRefreshComplete();
					if (ta.getType() != TopicType.ALL && !(cat instanceof SubCategory))
					{
						loadCats(false);
					}
					else 
					{
						// Pas d'élément, on rollback et on revient à la précédente cat/sous-cat ou type de topic
						rollbackAction(ta);
					}
				}
				else if (HFR4droidActivity.this instanceof PostsActivity && !sameActivity)
				{
					loadCats(false);	
				}
			}
			
			@Override
			protected void onError(Exception e)
			{
				super.onError(e);
				if (HFR4droidActivity.this instanceof SplashActivity) finish();
			}
		};
		
		task.execute(progressTitle, progressContent, noElement, sameActivity, pageNumber, displayLoading, cat);
		return task;
	}
	
	private void rollbackAction(TopicsActivity ta)
	{
		if (ta.getPreviousCat() != null)
		{
			ta.setCat(ta.getPreviousCat());
			ta.setPreviousCat(null);
		}

		if (ta.getPreviousType() != null)
		{
			ta.setType(ta.getPreviousType());
			ta.setPreviousType(null);
		}
	}

	protected void loadPosts(Topic topic, int pageNumber)
	{
		loadPosts(topic, pageNumber, true);
	}

	protected void loadPosts(final Topic topic, int pageNumber, boolean sameActivity)
	{
		String progressTitle = topic.getName() != null ? topic.toString() : null;
		String progressContent = topic.getNbPages() != -1 ?
		getString(R.string.getting_posts, pageNumber, topic.getNbPages()) :
		getString(R.string.getting_posts_simple, pageNumber, topic.getNbPages());
		String noElement = getString(R.string.no_post);
		

		new DataRetrieverAsyncTask<Post, Topic>(this)
		{			
			@Override
			protected List<Post> retrieveDataInBackground(Topic... topics) throws DataRetrieverException
			{
				return getDataRetriever().getPosts(topics[0], getPageNumber());
			}

			@Override
			protected void onPostExecuteSameActivity(List<Post> posts) throws ClassCastException
			{
				PostsActivity activity = (PostsActivity) HFR4droidActivity.this;
				activity.setPosts(posts);
				if (currentPageNumber != getPageNumber())
				{
					activity.setPageNumber(getPageNumber());
					activity.preloadPosts();
				}
				else
				{
					activity.setPageNumber(getPageNumber());
					if (currentPageNumber < activity.topic.getNbPages()) // Le topic a pris une page le temps qu'on post
					{
						Log.d(HFR4droidApplication.TAG, "New page during the new post publication, force preloading");
						activity.preloadPosts();
					}
				}
				setTitle();
				activity.displayPosts(posts);
			}

			@Override
			protected void onPostExecuteOtherActivity(List<Post> posts)
			{
				Intent intent = new Intent(HFR4droidActivity.this, PostsActivity.class);
				if (!keepNavigationHistory) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Bundle bundle = new Bundle();
				bundle.putSerializable("posts", new ArrayList<Post>(posts));
				bundle.putInt("pageNumber", getPageNumber());
				if (HFR4droidActivity.this instanceof TopicsActivity)
				{
					bundle.putSerializable("fromTopicType", ((TopicsActivity) HFR4droidActivity.this).getType());
					bundle.putBoolean("fromAllCats", ((TopicsActivity) HFR4droidActivity.this).isAllCatsCat());
				}
				else if (HFR4droidActivity.this instanceof PostsActivity || HFR4droidActivity.this instanceof PostsSearchActivity)
				{
					bundle.putSerializable("fromTopicType", ((PostsActivity) HFR4droidActivity.this).getFromType());
					bundle.putBoolean("fromAllCats", ((PostsActivity) HFR4droidActivity.this).isFromAllCats());
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
		}.execute(progressTitle, progressContent, noElement, sameActivity, pageNumber, topic);
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
	
	public int getCurrentPageNumber()
	{
		return currentPageNumber;
	}

	// Getter des préférences modifiables par l'utilisateur

	public int getWelcomeScreen()
	{
		return getHFR4droidApplication().getWelcomeScreen();
	}

	public boolean isCheckMpsEnable()
	{
		return getHFR4droidApplication().isCheckMpsEnable();
	}
	
	public int getTypeDrapeau()
	{
		return getHFR4droidApplication().getTypeDrapeau();
	}

	public boolean isSignatureEnable()
	{
		return getHFR4droidApplication().isSignatureEnable();
	}

	public boolean isDblTapEnable()
	{
		return getHFR4droidApplication().isDblTapEnable();
	}
	
	public float getSwipe()
	{
		return getHFR4droidApplication().getSwipe();
	}
	
	public PreLoadingCompleteListener getPreloadingCallback()
	{
		return getHFR4droidApplication().getPreloadingCallback();
	}
	
	public boolean isFullscreenEnable()
	{
		return getHFR4droidApplication().isFullscreenEnable();
	}
	
	public String getThemeKey()
	{
		return getHFR4droidApplication().getThemeKey();
	}
	
	public int getPoliceSize()
	{
		return getHFR4droidApplication().getPoliceSize();
	}
	
	public boolean isTopicMoreInfosEnable()
	{
		return getHFR4droidApplication().isTopicMoreInfosEnable();
	}

	public DrawableDisplayType getAvatarsDisplayType()
	{
		return getHFR4droidApplication().getAvatarsDisplayType();
	}

	public DrawableDisplayType getSmileysDisplayType()
	{
		return getHFR4droidApplication().getSmileysDisplayType();
	}

	public DrawableDisplayType getImgsDisplayType()
	{
		return getHFR4droidApplication().getImgsDisplayType();
	}

	public boolean isSrvMpEnable()
	{
		return getHFR4droidApplication().isSrvMpEnable();
	}

	public String getString(String keyStr, Object... params)
	{
		int key = -1;
		try
		{
			key = R.string.class.getField(keyStr).getInt(null);
		}
		catch (Exception e){} // On ne fait rien, une RuntimeException se produira au getString
		return getString(key, params);
	}
	
	public int getDrawableKey(String keyStr)
	{
		int key = -1;
		try
		{
			key = R.drawable.class.getField(keyStr).getInt(null);
		}
		catch (Exception e){} // On ne fait rien, une RuntimeException se produira au getString
		return key;
	}

	protected int getTextSize(int normalSize)
	{
		int newSize;
		switch (getPoliceSize())
		{
			case 2:
				newSize = (int)Math.round(normalSize * 1.25);
				break;
				
			case 3:
				newSize = (int)Math.round(normalSize * 1.5);
				break;
				
			case 4:
				newSize = (int)Math.round(normalSize * 1.75);
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
	
	protected AlertDialog getConfirmDialog(String title, String message, DialogInterface.OnClickListener listener)
	{
		return new AlertDialog.Builder(HFR4droidActivity.this)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(R.string.button_yes, listener)
		.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) {}
		}).create();
	}
	
	protected String formatDate(SimpleDateFormat todaySdf, SimpleDateFormat dateHeure, Date date)
	{
		SimpleDateFormat check = new SimpleDateFormat("ddMMyyyy");
		boolean today = check.format(new Date()).equals(check.format(date));
		return today ? todaySdf.format(date) : dateHeure.format(date);
	}
}