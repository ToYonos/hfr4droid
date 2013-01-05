package info.toyonos.hfr4droid.common.util.dialog;

import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.activity.HFR4droidActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public abstract class PageNumberDialog
{
	private HFR4droidActivity context;
	protected AlertDialog dialog;
	protected int currentPage;
	protected int pageMax;

	public PageNumberDialog(HFR4droidActivity context, int currentPage)
	{
		this.context = context;
		dialog = null;
		this.currentPage = currentPage;
		this.pageMax = -1;
	}

	public PageNumberDialog(HFR4droidActivity context, int currentPage, int pageMax)
	{
		this(context, currentPage);
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
			AlertDialog.Builder builder = new AlertDialog.Builder(context);                 
			builder.setTitle(context.getString(R.string.nav_page_number));  
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.goto_page_number, null);
			builder.setView(layout);

			final TextView infos = (TextView) layout.findViewById(R.id.CurrentPageInfos);
			infos.setText(pageMax != -1 ? (
			currentPage != -1 ?
					context.getString(R.string.nav_user_page_label1, currentPage, pageMax) :
						context.getString(R.string.nav_user_page_label2, pageMax)) :
			context.getString(R.string.nav_user_page_label3, currentPage));
			final EditText input = (EditText) layout.findViewById(R.id.PageNumber);
			builder.setPositiveButton(context.getString(R.string.button_ok), new OnClickListener()
			{  
				public void onClick(DialogInterface dialog, int whichButton)
				{  
					int pageNumber = getPageNumber(input);
					if (pageNumber != -1) onValidate(pageNumber);       
				}  
			});
			builder.setNegativeButton(context.getString(R.string.button_cancel), new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which){}
			});

			dialog = builder.create();
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
		dialog.show();
	}
}