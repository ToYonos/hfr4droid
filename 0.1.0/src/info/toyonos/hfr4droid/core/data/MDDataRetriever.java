package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.util.List;

/**
 * <p>Interface d�finissant le moyen de r�cup�rer des donn�es sur un forum 
 * de discussion MesDiscussions.net</p>
 * 
 * @author ToYonos
 * @see info.toyonos.core.bean.Category
 * @see info.toyonos.core.bean.Topic
 * @see info.toyonos.core.bean.Post
 *  
 */
public interface MDDataRetriever
{	
	/**
	 * R�cup�re le hash check courant
	 * @return Le hash check dans une <code>String</code>
	 */
	public String getHashCheck();

	/**
	 * R�cup�re les cat�gories
	 * @return Une <code>List</code> de <code>Category</code>
	 * @throws Exception Si un probl�me survient
	 */
	public List<Category> getCats() throws Exception;

	/**
	 * R�cup�re les topics d'un type donn� d'une cat�gorie
	 * @param cat La <code>Category</code> d�sir�e
	 * @param type Le type de topic (tous, drapeaux cyan, drapeau rouges ou favoris)
	 * @return Une <code>List</code> de <code>Topic</code>
	 * @throws Exception Si un probl�me survient
	 * @see #getTopics(Category, TopicType, int)
	 */
	public List<Topic> getTopics(Category cat, TopicType type) throws Exception;
	
	/**
	 * R�cup�re les topics d'un type donn� et de la page donn�e d'une cat�gorie
	 * @param cat La <code>Category</code> d�sir�e
	 * @param type Le type de topic (tous, drapeaux cyan, drapeau rouges ou favoris) 
	 * @param pageNumber Le num�ro de la page
	 * @return Une <code>List</code> de <code>Topic</code>
	 * @throws Exception Si un probl�me survient
	 */
	public List<Topic> getTopics(Category cat, TopicType type, int pageNumber) throws Exception;
	
	/**
	 * R�cup�re les posts de la page donn�e d'un topic
	 * @param topic Le <code>Topic</code> d�sir�
	 * @param pageNumber Le num�ro de la page
	 * @return Une <code>List</code> de <code>Post</code>
	 * @throws Exception Si un probl�me survient
	 */
	public List<Post> getPosts(Topic topic, int pageNumber) throws Exception;
	
	/**
	 * Donne le nombre de nouveaux mps
	 * @param topic R�f�rence d'un topic. Si un seul nouveau mp existe, il sera renvoy� par ce biais
	 * @return Le nombre de nouveaux mps
	 * @throws Exception Si un probl�me survient
	 */
	public int countNewMps(Topic topic) throws Exception;
	
	/**
	 * R�cup�re les smileys � ins�rer dans un post 
	 * @param tag Le tag des smileys recherch�s
	 * @return Le code HTML des smileys trouv�s
	 */
	public String getSmiliesByTag(String tag) throws Exception;
	
	/**
	 * R�cup�re le BBCode d'un post � quoter 
	 * @param post Le post concern�
	 * @return Le BBCode obtenu
	 */
	public String getQuote(Post post) throws Exception;
	
	/**
	 * R�cup�re le BBCode d'un post � �diter 
	 * @param post Le post concern�
	 * @return Le BBCode obtenu
	 */
	public String getPostContent(Post post) throws Exception;
}