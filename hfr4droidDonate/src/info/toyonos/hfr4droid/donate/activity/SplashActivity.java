package info.toyonos.hfr4droid.donate.activity;

import info.toyonos.hfr4droid.donate.R;
import info.toyonos.hfr4droid.lib.core.bean.Theme;
import android.widget.TextView;

public class SplashActivity extends info.toyonos.hfr4droid.lib.activity.SplashActivity
{
	@Override
	protected void applyTheme(Theme theme)
	{
		super.applyTheme(theme);
		TextView thankYou = (TextView) findViewById(R.id.ThankYou);
		thankYou.setTextColor(theme.getSplashTitleColor());
	}
}