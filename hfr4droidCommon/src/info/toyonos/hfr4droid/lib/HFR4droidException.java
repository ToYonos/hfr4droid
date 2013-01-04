package info.toyonos.hfr4droid.lib;

public class HFR4droidException extends Exception
{
	private static final long serialVersionUID = 6776619174712533182L;

	public HFR4droidException()
	{
		super();
	}

	public HFR4droidException(String detailMessage)
	{
		super(detailMessage);
	}

	public HFR4droidException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	public HFR4droidException(Throwable throwable)
	{
		super(throwable);
	}
}