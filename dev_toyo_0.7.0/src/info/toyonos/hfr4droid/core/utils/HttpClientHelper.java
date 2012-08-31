package info.toyonos.hfr4droid.core.utils;

import info.toyonos.hfr4droid.HFR4droidApplication;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
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
	private static DefaultHttpClient client = null;
	
	public static DefaultHttpClient getHttpClient()
	{
		return getHttpClient(null);
	}
	
	public static DefaultHttpClient getHttpClient(HFR4droidApplication context)
	{
		if (client == null)
		{
			HttpParams parameters = new BasicHttpParams();
			HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(parameters, HTTP.UTF_8);
			ConnPerRoute connPerRoute = new ConnPerRouteBean(50);
			ConnManagerParams.setMaxConnectionsPerRoute(parameters, connPerRoute); 
			ConnManagerParams.setMaxTotalConnections(parameters, 50); 

			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(parameters, schReg);

			client = new DefaultHttpClient(conMgr, parameters);
			
			// @see http://stackoverflow.com/a/6797742
			if(context != null && context.isCompressGzipEnable())
			{
				Log.d(HFR4droidApplication.TAG, "GZIP compression is enable");
				
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
		}
		return client;
	}
	
	public static void closeExpiredConnections()
	{
		if (client != null) client.getConnectionManager().closeExpiredConnections();
	}
	
	public static void shutdown()
	{
		if (client != null) client.getConnectionManager().shutdown();
	}
}
