package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;

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
	
	protected void removeUselessScrollView(View layout)
	{
		// Suppression de la ScrollView inutile (utilisée seulement quand new_post_content.xml est utilisé seul) 
		ScrollView sv = (ScrollView) layout.findViewById(R.id.SVPostContent);
		LinearLayout child = (LinearLayout) sv.getChildAt(0);
		TableRow parent = (TableRow) sv.getParent();
		parent.removeView(sv);
		sv.removeView(child);
		TableRow.LayoutParams tllp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		tllp.span = 3;
		child.setLayoutParams(tllp);
		parent.addView(child);
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
}