package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Theme;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * <p>Activity abstraite permettant d'ajouter un topic ou un post</p>
 * 
 * @author ToYonos
 *
 */
public abstract class NewPostGenericActivity extends NewPostUIActivity
{	
	/**
	 * La fenêtre de dialog pour choisir les smilies
	 */
	private Dialog smiliesDialog; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		if (!isLoggedIn()) showLoginDialog(true);	
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		if (smiliesDialog != null) smiliesDialog.dismiss();
	}

	@Override
	protected void goBack()
	{
		finish();
	}
	
	@Override
	protected void onLogout()
	{
		loadCats(false);
	}
	
	@Override
	protected void redrawPage()
	{
		applyTheme(currentTheme);
	}

	@Override
	protected void setCancelButtonClickListener(Button cancelButton)
	{
		cancelButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();	
			}
		});
	}

	@Override
	protected ViewGroup getSmiliesLayout()
	{
		if (smiliesDialog == null)
		{
			smiliesDialog = new Dialog(this);
			smiliesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.smilies, null);
			smiliesDialog.setContentView(layout);
			applyThemeForSmilies(currentTheme);
			
			smiliesDialog.setOnDismissListener(new OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					NewPostGenericActivity.super.hideWikiSmiliesResults(layout);
				}
			});
			
			return layout;
		}
		else
		{
			return (ViewGroup) smiliesDialog.findViewById(R.id.SmiliesContainer);
		}
	}

	@Override
	protected void showWikiSmiliesResults(ViewGroup layout)
	{
		smiliesDialog.show();
	}
	
	@Override
	protected void hideWikiSmiliesResults(ViewGroup layout)
	{
		smiliesDialog.dismiss();
		super.hideWikiSmiliesResults(layout);
	}
	
	@Override
	protected void applyTheme(Theme theme)
	{
		LinearLayout root = (LinearLayout) findViewById(R.id.NewPostGenericRoot);
		root.setBackgroundColor(theme.getListBackgroundColor());
		
		applyTheme(theme, (ViewGroup) findViewById(R.id.PostContainer).getParent());
		
		if (smiliesDialog != null) applyThemeForSmilies(theme);
	}
	
	private void applyThemeForSmilies(Theme theme)
	{
		LinearLayout smilies = (LinearLayout) smiliesDialog.findViewById(R.id.SmiliesContainer);
		smilies.setBackgroundColor(theme.getListBackgroundColor());
	}
}