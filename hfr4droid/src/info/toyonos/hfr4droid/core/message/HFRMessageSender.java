package info.toyonos.hfr4droid.core.message;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;
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

import android.content.Context;

public class HFRMessageSender
{	 
	private static final String FORM_URI = "http://forum.hardware.fr/bddpost.php?config=hfr.inc";
	private static final String FORM_EDIT_URI = "http://forum.hardware.fr/bdd.php?config=hfr.inc";
	private static final String FORM_EDIT_KEYWORDS_URI = "http://forum.hardware.fr/wikismilies.php?config=hfr.inc&option_wiki=0&withouttag=0";
	private static final String FAVORITE_URI = "http://forum.hardware.fr/user/addflag.php?config=hfr.inc&cat={$cat}&post={$topic}&numreponse={$post}";
	private static final String UNREAD_URI = "http://forum.hardware.fr/user/nonlu.php?config=hfr.inc&cat={$cat}&post={$topic}";
	private static final String UNFLAG_URI = "http://forum.hardware.fr//modo/manageaction.php?config=hfr.inc&cat={$cat}&type_page=forum1&moderation=0";

	/**
	 * Les codes des réponses
	 */
	public static enum ResponseCode
	{
		POST_EDIT_OK,
		POST_ADD_OK,
		TOPIC_NEW_OK,
		POST_FLOOD,
		TOPIC_FLOOD,
		POST_MDP_KO,
		MP_INVALID_RECIPIENT,
		POST_KO,
		POST_KO_EXCEPTION;
	};

	private Context context;
	private HFRAuthentication auth;

	public HFRMessageSender(Context context, HFRAuthentication auth)
	{
		this.context = context;
		this.auth = auth;
	}

	public ResponseCode postMessage(Topic t, String hashCheck, String message, boolean signature) throws MessageSenderException
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

