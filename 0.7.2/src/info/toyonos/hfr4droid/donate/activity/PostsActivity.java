package info.toyonos.hfr4droid.donate.activity;

import android.widget.EditText;
import android.widget.Toast;
import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.core.message.HFRMessageSender.ResponseCode;

public class PostsActivity extends info.toyonos.hfr4droid.common.activity.PostsActivity
{
	protected void onPostingOk(ResponseCode code, long postId)
	{
		EditText postContent = (EditText) postDialog.findViewById(R.id.InputPostContent);
		String content = postContent.getText().toString();

		switch (code)
		{	
			case POST_ADD_OK: // Easter Egg :)
				if (content.toLowerCase().contains(getString(R.string.god))) Toast.makeText(this, R.string.thankyou_respect, Toast.LENGTH_LONG).show();

			default:
				super.onPostingOk(code, postId);
		}
	}
}
