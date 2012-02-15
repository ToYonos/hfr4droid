package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.Profile;
import info.toyonos.hfr4droid.core.bean.SubCategory;
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
	 * @throws DataRetrieverException Si un problème survient
	 */
	public String getHashCheck() throws DataRetrieverException;
	
	/**
	 * Récupère l'url de base du forum
	 * @return L'url de base du forum sous forme de <code>String</code>
	 */
	public String getBaseUrl();
	
	/**
	 * Récupère l'url des images perso du forum
	 * @return L'url des images perso du forum sous forme de <code>String</code>
	 */
	public String getImgPersoUrl();

	/**
	 * Récupère les catégories
	 * @return Une <code>List</code> de <code>Category</code>
	 * @throws DataRetrieverException Si un problème survient
	 */
	public List<Category> getCats() throws DataRetrieverException;
	
	/**
	 * Récupère une <code>Category</code> par son code
	 * @param code Le code de la catégorie
	 * @return La <code>Category</code> trouvée ou null si elle n'existe pas
	 * @throws DataRetrieverException Si un problème survient
	 */
	public Category getCatByCode(String code) throws DataRetrieverException;
	
	/**
	 * Récupère une <code>Category</code> par son id
	 * @param id L'id de la catégorie
	 * @return La <code>Category</code> trouvée ou null si elle n'existe pas
	 * @throws DataRetrieverException Si un problème survient
	 */
	public Category getCatById(long id) throws DataRetrieverException;
	
	/**
	 * Récupère les sous catégories d'une catégorie
	 * @param cat La <code>Category</code> désirée
	 * @return Une <code>List</code> de <code>SubCategory</code>
	 * @throws DataRetrieverException Si un problème survient
	 */
	public List<SubCategory> getSubCats(Category cat) throws DataRetrieverException;
	
	/**
	 * Indique si les <code>SubCategory</code> d'une catégorie donnée sont chargés
	 * @param cat La catégorie ciblée
	 * @return true les sous catégories sont chargées, false sinon
	 */
	public boolean isSubCatsLoaded(Category cat) throws DataRetrieverException;
	
	/**
	 * Récupère une <code>SubCategory</code> par son id
	 * @param cat La catégorie ciblée
	 * @param id L'id de la sous-catégorie
	 * @return La <code>SubCategory</code> trouvée ou null si elle n'existe pas
	 * @throws DataRetrieverException Si un problème survient
	 */
	public SubCategory getSubCatById(Category cat, long id) throws DataRetrieverException;
	
	/**
	 * Récupère les topics d'un type donné d'une catégorie
	 * @param cat La <code>Category</code> désirée
	 * @param type Le type de topic (tous, drapeaux cyan, drapeau rouges ou favoris)
	 * @return Une <code>List</code> de <code>Topic</code>
	 * @throws DataRetrieverException Si un problème survient
	 * @see #getTopics(Category, TopicType, int)
	 */
	public List<Topic> getTopics(Category cat, TopicType type) throws DataRetrieverException;

	/**
	 * Récupère les topics d'un type donné et de la page donnée d'une catégorie
	 * @param cat La <code>Category</code> désirée
	 * @param type Le type de topic (tous, drapeaux cyan, drapeau rouges ou favoris) 
	 * @param pageNumber Le numéro de la page
	 * @return Une <code>List</code> de <code>Topic</code>
	 * @throws DataRetrieverException Si un problème survient
	 */
	public List<Topic> getTopics(Category cat, TopicType type, int pageNumber) throws DataRetrieverException;

	/**
	 * Récupère les posts de la page donnée d'un topic
	 * @param topic Le <code>Topic</code> désiré
	 * @param pageNumber Le numéro de la page
	 * @return Une <code>List</code> de <code>Post</code>
	 * @throws DataRetrieverException Si un problème survient
	 */
	public List<Post> getPosts(Topic topic, int pageNumber) throws DataRetrieverException;
	
	/**
	 * Cherche les posts d'un topic donné selon des critères
	 * @param topic Le <code>Topic</code> désiré
	 * @param pseudo L'auteur des posts recherchés
	 * @param word Le mot contenu dans les posts recherchés
	 * @param fromPost Le premier post à partir duquel la recherche doit s'effectuer, non inclus dans les résultats (null si depuis le début)
	 * @return Une <code>List</code> de <code>Post</code>
	 * @throws DataRetrieverException Si un problème survient
	 */
	public List<Post> searchPosts(Topic topic, String pseudo, String word, Post fromPost) throws DataRetrieverException;

	/**
	 * Donne le nombre de nouveaux mps
	 * @param topic Référence d'un topic. Si un seul nouveau mp existe, il sera renvoyé par ce biais
	 * @return Le nombre de nouveaux mps
	 * @throws DataRetrieverException Si un problème survient
	 */
	public int countNewMps(Topic topic) throws DataRetrieverException;

	/**
	 * Récupère les smileys à insérer dans un post 
	 * @param tag Le tag des smileys recherchés
	 * @return Le code HTML des smileys trouvés
	 * @throws DataRetrieverException Si un problème survient
	 */
	public String getSmiliesByTag(String tag) throws DataRetrieverException;

	/**
	 * Récupère le BBCode d'un post à quoter 
	 * @param post Le post concerné
	 * @return Le BBCode obtenu
	 * @throws DataRetrieverException Si un problème survient
	 */
	public String getQuote(Post post) throws DataRetrieverException;

	/**
	 * Récupère le BBCode d'un post à éditer 
	 * @param post Le post concerné
	 * @return Le BBCode obtenu
	 * @throws DataRetrieverException Si un problème survient
	 */
	public String getPostContent(Post post) throws DataRetrieverException;
	
	/**
	 * Récupère les mots clés d'un smiley
	 * @param code le code du smiley
	 * @return Les mots clés obtenus
	 * @throws DataRetrieverException Si un problème survient
	 */
	public String getKeywords(String code) throws DataRetrieverException;
	
	/**
	 * Récupère le profil d'un utilisateur
	 * @param pseudo son pseudo
	 * @return Le <code>Profile</code> de l'utilisateur
	 * @throws DataRetrieverException Si un problème survient
	 */
	public Profile getProfile(String pseudo) throws DataRetrieverException;
}