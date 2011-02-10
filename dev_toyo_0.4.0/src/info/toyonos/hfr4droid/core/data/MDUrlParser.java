package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.core.bean.BasicElement;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

/**
 * <p>Interface définissant le moyen de parser des url sur un forum 
 * de discussion MesDiscussions.net</p>
 * 
 * @author ToYonos
 *  
 */
public interface MDUrlParser
{
	/**
	 * Parse une url donnée
	 * @param url L'url à parser
	 * @return true le parsing à réussi, false sinon
	 */
	public boolean parseUrl(String url) throws DataRetrieverException;
	
	/**
	 * Renvoie le <code>BasicElement</code> correspondant aux données de l'url
	 * @return <ul>
	 * <li>Si c'est une url qui va lister les catégories, null sera renvoyé</li>
	 * <li>Si c'est une url qui va lister les topics d'une catégorie, une instance de <code>Categorie</code> sera renvoyée</li>
	 * <li>Si c'est une url qui va lister les posts d'un topic, une instance de <code>Topic</code> sera renvoyée</li>
	 * </ul>
	 */
	public BasicElement getElement();

	/**
	 * Renvoie le numéro de page trouvé dans l'url
	 * @return le numéro de page ou -1 si aucune page n'a été spécifiée
	 */
	public int getPage();
	
	/**
	 * Renvoie le type de topic trouvé dans l'url
	 * @return le type de topic et par défaut <code>TopicType.ALL</code> si rien n'a été spécifié
	 */
	public TopicType getType();
}