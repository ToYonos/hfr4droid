package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.core.bean.BasicElement;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

/**
 * <p>Interface d�finissant le moyen de parser des url sur un forum 
 * de discussion MesDiscussions.net</p>
 * 
 * @author ToYonos
 *  
 */
public interface MDUrlParser
{
	/**
	 * Parse une url donn�e
	 * @param url L'url � parser
	 * @return true le parsing � r�ussi, false sinon
	 */
	public boolean parseUrl(String url) throws DataRetrieverException;
	
	/**
	 * Renvoie le <code>BasicElement</code> correspondant aux donn�es de l'url
	 * @return <ul>
	 * <li>Si c'est une url qui va lister les cat�gories, null sera renvoy�</li>
	 * <li>Si c'est une url qui va lister les topics d'une cat�gorie, une instance de <code>Categorie</code> sera renvoy�e</li>
	 * <li>Si c'est une url qui va lister les posts d'un topic, une instance de <code>Topic</code> sera renvoy�e</li>
	 * </ul>
	 */
	public BasicElement getElement();

	/**
	 * Renvoie le num�ro de page trouv� dans l'url
	 * @return le num�ro de page ou -1 si aucune page n'a �t� sp�cifi�e
	 */
	public int getPage();
	
	/**
	 * Renvoie le type de topic trouv� dans l'url
	 * @return le type de topic et par d�faut <code>TopicType.ALL</code> si rien n'a �t� sp�cifi�
	 */
	public TopicType getType();
}