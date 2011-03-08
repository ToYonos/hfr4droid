package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.HFR4droidException;

public class DataRetrieverException extends HFR4droidException
{
	private static final long serialVersionUID = -6362237643644935025L;

	public DataRetrieverException()
	{
		super();
	}

	public DataRetrieverException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	public DataRetrieverException(String detailMessage)
	{
		super(detailMessage);
	}

	public DataRetrieverException(Throwable throwable)
	{
		super(throwable);
	}
}