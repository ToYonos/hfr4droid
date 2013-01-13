package info.toyonos.hfr4droid.common.core.message;

public class HFRMessageResponse
{
	private boolean success;
	private String message;

	public HFRMessageResponse()
	{
		this.success = false;
		this.message = null;
	}
	
	public HFRMessageResponse(boolean success, String message)
	{
		this.success = success;
		this.message = message;
	}

	public boolean isSuccess()
	{
		return success;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
