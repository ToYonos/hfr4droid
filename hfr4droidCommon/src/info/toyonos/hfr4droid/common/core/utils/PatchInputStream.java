package info.toyonos.hfr4droid.common.core.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author ToYonos
 * @see http://code.google.com/p/android/issues/detail?id=6066
 *
 */
public class PatchInputStream extends FilterInputStream
{
	public PatchInputStream(InputStream in)
	{
		super(in);
	}
	public long skip(long n) throws IOException
	{
		long m = 0L;
		while (m < n)
		{
			long _m = in.skip(n-m);
			if (_m == 0L) break;
			m += _m;
		}
		return m;
	}
}