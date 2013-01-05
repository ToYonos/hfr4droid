package info.toyonos.hfr4droid.donate.activity;

import info.toyonos.hfr4droid.common.core.bean.Theme;
import info.toyonos.hfr4droid.donate.R;
import android.widget.TextView;

public class SplashActivity extends info.toyonos.hfr4droid.common.activity.SplashActivity
{
	@Override
	protected void applyTheme(Theme theme)
	{
		super.applyTheme(theme);
		TextView thankYou = (TextView) findViewById(R.id.ThankYou);
		thankYou.setTextColor(theme.getSplashTitleColor());
	}
}