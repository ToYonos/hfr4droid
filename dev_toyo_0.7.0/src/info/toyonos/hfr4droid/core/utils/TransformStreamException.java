package info.toyonos.hfr4droid.core.utils;

import info.toyonos.hfr4droid.HFR4droidException;

public class TransformStreamException extends HFR4droidException
{
	private static final long serialVersionUID = -6362237643644935025L;

	public TransformStreamException()
	{
		super();
	}

	public TransformStreamException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	public TransformStreamException(String detailMessage)
	{
		super(detailMessage);
	}

	public TransformStreamException(Throwable throwable)
	{
		super(throwable);
	}
}