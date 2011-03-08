package info.toyonos.hfr4droid.core.auth;

import info.toyonos.hfr4droid.HFR4droidException;

public class AuthenticationException extends HFR4droidException
{
	private static final long serialVersionUID = 4697116882262724779L;

	public AuthenticationException()
	{
		super();
	}

	public AuthenticationException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	public AuthenticationException(String detailMessage)
	{
		super(detailMessage);
	}

	public AuthenticationException(Throwable throwable)
	{
		super(throwable);
	}
}