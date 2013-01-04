package info.toyonos.hfr4droid.lib.core.message;

import info.toyonos.hfr4droid.lib.HFR4droidException;

public class MessageSenderException extends HFR4droidException
{
	private static final long serialVersionUID = 922498477261789672L;

	public MessageSenderException()
	{
		super();
	}

	public MessageSenderException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	public MessageSenderException(String detailMessage)
	{
		super(detailMessage);
	}

	public MessageSenderException(Throwable throwable)
	{
		super(throwable);
	}
}