package info.toyonos.hfr4droid.core.utils;

import info.toyonos.hfr4droid.HFR4droidApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public abstract class HttpClient<T>
{
	private HttpClientHelper httpClientHelper;

	public HttpClient(HttpClientHelper httpClientHelper)
	{
		this.httpClientHelper = httpClientHelper;
	}

	/**
	 * Effectue une requête HTTP GET et récupère un <code>T</code> en retour
	 * @param url L'url concernée
	 * @return Un <code>T</code> contenant la réponse
	 * @throws IOException Si un problème intervient durant la requête
	 * @throws URISyntaxException Si l'url est foireuse
	 */
	public T doGet(String url) throws IOException, URISyntaxException, TransformStreamException
	{
		return doGet(url, null);
	}
	
	/**
	 * Effectue une requête HTTP GET et récupère un <code>T</code> en retour
	 * @param url L'url concernée
	 * @param cs Le <code>CookieStore</code> à utiliser
	 * @return Un <code>T</code> contenant la réponse
	 * @throws IOException Si un problème intervient durant la requête
	 * @throws URISyntaxException Si l'url est foireuse
	 */
	public T doGet(String url, CookieStore cs) throws IOException, URISyntaxException, TransformStreamException
	{
		URI uri = new URI(url);
		HttpGet method = new HttpGet(uri);
		method.setHeader("User-Agent", "Mozilla /4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6) Vodafone/1.0/SFR_v1615/1.56.163.8.39");
		return getResponse(method, cs);
	}
	
	/**
	 * Effectue une requête HTTP POST et récupère un <code>T</code> en retour
	 * @param url L'url concernée
	 * @param cs Le <code>CookieStore</code> à utiliser
	 * @param params Les paramètres à passer à la requête
	 * @return Un <code>T</code> contenant la réponse
	 * @throws IOException Si un problème intervient durant la requête
	 * @throws URISyntaxException Si l'url est foireuse
	 */
	public T doPost(String url, CookieStore cs, List<NameValuePair> params) throws IOException, URISyntaxException, TransformStreamException
	{
		URI uri = new URI(url);
		HttpPost method = new HttpPost(uri);
		method.setHeader("User-Agent", "Mozilla /4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6) Vodafone/1.0/SFR_v1615/1.56.163.8.39");
		method.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		return getResponse(method, cs);
	}
	
	private T getResponse(HttpUriRequest request, CookieStore cs) throws IOException, TransformStreamException
	{
		InputStream data = null;
		
		HttpResponse response = null;
		if (cs != null)
		{
			HttpContext httpContext = new BasicHttpContext();
			httpContext.setAttribute(ClientContext.COOKIE_STORE, cs);
			response = httpClientHelper.execute(request, httpContext);
		}
		else
		{
			response = httpClientHelper.execute(request);
		}
		
		if (response == null) return null;
		Log.d(HFR4droidApplication.TAG, "Status : " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
		HttpEntity entity = response.getEntity();

		T result = null;
		if (entity != null)
		{
			try
			{
				data = entity.getContent();
				result = transformStream(data);
			}
			finally
			{
				if (entity != null) entity.consumeContent();
			}
		}
		return result;
	}
	
	/**
	 * Transforme un InputStream dans le type <code>T</code> désiré.
	 * @param is
	 * @return
	 * @throws TransformStreamException
	 */
	protected abstract T transformStream(InputStream is) throws TransformStreamException;
}