package info.toyonos.hfr4droid.core.utils;

import info.toyonos.hfr4droid.HFR4droidApplication;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class HttpClientHelper
{	
	private DefaultHttpClient client = null;
	private Map<Long, HttpUriRequestWrapper> requests;
	
	public HttpClientHelper()
	{
		requests = Collections.synchronizedMap(new HashMap<Long, HttpUriRequestWrapper>());
		
		HttpParams parameters = new BasicHttpParams();
		HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(parameters, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(parameters, false);
		ConnPerRoute connPerRoute = new ConnPerRouteBean(50);
		ConnManagerParams.setMaxConnectionsPerRoute(parameters, connPerRoute); 
		ConnManagerParams.setMaxTotalConnections(parameters, 50); 

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(parameters, schReg);

		client = new DefaultHttpClient(conMgr, parameters);
		
		//HttpHost proxy = new HttpHost("192.168.3.108", 8080);
		//client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		
		// @see http://stackoverflow.com/a/6797742
		client.addRequestInterceptor(new HttpRequestInterceptor()
		{
			public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException
			{
				if (!request.containsHeader("Accept-Encoding"))
				{
					request.addHeader("Accept-Encoding", "gzip");
				}
			}
		});

		client.addResponseInterceptor(new HttpResponseInterceptor()
		{
			public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException
			{
				HttpEntity entity = response.getEntity();
				if (entity == null) return;
				Header contentEncodingHeader = entity.getContentEncoding();
				if(contentEncodingHeader != null)
				{
					HeaderElement[] compressors = contentEncodingHeader.getElements();
					for (int idx = 0; idx < compressors.length; idx++)
					{
						if (compressors[idx].getName().equalsIgnoreCase("gzip"))
						{
							response.setEntity(new GzipDecompressingEntity(response.getEntity()));
							return;
						}
					}
				}
			}
		});
	}
	
	public DefaultHttpClient getHttpClient()
	{
		return client;
	}	

	public HttpResponse execute(HttpUriRequest request) throws ClientProtocolException, IOException
	{
		return execute(request, null);
	}

	public HttpResponse execute(HttpUriRequest request, HttpContext context) throws ClientProtocolException, IOException
	{
		if (requests.keySet().size() > 0)
		{
			Log.d(HFR4droidApplication.TAG, "There is still " + requests.keySet().size() + " request(s) in progress");
		}
		Long threadId = Thread.currentThread().getId();
		requests.put(threadId, new HttpUriRequestWrapper(request));
		Log.d(HFR4droidApplication.TAG, "New request to " + request.getURI() + " for the thread #" + threadId);
		try
		{
			HttpResponse response = context != null ? client.execute(request, context) : client.execute(request);
			Log.d(HFR4droidApplication.TAG, "Response received for the thread #" + threadId);
			if (isCancelled(threadId)) return null;
			return response;
		}
		catch (IOException e)
		{
			if (isCancelled(threadId)) return null;
			throw e;
		}
	}
	
	private boolean isCancelled(long threadId)
	{
		HttpUriRequestWrapper request = requests.remove(threadId);
		if (request != null && request.isCancelled())
		{
			Log.d(HFR4droidApplication.TAG, "Request to " + request.get().getURI() + " has been cancelled");
			return true;
		}
		return false;
	}

	public void abortRequest(long threadId)
	{
		HttpUriRequestWrapper request = requests.get(threadId);
		if (request != null)
		{
			Log.d(HFR4droidApplication.TAG, "Aborting request to " + request.get().getURI() + " for the thread #" + threadId);
			// On annule la requête
			request.get().abort();
			// On flag la requête comme annulé
			request.setCancelled(true);
		}
	}
	
	public void closeExpiredConnections()
	{
		client.getConnectionManager().closeExpiredConnections();
	}

	public void shutdown()
	{
		client.getConnectionManager().shutdown();
	}
	
	private class HttpUriRequestWrapper
	{
		private boolean isCancelled;
		private HttpUriRequest request;
	
		public HttpUriRequestWrapper(HttpUriRequest request)
		{
			this.request = request;
		}

		public HttpUriRequest get()
		{
			return request;
		}

		public boolean isCancelled()
		{
			return isCancelled;
		}

		public void setCancelled(boolean isCancelled)
		{
			this.isCancelled = isCancelled;
		}
	}
}
