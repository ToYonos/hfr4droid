package info.toyonos.hfr4droid.common.util.helper;

import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.common.activity.ImagePicker;
import info.toyonos.hfr4droid.common.core.bean.Theme;
import info.toyonos.hfr4droid.common.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.common.util.asynctask.ProgressDialogAsyncTask;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * <p>Helper abstrait proposant des méthodes pour l'interface d'ajout / modification d'un post / topic</p>
 * 
 * @author ToYonos
 *
 */
public abstract class NewPostUIHelper
{
	private static final String SMILEY_KEY		= "smiley";
	private static final String BOLD_KEY		= "bold";
	private static final String ITALIC_KEY		= "italic";
	private static final String UNDERLINE_KEY	= "underline";
	private static final String STRIKE_KEY		= "strike";
	private static final String QUOTE_KEY		= "quote";
	private static final String FIXED_KEY		= "fixed";
	private static final String CODE_KEY		= "code";
	private static final String URL_KEY			= "url";
	private static final String IMG_KEY			= "img";
	private static final String PUCE_KEY		= "puce";
	private static final String SPOILER_KEY		= "spoiler";

	public static final long BOTTOM_PAGE_ID		= 999999999999999L;
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ImagePicker.CHOOSE_PICTURE && data != null)
		{
			Bundle extras = data.getExtras();
			if (extras != null)
			{
				String url = (String) extras.get(ImagePicker.FINAL_URL);
				onRehostOk(url);
			}
		}
	}
	
	protected abstract void onRehostOk(String url);
	
	public String fixHTML(String htmlContent)
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

	public Button getHfrRehostButton(final HFR4droidActivity context)
	{
		Button hfrRehost = new Button(context);
		hfrRehost.setTextSize(20);
		hfrRehost.setLines(1);
		hfrRehost.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		hfrRehost.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(context, ImagePicker.class);
				intent.setAction(ImagePicker.ACTION_HFRUPLOADER);
				context.startActivityForResult(intent, ImagePicker.CHOOSE_PICTURE);
			}
		});

		return hfrRehost;
	}
	
	public ImageButton getSmileyButton(HFR4droidActivity context, final ViewGroup layout)
	{
		ImageButton smiley = new ImageButton(context);
		smiley.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
		smiley.setImageResource(R.drawable.redface);
		smiley.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				layout.findViewById(R.id.Toolbar).setVisibility(View.GONE);
				layout.findViewById(R.id.SmileySearch).setVisibility(View.VISIBLE);
			}
		});

		return smiley;
	}

	public void addPostButtons(final HFR4droidActivity context, final ViewGroup layout)
	{
		LinearLayout ll = (LinearLayout) layout.findViewById(R.id.FormatButtons);
		ll.addView(getSmileyButton(context, layout));
		ll.addView(new FormatButton(context, layout, SMILEY_KEY));
		ll.addView(new FormatButton(context, layout, BOLD_KEY));
		ll.addView(new FormatButton(context, layout, ITALIC_KEY));
		ll.addView(new FormatButton(context, layout, UNDERLINE_KEY));
		ll.addView(new FormatButton(context, layout, STRIKE_KEY));
		ll.addView(new FormatButton(context, layout, QUOTE_KEY));
		ll.addView(new FormatButton(context, layout, FIXED_KEY));
		ll.addView(new FormatButton(context, layout, CODE_KEY));
		ll.addView(new FormatButton(context, layout, URL_KEY));
		ll.addView(new FormatButton(context, layout, IMG_KEY));
		ll.addView(getHfrRehostButton(context));
		ll.addView(new FormatButton(context, layout, PUCE_KEY));
		ll.addView(new FormatButton(context, layout, SPOILER_KEY));

		Button wikiButton = (Button) layout.findViewById(R.id.ButtonWikiSmilies);
		wikiButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				final EditText smileyTag = (EditText) layout.findViewById(R.id.InputSmileyTag);
				if (smileyTag.getText().length() == 0) return;

				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((EditText) layout.findViewById(R.id.InputSmileyTag)).getWindowToken(), 0);

				new ProgressDialogAsyncTask<Void, Void, String>(context)
				{
					protected WebView getWebView(final ViewGroup layout, final ViewGroup smiliesLayout, String smiliesData)
					{
						final WebView webView = new WebView(context);
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
								context.runOnUiThread(new Runnable()
								{
									public void run()
									{
										insertBBCode((EditText) layout.findViewById(R.id.InputPostContent), " " + smiley + " ", "");
										hideWikiSmiliesResults(smiliesLayout);
										hideWikiSmiliesSearch(layout);
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
									TextView smiliesLoading = new TextView(context);
									smiliesLoading.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
									smiliesLoading.setTextColor(Color.BLACK);
									float scale = context.getResources().getDisplayMetrics().density;
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

						webView.loadDataWithBaseURL("", "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" + fixHTML(js.toString()) + fixHTML(css.toString()) + "</head><body>" + fixHTML(smiliesData.toString()) + "</body></html>", "text/html", "UTF-8", null);
						return webView;
					}
					
					@Override
					protected void onPreExecute() 
					{
						super.onPreExecute();
						progressDialog.setMessage(context.getString(R.string.getting_smilies));
						progressDialog.show();
					}

					@Override
					protected String doInBackground(Void... params)
					{
						setThreadId();
						String data = "";
						try
						{
							data = context.getDataRetriever().getSmiliesByTag(smileyTag.getText().toString());
						}
						catch (DataRetrieverException e)
						{
							data = null;
							context.error(e, true, true);
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
	}
	
	public abstract ViewGroup getSmiliesLayout();
	
	protected abstract void showWikiSmiliesResults(ViewGroup layout);
	
	public abstract void hideWikiSmiliesResults(ViewGroup layout);
	
	public void destroyWikiSmiliesResults(ViewGroup layout)
	{
		if (layout == null) return;
		View webView = layout.getChildAt(0);
		if (webView != null && webView instanceof WebView)
		{
			layout.removeView(webView);
			((WebView) webView).destroy();
		}
		View lastChild = layout.getChildAt(layout.getChildCount() - 1);
		if (lastChild instanceof TextView)
		{
			layout.removeView(lastChild);
		}
	}

	public void hideWikiSmiliesSearch(ViewGroup layout)
	{
		layout.findViewById(R.id.Toolbar).setVisibility(View.VISIBLE);
		layout.findViewById(R.id.SmileySearch).setVisibility(View.GONE);
	}

	protected abstract void setOkButtonClickListener(Button okButton);

	public void insertBBCode(EditText editText, String left, String right)
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
	
	public void applyTheme(Theme theme, ViewGroup rootLayout)
	{
		rootLayout.setBackgroundColor(theme.getListBackgroundColor());
		
		TextView labelSmileyTag = (TextView) rootLayout.findViewById(R.id.LabelSmileyTag);
		labelSmileyTag.setTextColor(theme.getPostTextColor());
		
		EditText inputSmileyTag = (EditText) rootLayout.findViewById(R.id.InputSmileyTag);
		inputSmileyTag.setTextColor(theme.getPostTextColor());

		Button buttonWikiSmilies = (Button) rootLayout.findViewById(R.id.ButtonWikiSmilies);
		buttonWikiSmilies.setTextColor(theme.getPostTextColor());
		
		EditText inputPostContent = (EditText) rootLayout.findViewById(R.id.InputPostContent);
		inputPostContent.setTextColor(theme.getPostTextColor());
		
		Button buttonOkAddPost = (Button) rootLayout.findViewById(R.id.ButtonOkAddPost);
		buttonOkAddPost.setTextColor(theme.getPostTextColor());
		
		LinearLayout ll = (LinearLayout) rootLayout.findViewById(R.id.FormatButtons);
		for (int i = 0; i < ll.getChildCount(); i++)
		{
			View v = ll.getChildAt(i);
			if (v instanceof FormatButton)
			{
				((FormatButton) v).setTextColor(theme.getPostTextColor());
			}
			else if (v instanceof Button) // Hfrrehost
			{
				((Button) v).setText(Html.fromHtml("<font color=\"#477DBF\">" + rootLayout.getContext().getString(R.string.button_post_hfr_rehost_left) + "</font><font color=\"" + theme.getPostTextColorAsString() + "\">" + rootLayout.getContext().getString(R.string.button_post_hfr_rehost_right) + "</font>"));
			}
		}
	}

	/* Classes internes */
	
	private class FormatButton extends Button
	{
		public FormatButton(Context context)
		{
			super(context);
		}

		public FormatButton(HFR4droidActivity context, final View layout, String key)
		{
			super(context);
			final String left = context.getString("button_post_" + key.toLowerCase() + "_left");
			final String right = context.getString("button_post_" + key.toLowerCase() + "_right");
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