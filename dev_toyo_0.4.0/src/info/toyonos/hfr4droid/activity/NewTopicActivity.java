package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.core.message.MessageSenderException;
import info.toyonos.hfr4droid.core.message.HFRMessageSender.ResponseCode;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <p>Activity permettant d'ajouter un topic (classique ou MP)</p>
 * 
 * @author ToYonos
 *
 */
public class NewTopicActivity extends NewPostUIActivity
{
	private Category cat = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.new_post, null);
		ScrollView sv = (ScrollView) layout.findViewById(R.id.SVPostContent);
		LinearLayout child = (LinearLayout) sv.getChildAt(0);
		TableRow parent = (TableRow) sv.getParent();
		parent.removeView(sv);
		sv.removeView(child);
		TableRow.LayoutParams tllp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		tllp.span = 3;
		child.setLayoutParams(tllp);
		parent.addView(child);
		setContentView(layout);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String action = intent.getAction();
		if (bundle != null && bundle.getSerializable("cat") != null)
		{
			cat = (Category) bundle.getSerializable("cat");
		}
		else if (action.equals(Intent.ACTION_SEND))
		{
			// Envoie de données par message privé
			cat = Category.MPS_CAT;
			if (bundle.get(Intent.EXTRA_TEXT) != null)
			{
				((TextView) findViewById(R.id.InputPostContent)).setText(((String) bundle.get(Intent.EXTRA_TEXT)));
			}
			if (bundle.get(Intent.EXTRA_SUBJECT) != null)
			{
				((TextView) findViewById(R.id.inputPostSubject)).setText(((String) bundle.get(Intent.EXTRA_SUBJECT)));
			}
			if (bundle.get(Intent.EXTRA_STREAM) != null)
			{
				Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
				Intent intentRehost = new Intent(this, ImagePicker.class);
				intentRehost.setAction(ImagePicker.ACTION_HFRUPLOADER_MP);
				intentRehost.putExtra(Intent.EXTRA_STREAM, uri.toString());
				startActivityForResult(intentRehost, ImagePicker.CHOOSE_PICTURE);
			}
		}
		
		if (cat == null)
		{
			finish();
			return;
		}		
		addPostButtons(layout);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		if (!isLoggedIn()) showLoginDialog(true);	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ImagePicker.CHOOSE_PICTURE && data != null)
		{
			Bundle extras = data.getExtras();
			if (extras != null)
			{
				String url = (String) extras.get(ImagePicker.FINAL_URL);
				insertBBCode((EditText) findViewById(R.id.InputPostContent), url, "");
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.common, menu);
		inflater.inflate(R.menu.misc, menu);
		menu.removeItem(R.id.MenuMps);
		menu.removeItem(R.id.MenuRefresh);
		return true;
	}
	
	@Override
	protected void setTitle()
	{
		final TextView topicTitle = (TextView) findViewById(R.id.NewPostTitle);
		topicTitle.setText(isMpsCat(cat) ? getString(R.string.new_mp) : getString(R.string.new_topic, cat.getName()));
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
	protected void setOkButtonClickListener(Button okButton)
	{
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				final EditText postDest = (EditText) findViewById(R.id.inputMpTo);
				final EditText postSubject = (EditText) findViewById(R.id.inputPostSubject);
				final EditText postContent = (EditText) findViewById(R.id.InputPostContent);
				new ValidateMessageAsynckTask()
				{
					@Override
					protected boolean canExecute()
					{
						return postSubject.getText().length() != 0 && postContent.getText().length() != 0 && postDest.getText().length() != 0;
					}

					@Override
					protected ResponseCode validateMessage() throws MessageSenderException, DataRetrieverException
					{
						return getMessageSender().newTopic(Category.MPS_CAT, getDataRetriever().getHashCheck(), postDest.getText().toString(), postSubject.getText().toString(), postContent.getText().toString(), isSignatureEnable());
					}

					@Override
					protected boolean handleCodeResponse(ResponseCode code)
					{
						if (!super.handleCodeResponse(code))
						{
							switch (code)
							{	
								case TOPIC_NEW_OK: // New topic ok
									loadTopics(Category.MPS_CAT, TopicType.ALL, 1, false);
									return true;
									
								case MP_INVALID_RECIPIENT: // Invalid recipient
									Toast.makeText(NewTopicActivity.this, getString(R.string.mp_invalid_recipient), Toast.LENGTH_SHORT).show();
									return true;									
								
								default:
									return false;
							}							
						}
						else
						{
							return true;
						}
					}
				}.execute();
			}
		});
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
	
	/*smiliesDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
	{
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
		{
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
				if (smiliesDialog.isShowing())
				{
					hideWikiSmiliesResults(layout);
					return true;
				}
			}
			return false;
		}
	});*/

	@Override
	protected ViewGroup getSmiliesLayout()
	{
		// TODO Renvoyer le layout root de la dialog (id ?)
		return null;
	}

	@Override
	protected void showWikiSmiliesResults(ViewGroup layout)
	{
		// TODO afficher la dialog
	}
	
	@Override
	protected void hideWikiSmiliesResults(ViewGroup layout)
	{
		super.hideWikiSmiliesResults(layout);
		// TODO cacher la dialog
	}
}