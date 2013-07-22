package info.toyonos.hfr4droid.common.core.message;

import info.toyonos.hfr4droid.common.HFR4droidApplication;
import info.toyonos.hfr4droid.common.R;
import info.toyonos.hfr4droid.common.core.auth.HFRAuthentication;
import info.toyonos.hfr4droid.common.core.bean.Category;
import info.toyonos.hfr4droid.common.core.bean.Post;
import info.toyonos.hfr4droid.common.core.bean.Topic;
import info.toyonos.hfr4droid.common.core.bean.Topic.TopicType;
import info.toyonos.hfr4droid.common.core.data.HFRDataRetriever;
import info.toyonos.hfr4droid.common.core.utils.HttpClient;
import info.toyonos.hfr4droid.common.core.utils.HttpClientHelper;
import info.toyonos.hfr4droid.common.core.utils.TransformStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class HFRMessageSender
{	 
	private static final String FORM_URI = "http://forum.hardware.fr/bddpost.php?config=hfr.inc";
	private static final String FORM_EDIT_URI = "http://forum.hardware.fr/bdd.php?config=hfr.inc";
	private static final String FORM_EDIT_KEYWORDS_URI = "http://forum.hardware.fr/wikismilies.php?config=hfr.inc&option_wiki=0&withouttag=0";
	private static final String FAVORITE_URI = "http://forum.hardware.fr/user/addflag.php?config=hfr.inc&cat={$cat}&post={$topic}&numreponse={$post}";
	private static final String UNREAD_URI = "http://forum.hardware.fr/user/nonlu.php?config=hfr.inc&cat={$cat}&post={$topic}";
	private static final String UNFLAG_URI = "http://forum.hardware.fr/modo/manageaction.php?config=hfr.inc&cat={$cat}&type_page=forum1&moderation=0";
	private static final String DELETE_MP_URI = "http://forum.hardware.fr/modo/manageaction.php?config=hfr.inc&cat={$cat}&type_page=forum1&moderation=0";
	private static final String AQ_URI = "http://alerte-qualitay.toyonos.info/api/addAlerte.php5";
	private static final String MODO_URI = "http://forum.hardware.fr/user/modo.php?config=hfr.inc&cat={$cat}&post={$topic}&numreponse={$post}";

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

	private HFR4droidApplication context;
	private HFRAuthentication auth;
	private HttpClient<String> client;

	public HFRMessageSender(HFR4droidApplication context, HttpClientHelper httpClientHelper, HFRAuthentication auth)
	{
		this.context = context;
		this.auth = auth;
		client = new HttpClient<String>(httpClientHelper)
		{		
			@Override
			protected String transformStream(InputStream is) throws TransformStreamException
			{
				try
				{
					return HFRDataRetriever.streamToString(is, false);
				}
				catch (IOException e)
				{
					throw new TransformStreamException(e);
				}
			}
		};
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
		params.add(new BasicNameValuePair("emaill", t.hasEmailNotification() ? "1" : "0"));

		String response = null;
		try
		{
			response = client.doPost(FORM_URI, auth.getCookies(), params);
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
			response = client.doPost(FORM_URI, auth.getCookies(), params);
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
		params.add(new BasicNameValuePair("emaill", p.getTopic().hasEmailNotification() ? "1" : "0"));

		String response = null;
		try
		{
			response = client.doPost(FORM_EDIT_URI, auth.getCookies(), params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.post_failed), e);
		}
		return getResponseCode(response);
	}

	public HFRMessageResponse deleteMessage(Post p, String hashCheck) throws MessageSenderException
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
			response = client.doPost(FORM_EDIT_URI, auth.getCookies(), params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.delete_failed), e);
		}

		return new HFRMessageResponse(
			response.matches(".*Message effacé avec succès.*"),
			HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response));
	}
	
	/**
	 * Modifie les mots clés d'un smiley
	 * @param hashCheck le hashCheck
	 * @param code le code du smiley
	 * @param keywords les nouveaux mots clés du smiley
	 * @return Si l'opération s'est bien passée et le message correspondant
	 * @throws MessageSenderException Si un problème survient
	 */
	public HFRMessageResponse setKeywords(String hashCheck, String code, String keywords) throws MessageSenderException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("modif0", "1"));
		params.add(new BasicNameValuePair("smiley0",code));
		params.add(new BasicNameValuePair("keywords0", keywords));
		params.add(new BasicNameValuePair("hash_check", hashCheck));

		String response = null;
		try
		{
			response = client.doPost(FORM_EDIT_KEYWORDS_URI, auth.getCookies(), params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.keywords_failed), e);
		}

		return new HFRMessageResponse(
			response.matches(".*Vos modifications sur les mots clés ont été enregistrés avec succès.*"),
			HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response));
	}
	
	/**
	 * Ajoute un favori sur un post donné
	 * @param p le post concerné
	 * @return Si l'opération s'est bien passée et le message correspondant
	 * @throws MessageSenderException Si un problème survient
	 */
	public HFRMessageResponse addFavorite(Post p) throws MessageSenderException
	{
		String url = FAVORITE_URI.replaceFirst("\\{\\$cat\\}", p.getTopic().getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(p.getTopic().getId()))
		.replaceFirst("\\{\\$post\\}", String.valueOf(p.getId()));
		
		String response = null;
		try
		{
			response = client.doGet(url, auth.getCookies());
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.favorite_failed), e);
		}

		return new HFRMessageResponse(
			response.matches(".*Favori positionné avec succès.*"),
			HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response));
	}
	
	/**
	 * Met un mp en non lu
	 * @param t le topic concerné
	 * @return Si l'opération s'est bien passée et le message correspondant
	 * @throws MessageSenderException Si un problème survient
	 */
	public HFRMessageResponse setUnread(Topic t) throws MessageSenderException
	{
		String url = UNREAD_URI.replaceFirst("\\{\\$cat\\}", t.getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(t.getId()));
		
		String response = null;
		try
		{
			response = client.doGet(url, auth.getCookies());
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.unread_failed), e);
		}

		return new HFRMessageResponse(
			response.matches(".*Le message a été marqué comme non lu avec succès.*"),
			HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response));
	}
	
	/**
	 * Déflag un topic
	 * @param t le topic concerné
	 * @return Si l'opération s'est bien passée et le message correspondant
	 * @throws MessageSenderException Si un problème survient
	 */
	public HFRMessageResponse unflag(Topic t, TopicType type, String hashCheck) throws MessageSenderException
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
			response = client.doPost(UNFLAG_URI.replaceFirst("\\{\\$cat\\}", t.getCategory().getRealId()), auth.getCookies(), params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.unflag_failed), e);
		}

		return new HFRMessageResponse(
			response.matches(".*Drapeaux effacés avec succès.*"),
			HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response));
	}
	
	/**
	 * Supprime un message privé
	 * @param t le mp concerné
	 * @return Si l'opération s'est bien passée et le message correspondant
	 * @throws MessageSenderException Si un problème survient
	 */
	public HFRMessageResponse deleteMP(Topic t, String hashCheck) throws MessageSenderException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action_reaction", "valid_eff_prive"));
		params.add(new BasicNameValuePair("topic1", String.valueOf(t.getId())));
		params.add(new BasicNameValuePair("hash_check", hashCheck));

		String response = null;
		try
		{
			response = client.doPost(DELETE_MP_URI.replaceFirst("\\{\\$cat\\}", t.getCategory().getRealId()), auth.getCookies(), params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.delete_mp_failed), e);
		}

		return new HFRMessageResponse(
			response.matches(".*Action effectuée avec succès.*"),
			HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*</div>", response));
	}
	
	/**
	 * Fait une alerte modo pour un post donné
	 * @param p le post concerné
	 * @return Si l'opération s'est bien passée et le message correspondant
	 * @throws MessageSenderException Si un problème survient
	 */
	public HFRMessageResponse warmModeration(Post p, String reason, String hashCheck) throws MessageSenderException
	{
		String url = MODO_URI.replaceFirst("\\{\\$cat\\}", p.getTopic().getCategory().getRealId())
		.replaceFirst("\\{\\$topic\\}", String.valueOf(p.getTopic().getId()))
		.replaceFirst("\\{\\$post\\}", String.valueOf(p.getId()));
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("raison", reason));
		params.add(new BasicNameValuePair("hash_check", hashCheck));

		String response = null;
		try
		{
			response = client.doPost(url, auth.getCookies(), params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.warm_modo_failed), e);
		}

		return new HFRMessageResponse(
			!response.matches(".*Un mail a déjà été envoyé aux modérateurs.*"),
			HFRDataRetriever.getSingleElement("<div\\s*class=\"hop\">\\s*(.*?)\\s*<br />", response));
	}
	
	/**
	 * Fait une alerte qualitaÿ sur le post concerné
	 * @param alertId l'id de l'alerte existante en cas de +1, -1 si nouvelle alerte
	 * @param alertName le nom de l'alerte si nouvelle alerte
	 * @param p le post concerné
	 * @param postUrl son url
	 * @param comment l'éventuel commentaire
	 * @return Si l'opération s'est bien passée et le message correspondant
	 * @throws MessageSenderException
	 */
	public HFRMessageResponse alertPost(long alertId, String alertName, Post p, String postUrl, String comment) throws MessageSenderException
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("alerte_qualitay_id", String.valueOf(alertId)));
		if (alertId == -1)
		{
			params.add(new BasicNameValuePair("nom", alertName));
			params.add(new BasicNameValuePair("topic_id", String.valueOf(p.getTopic().getId())));
			params.add(new BasicNameValuePair("topic_titre", p.getTopic().getName()));
		}
		params.add(new BasicNameValuePair("pseudo", auth.getUser()));
		params.add(new BasicNameValuePair("post_id", String.valueOf(p.getId())));
		params.add(new BasicNameValuePair("post_url", postUrl));
		if (comment != null && !comment.equals("")) params.add(new BasicNameValuePair("commentaire", comment));
	
		String response = null;
		try
		{
			response = client.doPost(AQ_URI, auth.getCookies(), params);
		}
		catch (Exception e)
		{
			throw new MessageSenderException(context.getString(R.string.alert_post_failed), e);
		}
		
		int code = -1;
		try
		{
			code = Integer.parseInt(response);
		}
		catch (NumberFormatException e){} // Reste en -1 : erreur		

		return new HFRMessageResponse(
			code == 1,
			getAQResponseAsString(code));
	}
	
	private String getAQResponseAsString(int code)
	{
		switch (code)
		{
			case 1:
				return context.getString(R.string.aq_ok);
				
			case -2:
				return context.getString(R.string.aq_not_found);
				
			case -3:
				return context.getString(R.string.aq_missing_parameter);
				
			case -4:
				return context.getString(R.string.aq_already_report);

			default:
				return context.getString(R.string.aq_ko);
		}
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