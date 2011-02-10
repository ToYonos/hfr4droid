package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.HFR4droidException;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.core.message.MessageSenderException;
import info.toyonos.hfr4droid.core.message.HFRMessageSender.ResponseCode;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

	protected long postId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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

	protected void addPostButtons(final ViewGroup layout)
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
					protected WebView getWebView(final ViewGroup layout, final ViewGroup smiliesLayout, String smiliesData)
					{
						final WebView webView = new WebView(NewPostUIActivity.this);
						webView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
						webView.setBackgroundColor(0);
						webView.setVisibility(View.GONE); 
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
										hideWikiSmiliesResults(smiliesLayout);
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
								View lastChild = smiliesLayout.getChildAt(smiliesLayout.getChildCount() - 1);
								if (progress > 0 && progressDialog.isShowing())
								{
									progressDialog.dismiss();
									TextView smiliesLoading = new TextView(NewPostUIActivity.this);
									smiliesLoading.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
									smiliesLoading.setTextColor(Color.BLACK);
									float scale = getResources().getDisplayMetrics().density;
									float dip = 20;
									int pixel = (int) (dip * scale + 0.5f);
									smiliesLoading.setPadding(pixel, pixel, pixel, pixel);
									smiliesLoading.setText(R.string.smilies_loading);
									smiliesLayout.addView(smiliesLoading);
									showWikiSmiliesResults(smiliesLayout);
								}
								else if (progress > 15 && lastChild instanceof TextView)
								{
									smiliesLayout.removeView(lastChild);
									webView.setVisibility(View.VISIBLE);
								}
							}
						});

						webView.loadData("<html><head>" + fixHTML(js.toString()) + fixHTML(css.toString()) + "</head><body>" + fixHTML(smiliesData.toString()) + "</body></html>", "text/html", "UTF-8");
						return webView;
					}
					
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
						catch (DataRetrieverException e)
						{
							data = null;
							error(e, true, true);
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
						
						ViewGroup smiliesLayout = getSmiliesLayout();
						WebView webView = getWebView(layout, smiliesLayout, data);
						smiliesLayout.addView(webView, 0);
					}
				}.execute();
			}
		});

		Button okButton = (Button) layout.findViewById(R.id.ButtonOkAddPost);
		setOkButtonClickListener(okButton);

		Button cancelButton = (Button) layout.findViewById(R.id.ButtonCancelAddPost);
		setCancelButtonClickListener(cancelButton);
	}
	
	protected abstract ViewGroup getSmiliesLayout();
	
	protected abstract void showWikiSmiliesResults(ViewGroup layout);
	
	protected void hideWikiSmiliesResults(ViewGroup layout)
	{
		WebView webView = (WebView) layout.getChildAt(0);
		layout.removeView(webView);
		webView.destroy();
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

	protected abstract class ValidateMessageAsynckTask extends AsyncTask<Void, Void, ResponseCode>
	{
		private ProgressDialog progressDialog;
		
		public ValidateMessageAsynckTask()
		{
			progressDialog = new ProgressDialog(NewPostUIActivity.this);
			progressDialog.setMessage(getString(R.string.post_loading));
			progressDialog.setIndeterminate(true);
		}
		
		protected abstract boolean canExecute(); 
		
		protected abstract ResponseCode validateMessage() throws MessageSenderException, DataRetrieverException;
		
		@Override
		protected void onPreExecute() 
		{
			if (canExecute())
			{
				progressDialog.show();
			}
			else
			{
				cancel(true);
			}
		}

		@Override
		protected ResponseCode doInBackground(Void... params)
		{
			ResponseCode code = ResponseCode.POST_KO_EXCEPTION;
			try
			{
				code = validateMessage();
			}
			catch (HFR4droidException e) // MessageSenderException, DataRetrieverException
			{
				error(e, true, true);
			}
			return code;
		}

		@Override
		protected void onPostExecute(ResponseCode code)
		{
			handleCodeResponse(code);
			progressDialog.dismiss();
		}

		protected boolean handleCodeResponse(ResponseCode code)
		{
			switch (code)
			{
				case POST_KO: // Undefined error
					Toast.makeText(NewPostUIActivity.this, getString("post_" + (postId != -1 ? "edit" : "add") + "_failed"), Toast.LENGTH_SHORT).show();
					return true;

				case POST_FLOOD: // Flood // TODO ajout autre flood
					Toast.makeText(NewPostUIActivity.this, getString(R.string.post_flood), Toast.LENGTH_SHORT).show();
					return true;
					
				case POST_MDP_KO: // Wrong password
					Toast.makeText(NewPostUIActivity.this, getString(R.string.post_wrong_password), Toast.LENGTH_SHORT).show();
					return true;
					
				default:
					return false;
					
			}
		}		
	}
	
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