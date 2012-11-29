package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Theme;
import info.toyonos.hfr4droid.util.helper.NewPostUIHelper;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * <p>Activity abstraite permettant d'ajouter un topic ou un post</p>
 * 
 * @author ToYonos
 *
 */
public abstract class NewPostGenericActivity extends HFR4droidActivity
{	
	/**
	 * La fenêtre de dialog pour choisir les smilies
	 */
	private Dialog smiliesDialog; 
	
	/**
	 * le helper pour gérer le comportement de l'interface
	 */
	protected NewPostUIHelper uiHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		uiHelper = getNewPostUIHelper();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			ViewGroup layout = (ViewGroup) findViewById(R.id.PostContainer);
			if (layout.findViewById(R.id.SmileySearch).getVisibility() == View.VISIBLE)
			{
				uiHelper.hideWikiSmiliesSearch(layout);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
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
	protected void applyTheme(Theme theme)
	{
		LinearLayout root = (LinearLayout) findViewById(R.id.NewPostGenericRoot);
		root.setBackgroundColor(theme.getListBackgroundColor());
		
		uiHelper.applyTheme(theme, (ViewGroup) findViewById(R.id.PostContainer).getParent());
		
		if (smiliesDialog != null) applyThemeForSmilies(theme);
	}
	
	private void applyThemeForSmilies(Theme theme)
	{
		LinearLayout smilies = (LinearLayout) smiliesDialog.findViewById(R.id.SmiliesContainer);
		smilies.setBackgroundColor(theme.getListBackgroundColor());
	}
	
	protected abstract void setOkButtonClickListener(Button okButton);
	
	private NewPostUIHelper getNewPostUIHelper()
	{
		return new NewPostUIHelper()
		{
			@Override
			protected void showWikiSmiliesResults(ViewGroup layout)
			{
				smiliesDialog.show();
			}
			
			@Override
			public void hideWikiSmiliesResults(ViewGroup layout)
			{
				smiliesDialog.dismiss();
				destroyWikiSmiliesResults(layout);
			}

			@Override
			protected void setOkButtonClickListener(Button okButton)
			{
				NewPostGenericActivity.this.setOkButtonClickListener(okButton);
			}
			
			@Override
			protected void onRehostOk(String url)
			{
				insertBBCode((EditText) findViewById(R.id.InputPostContent), url, "");
			}

			
			@Override
			public ViewGroup getSmiliesLayout()
			{
				if (smiliesDialog == null)
				{
					smiliesDialog = new Dialog(NewPostGenericActivity.this);
					smiliesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
					final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.smilies, null);
					smiliesDialog.setContentView(layout);
					applyThemeForSmilies(currentTheme);
					
					smiliesDialog.setOnDismissListener(new OnDismissListener()
					{
						public void onDismiss(DialogInterface dialog)
						{
							destroyWikiSmiliesResults(layout);
						}
					});
					
					return layout;
				}
				else
				{
					return (ViewGroup) smiliesDialog.findViewById(R.id.SmiliesContainer);
				}
			}
		};
	}
}