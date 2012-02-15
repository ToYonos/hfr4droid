package info.toyonos.hfr4droid.core.data;

import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Post;
import info.toyonos.hfr4droid.core.bean.Profile;
import info.toyonos.hfr4droid.core.bean.SubCategory;
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
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public String getHashCheck() throws DataRetrieverException;
	
	/**
	 * R�cup�re l'url de base du forum
	 * @return L'url de base du forum sous forme de <code>String</code>
	 */
	public String getBaseUrl();
	
	/**
	 * R�cup�re l'url des images perso du forum
	 * @return L'url des images perso du forum sous forme de <code>String</code>
	 */
	public String getImgPersoUrl();

	/**
	 * R�cup�re les cat�gories
	 * @return Une <code>List</code> de <code>Category</code>
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public List<Category> getCats() throws DataRetrieverException;
	
	/**
	 * R�cup�re une <code>Category</code> par son code
	 * @param code Le code de la cat�gorie
	 * @return La <code>Category</code> trouv�e ou null si elle n'existe pas
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public Category getCatByCode(String code) throws DataRetrieverException;
	
	/**
	 * R�cup�re une <code>Category</code> par son id
	 * @param id L'id de la cat�gorie
	 * @return La <code>Category</code> trouv�e ou null si elle n'existe pas
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public Category getCatById(long id) throws DataRetrieverException;
	
	/**
	 * R�cup�re les sous cat�gories d'une cat�gorie
	 * @param cat La <code>Category</code> d�sir�e
	 * @return Une <code>List</code> de <code>SubCategory</code>
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public List<SubCategory> getSubCats(Category cat) throws DataRetrieverException;
	
	/**
	 * Indique si les <code>SubCategory</code> d'une cat�gorie donn�e sont charg�s
	 * @param cat La cat�gorie cibl�e
	 * @return true les sous cat�gories sont charg�es, false sinon
	 */
	public boolean isSubCatsLoaded(Category cat) throws DataRetrieverException;
	
	/**
	 * R�cup�re une <code>SubCategory</code> par son id
	 * @param cat La cat�gorie cibl�e
	 * @param id L'id de la sous-cat�gorie
	 * @return La <code>SubCategory</code> trouv�e ou null si elle n'existe pas
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public SubCategory getSubCatById(Category cat, long id) throws DataRetrieverException;
	
	/**
	 * R�cup�re les topics d'un type donn� d'une cat�gorie
	 * @param cat La <code>Category</code> d�sir�e
	 * @param type Le type de topic (tous, drapeaux cyan, drapeau rouges ou favoris)
	 * @return Une <code>List</code> de <code>Topic</code>
	 * @throws DataRetrieverException Si un probl�me survient
	 * @see #getTopics(Category, TopicType, int)
	 */
	public List<Topic> getTopics(Category cat, TopicType type) throws DataRetrieverException;

	/**
	 * R�cup�re les topics d'un type donn� et de la page donn�e d'une cat�gorie
	 * @param cat La <code>Category</code> d�sir�e
	 * @param type Le type de topic (tous, drapeaux cyan, drapeau rouges ou favoris) 
	 * @param pageNumber Le num�ro de la page
	 * @return Une <code>List</code> de <code>Topic</code>
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public List<Topic> getTopics(Category cat, TopicType type, int pageNumber) throws DataRetrieverException;

	/**
	 * R�cup�re les posts de la page donn�e d'un topic
	 * @param topic Le <code>Topic</code> d�sir�
	 * @param pageNumber Le num�ro de la page
	 * @return Une <code>List</code> de <code>Post</code>
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public List<Post> getPosts(Topic topic, int pageNumber) throws DataRetrieverException;
	
	/**
	 * Cherche les posts d'un topic donn� selon des crit�res
	 * @param topic Le <code>Topic</code> d�sir�
	 * @param pseudo L'auteur des posts recherch�s
	 * @param word Le mot contenu dans les posts recherch�s
	 * @param fromPost Le premier post � partir duquel la recherche doit s'effectuer, non inclus dans les r�sultats (null si depuis le d�but)
	 * @return Une <code>List</code> de <code>Post</code>
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public List<Post> searchPosts(Topic topic, String pseudo, String word, Post fromPost) throws DataRetrieverException;

	/**
	 * Donne le nombre de nouveaux mps
	 * @param topic R�f�rence d'un topic. Si un seul nouveau mp existe, il sera renvoy� par ce biais
	 * @return Le nombre de nouveaux mps
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public int countNewMps(Topic topic) throws DataRetrieverException;

	/**
	 * R�cup�re les smileys � ins�rer dans un post 
	 * @param tag Le tag des smileys recherch�s
	 * @return Le code HTML des smileys trouv�s
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public String getSmiliesByTag(String tag) throws DataRetrieverException;

	/**
	 * R�cup�re le BBCode d'un post � quoter 
	 * @param post Le post concern�
	 * @return Le BBCode obtenu
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public String getQuote(Post post) throws DataRetrieverException;

	/**
	 * R�cup�re le BBCode d'un post � �diter 
	 * @param post Le post concern�
	 * @return Le BBCode obtenu
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public String getPostContent(Post post) throws DataRetrieverException;
	
	/**
	 * R�cup�re les mots cl�s d'un smiley
	 * @param code le code du smiley
	 * @return Les mots cl�s obtenus
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public String getKeywords(String code) throws DataRetrieverException;
	
	/**
	 * R�cup�re le profil d'un utilisateur
	 * @param pseudo son pseudo
	 * @return Le <code>Profile</code> de l'utilisateur
	 * @throws DataRetrieverException Si un probl�me survient
	 */
	public Profile getProfile(String pseudo) throws DataRetrieverException;
}