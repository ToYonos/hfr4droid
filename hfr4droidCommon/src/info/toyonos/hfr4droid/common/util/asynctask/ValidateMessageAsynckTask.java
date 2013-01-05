package info.toyonos.hfr4droid.common.util.asynctask;

import info.toyonos.hfr4droid.common.HFR4droidException;
import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.activity.HFR4droidActivity;
import info.toyonos.hfr4droid.common.core.data.DataRetrieverException;
import info.toyonos.hfr4droid.common.core.message.HFRMessageSender.ResponseCode;
import info.toyonos.hfr4droid.common.core.message.MessageSenderException;
import android.widget.Toast;

public abstract class ValidateMessageAsynckTask extends ProgressDialogAsyncTask<Void, Void, ResponseCode>
{
	protected long postId;
	
	public ValidateMessageAsynckTask(HFR4droidActivity context, long postId)
	{
		super(context);
		this.context = context;
		this.postId = postId;
	}
	
	protected abstract boolean canExecute(); 
	
	protected abstract ResponseCode validateMessage() throws MessageSenderException, DataRetrieverException;
	
	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		progressDialog.setMessage(context.getString(R.string.post_loading));
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
		setThreadId();
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