package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Theme;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.util.asynctask.DataRetrieverAsyncTask;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends HFR4droidActivity
{
	private DataRetrieverAsyncTask<?, ?> task = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		applyTheme(currentTheme);
		
		RotateAnimation anim = new RotateAnimation(0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(700);

		ImageView splash = (ImageView) findViewById(R.id.SplashAnimation);
		splash.startAnimation(anim);
		
		new Thread(new Runnable()
		{	
			public void run()
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					error(e, true);
				}
				runOnUiThread(new Runnable()
				{	
					public void run()
					{
						int welcomeScreen = getWelcomeScreen();
						if (welcomeScreen > 0 && isLoggedIn())
						{
							task = loadTopics(Category.ALL_CATS, TopicType.fromInt(welcomeScreen), 1, false, false);
						}
						else
						{
							task = loadCats(false, false);
						}
					}
				});
			}
		}).start();

		
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		updateLogo();
	}

	@Override
	public void onConfigurationChanged(Configuration conf)
	{
		super.onConfigurationChanged(conf);
		updateLogo();
	}

	private void updateLogo()
	{
		Display display = getWindowManager().getDefaultDisplay();
		boolean landscape = display.getWidth() > display.getHeight();
		ImageView logo = (ImageView) findViewById(R.id.SplashLogo);
		logo.setBackgroundResource(landscape ? R.drawable.logo_medium : R.drawable.logo_big);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (task != null) task.cancel(true);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void setTitle()
	{
		TextView title = (TextView) findViewById(R.id.VersionAndAuthor);
		title.setText(getString(R.string.splash_title, getVersionName()));
	}

	@Override
	protected void applyTheme(Theme theme)
	{
		LinearLayout container = (LinearLayout) findViewById(R.id.SplashContainer);
		container.setBackgroundColor(theme.getListBackgroundColor());
		
		TextView title = (TextView) findViewById(R.id.VersionAndAuthor);
		title.setTextColor(theme.getSplashTitleColor());
	}
}