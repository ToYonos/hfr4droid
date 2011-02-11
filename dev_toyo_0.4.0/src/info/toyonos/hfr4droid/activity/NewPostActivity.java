package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.Topic;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * <p>Activity permettant d'ajouter un post (classique ou MP)</p>
 * 
 * @author ToYonos
 *
 */
public class NewPostActivity extends NewPostGenericActivity
{
	private Topic topic = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.new_post, null);
		removeUselessScrollView(layout);
		setContentView(layout);
		
		// TODO suite
		
		addPostButtons(layout);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		// TODO suite
		return true;
	}
	
	@Override
	protected void setTitle()
	{
		final TextView topicTitle = (TextView) findViewById(R.id.NewPostTitle);
		// TODO suite
		//topicTitle.setText(isMpsCat(cat) ? getString(R.string.new_mp) : getString(R.string.new_topic, cat.getName()));
	}
		
	@Override
	protected void setOkButtonClickListener(Button okButton)
	{
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				EditText postRecipient = (EditText) findViewById(R.id.inputMpTo);
				EditText postSubject = (EditText) findViewById(R.id.inputTopicSubject);
				EditText postContent = (EditText) findViewById(R.id.InputPostContent);
				// TODO suite
			}
		});
	}
}