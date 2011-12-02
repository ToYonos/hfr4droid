package info.toyonos.hfr4droid.util.asyncktask;

import info.toyonos.hfr4droid.HFR4droidException;
import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.activity.NewPostUIActivity;
import info.toyonos.hfr4droid.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.core.message.MessageSenderException;
import info.toyonos.hfr4droid.core.message.HFRMessageSender.ResponseCode;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class ValidateMessageAsynckTask extends AsyncTask<Void, Void, ResponseCode>
{
	private NewPostUIActivity context;
	private long postId;
	private ProgressDialog progressDialog;
	
	public ValidateMessageAsynckTask(NewPostUIActivity context, long postId)
	{
		this.context = context;
		this.postId = postId;
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getString(R.string.post_loading));
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
			context.error(e, true, true);
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
				Toast.makeText(context, context.getString("post_" + (postId != -1 ? "edit" : "add") + "_failed"), Toast.LENGTH_SHORT).show();
				return true;

			case POST_FLOOD: // Flood
				Toast.makeText(context, context.getString(R.string.post_flood), Toast.LENGTH_SHORT).show();
				return true;
				
			case POST_MDP_KO: // Wrong password
				Toast.makeText(context, context.getString(R.string.post_wrong_password), Toast.LENGTH_SHORT).show();
				return true;
				
			default:
				return false;
				
		}
	}		
}