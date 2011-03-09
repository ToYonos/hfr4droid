package info.toyonos.hfr4droid.core.bean;


/**
 * <p>Bean modélisant un topic</p>
 * 
 * @author ToYonos
 *
 */
public class Topic extends BasicElement
{
	private static final long serialVersionUID = -92732456682178896L;

	/**
	 * Les types de topic
	 */
	public static enum TopicType
	{
		ALL(0, "all", "last"),
		CYAN(1, "cyan", "participated"),
		ROUGE(2, "rouge", "read"),
		FAVORI(3, "favori", "favorites");

		private final int value;
		private final String key;
		private final String jsonKey;

		private TopicType(int value, String key, String jsonKey)
		{
			this.value = value;
			this.key = key;
			this.jsonKey = jsonKey;
		}

		public int getValue()
		{
			return this.value;
		}

		public String getKey()
		{
			return this.key;
		}

		public String getJsonKey()
		{
			return this.jsonKey;
		}
		
		public static TopicType fromInt(int anInt) 
		{
			for (TopicType type : TopicType.values())
			{
				if (anInt == type.getValue()) return type;
			}
			return null;
		}
	};

	/**
	 * Les statuts de topic
	 */
	public static enum TopicStatus
	{
		NEW_CYAN,
		NEW_ROUGE,
		NEW_FAVORI,
		NO_NEW_POST,
		NEW_MP,
		NO_NEW_MP,
		LOCKED,
		NONE;
	};

	private TopicStatus status;
	private String author;
	private int lastReadPage;
	private long lastReadPost;
	private int nbPosts;
	private int nbPages;
	private boolean sticky;
	private boolean unread;
	private Category category;

	public Topic(long id)
	{
		super(id, "undefined");
		this.author = "undefined";
		this.status = TopicStatus.NONE;
		this.lastReadPage = -1;
		this.lastReadPost = -1;
		this.nbPosts = -1;
		this.nbPages = -1;
		this.sticky = false;
		this.unread = false;
		this.category = null;
	}

	public Topic(long id, String name)
	{
		this(id);
		this.name = name;
	}

	public Topic(int id, String name, String author, TopicStatus status, int lastReadPage, long lastReadPost, int nbPosts, int nbPages, boolean sticky, boolean unread, Category category)
	{
		super(id, name);
		this.status = status;
		this.author = author;
		this.lastReadPage = lastReadPage;
		this.lastReadPost = lastReadPost;
		this.nbPosts = nbPosts;
		this.nbPages = nbPages;
		this.sticky = sticky;
		this.unread = unread;
		this.category = category;
	}

	public TopicStatus getStatus()
	{
		return status;
	}

	public void setStatus(TopicStatus status)
	{
		this.status = status;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getLastReadPage()
	{
		return lastReadPage;
	}

	public void setLastReadPage(int lastUnreadPage)
	{
		this.lastReadPage = lastUnreadPage;
	}

	public long getLastReadPost()
	{
		return lastReadPost;
	}

	public void setLastReadPost(long lastUnreadPost)
	{
		this.lastReadPost = lastUnreadPost;
	}

	public int getNbPosts()
	{
		return nbPosts;
	}

	public void setNbPosts(int nbPosts)
	{
		this.nbPosts = nbPosts;
	}

	public int getNbPages()
	{
		return nbPages;
	}

	public void setNbPages(int nbPages)
	{
		this.nbPages = nbPages;
	}

	public Category getCategory()
	{
		return category;
	}

	public void setCategory(Category category)
	{
		this.category = category;
	}

	public boolean isSticky()
	{
		return sticky;
	}

	public void setSticky(boolean sticky)
	{
		this.sticky = sticky;
	}

	public boolean isUnread()
	{
		return unread;
	}

	public void setUnread(boolean unread)
	{
		this.unread = unread;
	}
}