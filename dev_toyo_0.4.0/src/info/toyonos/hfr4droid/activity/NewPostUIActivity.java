package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <p>Activity abstraite proposant des méthodes pour l'interface d'ajout / modification d'un post</p>
 * 
 * @author ToYonos
 *
 */
public abstract class NewPostUIActivity extends HFR4droidActivity
{
	private static final String SMILEY_KEY		= "smiley";
	private static final String BOLD_KEY		= "bold";
	private static final String ITALIC_KEY		= "italic";
	private static final String UNDERLINE_KEY	= "underline";
	private static final String STRIKE_KEY		= "strike";
	private static final String FIXED_KEY		= "fixed";
	private static final String CODE_KEY		= "code";
	private static final String URL_KEY			= "url";
	private static final String IMG_KEY			= "img";
	private static final String PUCE_KEY		= "puce";
	private static final String SPOILER_KEY		= "spoiler";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// TODO quelque chose ?
	}
	
	protected String fixHTML(String htmlContent)
	{
		int len = htmlContent.length();
		StringBuilder buf = new StringBuilder(len + 100);
		for (int i = 0; i < len; i++)
		{
			char chr = htmlContent.charAt(i);
			switch (chr)
			{
				case '%':
					buf.append("%25");
					break;
				case '\'':
					buf.append("%27");
					break;
				case '#':
					buf.append("%23");
					break;
				default:
					buf.append(chr);
			}
		}
		return buf.toString();
	}

	protected Button getHfrRehostButton()
	{
		Button hfrRehost = new Button(NewPostUIActivity.this);
		hfrRehost.setTextSize(20);
		hfrRehost.setLines(1);
		hfrRehost.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		hfrRehost.setText(Html.fromHtml("<font color=\"#477DBF\">" + getString(R.string.button_post_hfr_rehost_left) + "</font><font color=\"black\">" + getString(R.string.button_post_hfr_rehost_right) + "</font>"));

		hfrRehost.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(NewPostUIActivity.this, ImagePicker.class);
				intent.setAction(ImagePicker.ACTION_HFRUPLOADER);
				startActivityForResult(intent, ImagePicker.CHOOSE_PICTURE);
			}
		});

		return hfrRehost;
	}

	protected void addPostDialogButtons(final View layout)
	{
		LinearLayout ll = (LinearLayout) layout.findViewById(R.id.FormatButtons);
		ll.addView(new FormatButton(layout, SMILEY_KEY));
		ll.addView(new FormatButton(layout, BOLD_KEY));
		ll.addView(new FormatButton(layout, ITALIC_KEY));
		ll.addView(new FormatButton(layout, UNDERLINE_KEY));
		ll.addView(new FormatButton(layout, STRIKE_KEY));
		ll.addView(new FormatButton(layout, FIXED_KEY));
		ll.addView(new FormatButton(layout, CODE_KEY));
		ll.addView(new FormatButton(layout, URL_KEY));
		ll.addView(new FormatButton(layout, IMG_KEY));
		ll.addView(getHfrRehostButton());
		ll.addView(new FormatButton(layout, PUCE_KEY));
		ll.addView(new FormatButton(layout, SPOILER_KEY));

		Button wikiButton = (Button) layout.findViewById(R.id.ButtonWikiSmilies);
		wikiButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				final EditText smileyTag = (EditText) layout.findViewById(R.id.inputSmileyTag);
				if (smileyTag.getText().length() == 0) return;

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((EditText) layout.findViewById(R.id.inputSmileyTag)).getWindowToken(), 0);

				final ProgressDialog progressDialog = new ProgressDialog(NewPostUIActivity.this);
				progressDialog.setMessage(getString(R.string.getting_smilies));
				progressDialog.setIndeterminate(true);
				new AsyncTask<Void, Void, String>()
				{
					@Override
					protected void onPreExecute() 
					{
						progressDialog.show();
					}

					@Override
					protected String doInBackground(Void... params)
					{
						String data = "";
						try
						{
							data = getDataRetriever().getSmiliesByTag(smileyTag.getText().toString());
						}
						catch (final Exception e)
						{
							data = null;
							Log.e(NewPostUIActivity.this.getClass().getSimpleName(), String.format(getString(R.string.error), e.getClass().getName(), e.getMessage()));
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									Toast t = Toast.makeText(NewPostUIActivity.this, getString(R.string.error_retrieve_data, e.getClass().getSimpleName(), e.getMessage()), Toast.LENGTH_LONG);
									t.show();
								}
							});
						}
						return data;
					}

					@Override
					protected void onPostExecute(String data)
					{
						if (data == null)
						{
							progressDialog.dismiss();
							return;
						}
						final LinearLayout smiliesContainer = ((LinearLayout) layout.findViewById(R.id.SmiliesContainer));
						final TextView smiliesLoading = ((TextView) layout.findViewById(R.id.SmiliesLoading));
						final TableLayout postContainer = ((TableLayout) layout.findViewById(R.id.PostContainer));
						WebView oldWebView = (WebView) smiliesContainer.getChildAt(0);
						final WebView webView = new WebView(NewPostUIActivity.this);
						TableRow.LayoutParams tllp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						tllp.span = 3;
						webView.setLayoutParams(tllp);
						webView.setBackgroundColor(0); 
						WebSettings settings = webView.getSettings();
						settings.setDefaultTextEncodingName("UTF-8");
						settings.setJavaScriptEnabled(true);
						webView.addJavascriptInterface(new Object()
						{
							@SuppressWarnings("unused")
							public void addSmiley(final String smiley)
							{
								runOnUiThread(new Runnable()
								{
									public void run()
									{
										insertBBCode((EditText) layout.findViewById(R.id.InputPostContent), " " + smiley + " ", "");
										smiliesContainer.setVisibility(View.GONE);
										postContainer.setVisibility(View.VISIBLE);
									}
								});
							}
						}, "HFR4Droid");

						StringBuffer css = new StringBuffer("<style type=\"text/css\">");
						css.append("img { margin: 5px }");
						css.append("</style>");
						StringBuffer js = new StringBuffer("<script type=\"text/javascript\">");
						js.append("function putSmiley(code, src) { window.HFR4Droid.addSmiley(code); }");
						js.append("</script>");

						webView.setWebChromeClient(new WebChromeClient()
						{
							public void onProgressChanged(WebView view, int progress)
							{
								if (progress > 0 && progressDialog.isShowing())
								{
									progressDialog.dismiss();
									smiliesLoading.setVisibility(View.VISIBLE);
									postContainer.setVisibility(View.GONE);
								}
								else if (progress > 15 && smiliesLoading.getVisibility() == View.VISIBLE)
								{
									smiliesLoading.setVisibility(View.GONE);
									smiliesContainer.setVisibility(View.VISIBLE);
								}
							}
						});

						webView.loadData("<html><head>" + fixHTML(js.toString()) + fixHTML(css.toString()) + "</head><body>" + fixHTML(data.toString()) + "</body></html>", "text/html", "UTF-8");

						if (oldWebView != null)
						{
							oldWebView.destroy();
							smiliesContainer.removeView(oldWebView);
						}
						smiliesContainer.addView(webView);
					}
				}.execute();
			}
		});

		Button okButton = (Button) layout.findViewById(R.id.ButtonOkAddPost);
		setOkButtonClickListener(okButton);

		Button cancelButton = (Button) layout.findViewById(R.id.ButtonCancelAddPost);
		setCancelButtonClickListener(cancelButton);
	}
	
	protected abstract void setOkButtonClickListener(Button okButton);
	
	protected abstract void setCancelButtonClickListener(Button cancelButton);

	protected void insertBBCode(EditText editText, String left, String right)
	{
		if (editText.getSelectionStart() != -1)
		{
			int firstPos = Math.min(editText.getSelectionStart(), editText.getSelectionEnd());
			int secondPos = Math.max(editText.getSelectionStart(), editText.getSelectionEnd()) + left.length();

			editText.setText(editText.getText().toString().subSequence(0, firstPos) + left + editText.getText().toString().substring(firstPos));
			editText.setText(editText.getText().toString().subSequence(0, secondPos) + right + editText.getText().toString().substring(secondPos));

			editText.setSelection(firstPos + left.length(), secondPos);
			editText.requestFocus();
		}
	}

	/* Classes internes */

	protected class FormatButton extends Button
	{
		public FormatButton(Context context)
		{
			super(context);
		}

		public FormatButton(final View layout, String key)
		{
			super(NewPostUIActivity.this);
			final String left = getString("button_post_" + key.toLowerCase() + "_left");
			final String right = getString("button_post_" + key.toLowerCase() + "_right");
			setTextSize(20);
			setLines(1);
			setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			setText(left);

			this.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					EditText postContent = (EditText) layout.findViewById(R.id.InputPostContent);
					if (postContent.getSelectionStart() != postContent.getSelectionEnd())
					{
						insertBBCode(postContent, left, right);
					}
					else
					{
						String currentTag = ((FormatButton) v).getText().toString();
						insertBBCode(postContent, currentTag, "");
						if (!right.equals("")) setText(currentTag.equals(left) ? right : left);
					}
				}
			});	
		}
	}
}