package info.toyonos.hfr4droid.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public abstract class HttpClient<T>
{		
	/**
	 * Effectue une requête HTTP GET et récupère un <code>T</code> en retour
	 * @param url L'url concernée
	 * @return Un <code>T</code> contenant la réponse
	 * @throws IOException Si un problème intervient durant la requête
	 * @throws URISyntaxException Si l'url est foireuse
	 */
	public T getResponse(String url) throws IOException, URISyntaxException
	{
		DefaultHttpClient client = new DefaultHttpClient();
		InputStream data = null;
		URI uri = new URI(url);
		HttpGet method = new HttpGet(uri);
		method.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; fr; rv:1.9.2) Gecko/20100101 Firefox/4.0.1");

		HttpResponse response = client.execute(method);
		HttpEntity entity = response.getEntity();
		T result = null;
		if (entity != null)
		{
			try
			{
				data = entity.getContent();
				result = transformStream(data);
			}
			catch (IOException e)
			{
				throw e;
			}
			catch (RuntimeException e)
			{
				method.abort();
				throw e;
			}
			finally
			{
				if (entity != null) entity.consumeContent();
				client.getConnectionManager().shutdown();	
			}
		}
		return result;
	}
	
	protected abstract T transformStream(InputStream is) throws IOException;		
}