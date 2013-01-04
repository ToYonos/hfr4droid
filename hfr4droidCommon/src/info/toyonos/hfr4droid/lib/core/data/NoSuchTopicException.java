package info.toyonos.hfr4droid.lib.core.data;

public class NoSuchTopicException extends Exception
{
	private static final long serialVersionUID = 6776619174712533182L;

	public NoSuchTopicException()
	{
		super();
	}

	public NoSuchTopicException(String detailMessage)
	{
		super(detailMessage);
	}
}