package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.bean.Topic.TopicType;

import java.util.List;

/**
 * <p>Interface définissant le moyen de récupérer des données sur un forum 
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
	 * Récupère le hash check courant
	 * @return Le hash check dans une <code>String</code>
	 */
	public String getHashCheck();

	/**
	 * Récupère les catégories
	 * @return Une <code>List</code> de <code>Category</code>
	 * @throws Exception Si un problème survient
	 */
	public List<Category> getCats() throws Exception;

	/**
	 * Récupère une <code>Category</code> par son code
	 * @param code Le code de la catégorie
	 * @return La <code>Category</code> trouvée ou null si elle n'existe pas
	 * @throws Exception Si un problème survient
	 */
	public Category getCatByCode(String code) throws Exception;
	
	/**
	 * Récupère une <code>Category</code> par son id
	 * @param id L'id de la catégorie
	 * @return La <code>Category</code> trouvée ou null si elle n'existe pas
	 * @throws Exception Si un problème survient
	 */
	public Category getCatById(long id) throws Exception;
	
	/**
	 * Récupère les topics d'un type donné d'une catégorie
	 * @param cat La <code>Category</code> désirée
	 * @param type Le type de topic (tous, drapeaux cyan, drapeau rouges ou favoris)
	 * @return Une <code>List</code> de <code>Topic</code>
	 * @throws Exception Si un problème survient
	 * @see #getTopics(Category, TopicType, int)
	 */
	public List<Topic> getTopics(Category cat, TopicType type) throws Exception;

	/**
	 * Récupère les topics d'un type donné et de la page donnée d'une catégorie
	 * @param cat La <code>Category</code> désirée
	 * @param type Le type de topic (tous, drapeaux cyan, drapeau rouges ou favoris) 
	 * @param pageNumber Le numéro de la page
	 * @return Une <code>List</code> de <code>Topic</code>
	 * @throws Exception Si un problème survient
	 */
	public List<Topic> getTopics(Category cat, TopicType type, int pageNumber) throws Exception;

	/**
	 * Récupère les posts de la page donnée d'un topic
	 * @param topic Le <code>Topic</code> désiré
	 * @param pageNumber Le numéro de la page
	 * @return Une <code>List</code> de <code>Post</code>
	 * @throws Exception Si un problème survient
	 */
	public List<Post> getPosts(Topic topic, int pageNumber) throws Exception;

	/**
	 * Donne le nombre de nouveaux mps
	 * @param topic Référence d'un topic. Si un seul nouveau mp existe, il sera renvoyé par ce biais
	 * @return Le nombre de nouveaux mps
	 * @throws Exception Si un problème survient
	 */
	public int countNewMps(Topic topic) throws Exception;

	/**
	 * Récupère les smileys à insérer dans un post 
	 * @param tag Le tag des smileys recherchés
	 * @return Le code HTML des smileys trouvés
	 */
	public String getSmiliesByTag(String tag) throws Exception;

	/**
	 * Récupère le BBCode d'un post à quoter 
	 * @param post Le post concerné
	 * @return Le BBCode obtenu
	 */
	public String getQuote(Post post) throws Exception;

	/**
	 * Récupère le BBCode d'un post à éditer 
	 * @param post Le post concerné
	 * @return Le BBCode obtenu
	 */
	public String getPostContent(Post post) throws Exception;
}