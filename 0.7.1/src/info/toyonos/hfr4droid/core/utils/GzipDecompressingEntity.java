package info.toyonos.hfr4droid.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 * Décompression GZIP de l'input stream.
 * @see http://stackoverflow.com/a/6797742
 */
public class GzipDecompressingEntity extends HttpEntityWrapper
{
	public GzipDecompressingEntity(final HttpEntity entity)
	{
		super(entity);
	}

	@Override
	public InputStream getContent()	throws IOException, IllegalStateException
	{
		InputStream wrappedin = wrappedEntity.getContent();
		return new GZIPInputStream(wrappedin);
	}

	@Override
	public long getContentLength()
	{
		return -1;
	}
}
