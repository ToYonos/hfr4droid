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
	public T getResponse(String url) throws IOException, URISyntaxException, TransformStreamException
	{
		DefaultHttpClient client = HttpClientHelper.getHttpClient();
		InputStream data = null;
		URI uri = new URI(url);
		HttpGet method = new HttpGet(uri);
		method.setHeader("User-Agent", "Mozilla /4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6) Vodafone/1.0/SFR_v1615/1.56.163.8.39");

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
			catch (RuntimeException e)
			{
				method.abort();
				throw e;
			}
			finally
			{
				if (entity != null) entity.consumeContent();
			}
		}
		return result;
	}
	
	protected abstract T transformStream(InputStream is) throws TransformStreamException;
}