		String response = null;
		try
		{
			response = innerGetResponse(FORM_URI, params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.post_failed), e);
		}
		return getResponseCode(response);
	}
	
	public ResponseCode newTopic(Category c, String hashCheck, String dest, String sujet, String message, boolean signature) throws MessageSenderException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("hash_check", hashCheck));
		params.add(new BasicNameValuePair("cat", c.getRealId()));
		params.add(new BasicNameValuePair("verifrequet", "1100"));
		params.add(new BasicNameValuePair("MsgIcon", "20"));
		//params.add(new BasicNameValuePair("page", String.valueOf(t.getNbPages())));
		params.add(new BasicNameValuePair("pseudo", auth.getUser()));
		params.add(new BasicNameValuePair("content_form", message));
		params.add(new BasicNameValuePair("dest", dest));
		params.add(new BasicNameValuePair("sujet", sujet));
		params.add(new BasicNameValuePair("signature", signature ? "1" : "0"));

		String response = null;
		try
		{
			response = innerGetResponse(FORM_URI, params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.post_failed), e);
		}
		return getResponseCode(response);
	}

	public ResponseCode editMessage(Post p, String hashCheck, String message, boolean signature) throws MessageSenderException
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
		params.add(new BasicNameValuePair("subcat", String.valueOf(p.getTopic().getSubCategory().getSubCatId())));

		String response = null;
		try
		{
			response = innerGetResponse(FORM_EDIT_URI, params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.post_failed), e);
		}
		return getResponseCode(response);
	}

	public boolean deleteMessage(Post p, String hashCheck) throws MessageSenderException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("hash_check", hashCheck));
		params.add(new BasicNameValuePair("numreponse", String.valueOf(p.getId())));
		params.add(new BasicNameValuePair("post", String.valueOf(p.getTopic().getId())));
		params.add(new BasicNameValuePair("cat", p.getTopic().getCategory().getRealId()));
		params.add(new BasicNameValuePair("pseudo", auth.getUser()));
		params.add(new BasicNameValuePair("delete", "1"));

		String response = null;
		try
		{
			response = innerGetResponse(FORM_EDIT_URI, params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.delete_failed), e);
		}
		return response.matches(".*Message effacé avec succès.*");
	}
	
	/**
	 * Modifie les mots clés d'un smiley
	 * @param hashCheck le hashCheck
	 * @param code le code du smiley
	 * @param keywords les nouveaux mots clés du smiley
	 * @return Le message indiquant si l'opération s'est bien passée
	 * @throws MessageSenderException Si un problème survient
	 */
	public String setKeywords(String hashCheck, String code, String keywords) throws MessageSenderException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("modif0", "1"));
		params.add(new BasicNameValuePair("smiley0",code));
		params.add(new BasicNameValuePair("keywords0", keywords));
		params.add(new BasicNameValuePair("hash_check", hashCheck));

		String response = null;
		try
		{
			response = innerGetResponse(FORM_EDIT_KEYWORDS_URI, params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.keywords_failed), e);
		}
		return HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response);
	}
	
	/**
	 * Ajoute un favori sur un post donné
	 * @param p le post concerné
	 * @return Le message indiquant si l'opération s'est bien passée
	 * @throws MessageSenderException Si un problème survient
	 */
	public String addFavorite(Post p) throws MessageSenderException
	{
		String url = FAVORITE_URI.replaceFirst("\\{\\$cat\\}", p.getTopic().getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(p.getTopic().getId()))
		.replaceFirst("\\{\\$post\\}", String.valueOf(p.getId()));
		
		String response = null;
		try
		{
			response = innerGetResponse(url);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.favorite_failed), e);
		}
		return HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response);
	}
	
	/**
	 * Met un mp en non lu
	 * @param t le topic concerné
	 * @return true ou false suivant si l'opération s'est bien passé
	 * @throws MessageSenderException Si un problème survient
	 */
	public boolean setUnread(Topic t) throws MessageSenderException
	{
		String url = UNREAD_URI.replaceFirst("\\{\\$cat\\}", t.getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(t.getId()));
		
		String response = null;
		try
		{
			response = innerGetResponse(url);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.unread_failed), e);
		}
		return response.matches(".*Le message a été marqué comme non lu avec succès.*");
	}
	
	/**
	 * Déflag un topic
	 * @param t le topic concerné
	 * @return Le message indiquant si l'opération s'est bien passée
	 * @throws MessageSenderException Si un problème survient
	 */
	public String unflag(Topic t, TopicType type, String hashCheck) throws MessageSenderException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action_reaction", "message_forum_delflags"));
		params.add(new BasicNameValuePair("topic0", String.valueOf(t.getId())));
		params.add(new BasicNameValuePair("valuecat0", t.getCategory().getRealId()));
		params.add(new BasicNameValuePair("valueforum0", "hardwarefr"));
		params.add(new BasicNameValuePair("type_page", "forum1"));
		params.add(new BasicNameValuePair("owntopic", String.valueOf(type.getValue())));
		params.add(new BasicNameValuePair("topic1", "-1"));
		params.add(new BasicNameValuePair("topic_statusno1", "-1"));
		params.add(new BasicNameValuePair("hash_check", hashCheck));

		String response = null;
		try
		{
			response = innerGetResponse(UNFLAG_URI.replaceFirst("\\{\\$cat\\}", t.getCategory().getRealId()), params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.keywords_failed), e);
		}
		return HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response);
	}

	private String innerGetResponse(String url, List<NameValuePair> params) throws UnsupportedEncodingException, IOException
	{
		HttpContext ctx = new BasicHttpContext();
		ctx.setAttribute(ClientContext.COOKIE_STORE, auth.getCookies());
		HttpClient client = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(client.getParams(), false);
		HttpPost post = new HttpPost(url);
		post.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; fr; rv:1.9.2) Gecko/20100101 Firefox/4.0.1");
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
		get.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; fr; rv:1.9.2) Gecko/20100101 Firefox/4.0.1");
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

	private ResponseCode getResponseCode(String response)
	{
		if (response.matches(".*Votre réponse a été postée avec succès.*"))
		{
			/*Matcher m = Pattern.compile("<meta\\s*http\\-equiv=\"Refresh\"\\s*content=\"0;\\s*url=(?:(?:/forum2\\.php.*?page=([0-9]+))|(?:/hfr.*?([0-9]+)\\.htm))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
			.matcher(response);
			if  (m.find())
			{
				return Integer.parseInt(m.group(1) != null ? m.group(1) : m.group(2));  
			}*/
			return ResponseCode.POST_ADD_OK;
		}
		else if (response.matches(".*Votre message a été posté avec succès.*"))
		{
			return ResponseCode.TOPIC_NEW_OK;
		}		
		else if (response.matches(".*Votre message a été édité avec succès.*"))
		{
			return ResponseCode.POST_EDIT_OK;
		}
		else if (response.matches(".*Désolé, le pseudo suivant n'existe pas.*"))
		{
			return ResponseCode.MP_INVALID_RECIPIENT;
		}
		else if (response.matches(".*Mot de passe incorrect !*"))
		{
			return ResponseCode.POST_MDP_KO;
		}		
		else if (response.matches(".*réponses consécutives.*"))
		{
			return ResponseCode.POST_FLOOD;
		}
		else if (response.matches(".*nouveaux sujets consécutifs.*"))
		{
			return ResponseCode.TOPIC_FLOOD;
		}
		else
		{
			return ResponseCode.POST_KO;
		}
	}
}