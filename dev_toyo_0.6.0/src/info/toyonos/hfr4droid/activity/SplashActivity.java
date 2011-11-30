package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Theme;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class SplashActivity extends HFR4droidActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		RotateAnimation anim = new RotateAnimation(0f, 350f, 16f, 16f);
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
							loadTopics(Category.ALL_CATS, TopicType.fromInt(welcomeScreen), 1, false, false);
						}
						else
						{
							loadCats(false, false);
						}
					}
				});
			}
		}).start();

		
	}
	
	@Override
	protected void setTitle() {}

	@Override
	protected void applyTheme(Theme theme) {}
}
