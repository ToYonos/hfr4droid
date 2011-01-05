package info.toyonos.hfr4droid.core.message;

import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.data.HFRDataRetriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class HFRMessageSender
{	 
	private static final String FORM_URI = "http://forum.hardware.fr/bddpost.php?config=hfr.inc";
	private static final String FORM_EDIT_URI = "http://forum.hardware.fr/bdd.php?config=hfr.inc";
	private static final String FORM_EDIT_KEYWORDS_URI = "http://forum.hardware.fr/wikismilies.php?config=hfr.inc&option_wiki=0&withouttag=0";
	private static final String FAVORITE_URI = "http://forum.hardware.fr/user/addflag.php?config=hfr.inc&cat={$cat}&post={$topic}&numreponse={$post}";

	public static final int POST_OK = 2;
	public static final int POST_EDIT_OK = 1;
	public static final int POST_FLOOD = -1;
	public static final int POST_KO = -99;

	private HFRAuthentication auth;

	public HFRMessageSender(HFRAuthentication authentication)
	{
		auth = authentication;
	}

	public int postMessage(Topic t, String hashCheck, String message, boolean signature) throws UnsupportedEncodingException, IOException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("hash_check", hashCheck));
		params.add(new BasicNameValuePair("post", String.valueOf(t.getId())));
		params.add(new BasicNameValuePair("cat", t.getCategory().getRealId()));
		params.add(new BasicNameValuePair("verifrequet", "1100"));
		params.add(new BasicNameValuePair("MsgIcon", "20"));
		params.add(new BasicNameValuePair("page", String.valueOf(t.getNbPages())));
		params.add(new BasicNameValuePair("pseudo", auth.getUser()));
		params.add(new BasicNameValuePair("content_form", message));
		params.add(new BasicNameValuePair("sujet", t.getName()));
		params.add(new BasicNameValuePair("signature", signature ? "1" : "0"));

		String response = innerGetResponse(FORM_URI, params);
		return getResponseCode(response);
	}

	public int editMessage(Post p, String hashCheck, String message, boolean signature) throws UnsupportedEncodingException, IOException
	{
		StringBuilder parents = new StringBuilder("");
		Matcher m = Pattern.compile("\\[quotemsg=([0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(message);
		while (m.find())
		{
			if (!parents.toString().equals("")) parents.append("-");
			parents.append(m.group(1));
		}
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("hash_check", hashCheck));
		params.add(new BasicNameValuePair("numreponse", String.valueOf(p.getId())));
		params.add(new BasicNameValuePair("post", String.valueOf(p.getTopic().getId())));
		params.add(new BasicNameValuePair("cat", p.getTopic().getCategory().getRealId()));
		params.add(new BasicNameValuePair("verifrequet", "1100"));
		params.add(new BasicNameValuePair("parents", parents.toString()));
		params.add(new BasicNameValuePair("pseudo", auth.getUser()));
		params.add(new BasicNameValuePair("content_form", message));
		params.add(new BasicNameValuePair("sujet", p.getTopic().getName()));
		params.add(new BasicNameValuePair("signature", signature ? "1" : "0"));
		params.add(new BasicNameValuePair("subcat", String.valueOf(p.getTopic().getSubcat())));

		String response = innerGetResponse(FORM_EDIT_URI, params);
		return getResponseCode(response);
	}

	public boolean deleteMessage(Post p, String hashCheck) throws UnsupportedEncodingException, IOException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("hash_check", hashCheck));
		params.add(new BasicNameValuePair("numreponse", String.valueOf(p.getId())));
		params.add(new BasicNameValuePair("post", String.valueOf(p.getTopic().getId())));
		params.add(new BasicNameValuePair("cat", p.getTopic().getCategory().getRealId()));
		params.add(new BasicNameValuePair("pseudo", auth.getUser()));
		params.add(new BasicNameValuePair("delete", "1"));

		String response = innerGetResponse(FORM_EDIT_URI, params);
		return response.matches(".*Message effacé avec succès.*");
	}
	
	/**
	 * Modifie les mots clés d'un smiley
	 * @param hashCheck le hashCheck
	 * @param code le code du smiley
	 * @param keywords les nouveaux mots clés du smiley
	 * @return Le message indiquant si l'opération s'est bien passée
	 * @throws Exception Si un problème survient
	 */
	public String setKeywords(String hashCheck, String code, String keywords) throws Exception
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("modif0", "1"));
		params.add(new BasicNameValuePair("smiley0",code));
		params.add(new BasicNameValuePair("keywords0", keywords));
		params.add(new BasicNameValuePair("hash_check", hashCheck));

		String response = innerGetResponse(FORM_EDIT_KEYWORDS_URI, params);
		return HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response);
	}
	
	/**
	 * Ajoute un favori sur un post donné
	 * @param p le post concerné
	 * @return Le message indiquant si l'opération s'est bien passée
	 * @throws Exception Si un problème survient
	 */
	public String addFavorite(Post p) throws Exception
	{
		String url = FAVORITE_URI.replaceFirst("\\{\\$cat\\}", p.getTopic().getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(p.getTopic().getId()))
		.replaceFirst("\\{\\$post\\}", String.valueOf(p.getId()));
		
		String response = innerGetResponse(url);
		return HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response);
	}

	private String innerGetResponse(String url, List<NameValuePair> params) throws UnsupportedEncodingException, IOException
	{
		HttpContext ctx = new BasicHttpContext();
		ctx.setAttribute(ClientContext.COOKIE_STORE, auth.getCookies());
		HttpClient client = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(client.getParams(), false);
		HttpPost post = new HttpPost(url);
		post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		StringBuilder sb = new StringBuilder("");
		HttpResponse rep = client.execute(post, ctx);
		InputStream is = rep.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String repHtml = reader.readLine();
		while (repHtml != null)
		{
			sb.append(repHtml);
			repHtml = reader.readLine();
		}
		rep.getEntity().consumeContent();
		client.getConnectionManager().shutdown();
		return sb.toString();		
	}
	
	private String innerGetResponse(String url) throws IOException
	{
		HttpContext ctx = new BasicHttpContext();
		ctx.setAttribute(ClientContext.COOKIE_STORE, auth.getCookies());
		HttpClient client = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(client.getParams(), false);
		HttpGet get = new HttpGet(url);
		StringBuilder sb = new StringBuilder("");
		HttpResponse rep = client.execute(get, ctx);
		InputStream is = rep.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String repHtml = reader.readLine();
		while (repHtml != null)
		{
			sb.append(repHtml);
			repHtml = reader.readLine();
		}
		rep.getEntity().consumeContent();
		client.getConnectionManager().shutdown();
		return sb.toString();		
	}

	private int getResponseCode(String response)
	{
		if (response.matches(".*Votre réponse a été postée avec succès.*"))
		{
			/*Matcher m = Pattern.compile("<meta\\s*http\\-equiv=\"Refresh\"\\s*content=\"0;\\s*url=(?:(?:/forum2\\.php.*?page=([0-9]+))|(?:/hfr.*?([0-9]+)\\.htm))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
			.matcher(response);
			if  (m.find())
			{
				return Integer.parseInt(m.group(1) != null ? m.group(1) : m.group(2));  
			}*/
			return POST_OK;
		}
		else if (response.matches(".*Votre message a été édité avec succès.*"))
		{
			return POST_EDIT_OK;
		}
		else if (response.matches(".*flood.*"))
		{
			return POST_FLOOD;
		}
		else
		{
			return POST_KO;
		}
	}
}