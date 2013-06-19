package info.toyonos.hfr4droid.donate.activity;

import info.toyonos.hfr4droid.common.core.bean.Theme;
import info.toyonos.hfr4droid.donate.R;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends info.toyonos.hfr4droid.common.activity.SplashActivity
{
	@Override
	protected void applyTheme(Theme theme)
	{
		FrameLayout container = (FrameLayout) findViewById(R.id.SplashContainer);
		container.setBackgroundColor(theme.getListBackgroundColor());
		
		TextView title = (TextView) findViewById(R.id.VersionAndAuthor);
		title.setTextColor(theme.getSplashTitleColor());
		
		TextView thankYou = (TextView) findViewById(R.id.ThankYou);
		thankYou.setTextColor(theme.getSplashTitleColor());
	
		ImageView premium = (ImageView) findViewById(R.id.Premium);
		premium.setImageResource(theme.getKey().equals("dark") ? R.drawable.premium_dark : R.drawable.premium);
	}

	@Override
	protected void updateLogo()
	{
		super.updateLogo();

		ImageView glasses = (ImageView) findViewById(R.id.SplashLogoGlasses);
		if (getPoliceSize() > 2)
		{
			Display display = getWindowManager().getDefaultDisplay();
			boolean landscape = display.getWidth() > display.getHeight();

			glasses.setVisibility(View.VISIBLE);
			glasses.setImageResource(landscape ? R.drawable.glasses_medium : R.drawable.glasses_big);
			glasses.setPadding(0, landscape ? 30 : 70, 0, 0);
		}
		else
		{
			glasses.setVisibility(View.GONE);
		}
	}
}