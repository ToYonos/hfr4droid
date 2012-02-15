package info.toyonos.hfr4droid.core.bean;

import java.util.Date;

import android.graphics.Bitmap;

/**
 * Le profile d'un utilisateur
 * 
 * @author ToYonos
 *
 */
public class Profile
{
	//public static final String DELETED_PROFILE = "Profil supprimé";
	
	/**
	 * Les genres
	 */
	public static enum Gender
	{
		MAN("homme", "Homme"),
		WOMAN("femme", "Femme"),
		ANGEL("NA", "Ange");

		private final String key;
		private final String label;

		private Gender(String key, String label)
		{
			this.key = key;
			this.label = label;
		}

		public String getKey()
		{
			return this.key;
		}
		
		public String getLabel()
		{
			return this.label;
		}
		
		public static Gender fromString(String key) 
		{
			for (Gender gender : Gender.values())
			{
				if (gender.getKey().equals(key)) return gender;
			}
			return null;
		}
	};
	
	/**
	 * Les type de profil
	 */
	public static enum ProfileType
	{
		MEMBER("Membre", "Membre"),
		MODO("Modérateur", "Modérateur"),
		SUPADMIN("Super Administrateur", "Super Administrateur"),
		ARCHI("Architecte / Développeur principal", "Architecte / Développeur principal");

		private final String key;
		private final String label;

		private ProfileType(String key, String label)
		{
			this.key = key;
			this.label = label;
		}

		public String getKey()
		{
			return this.key;
		}
		
		public String getLabel()
		{
			return this.label;
		}
		
		public static ProfileType fromString(String key) 
		{
			for (ProfileType type : ProfileType.values())
			{
				if (type.getKey().equals(key)) return type;
			}
			return null;
		}
	};
	
	private String pseudo;
	private Date birthDate;
	private String[] location;
	private String city;
	private Gender gender;
	private int nbPosts;
	private ProfileType type;
	private Date lastPostDate;
	private Date registrationDate;
	private String avatarUrl;
	private Bitmap avatarBitmap;
	private String[] smileysUrls;

	public Profile()
	{
		this.pseudo = null;
		this.birthDate = null;
		this.location = null;
		this.city = null;
		this.gender = null;
		this.nbPosts = -1;
		this.type = null;
		this.lastPostDate = null;
		this.registrationDate = null;
		this.avatarUrl = null;
		this.avatarBitmap = null;
		this.smileysUrls = null;
	}

	public Profile(String pseudo, Date birthDate, String[] location, String city, Gender gender, int nbPosts, ProfileType type, Date lastPost, Date registration, String avatarUrl,	String[] smileysUrls)
	{
		this.pseudo = pseudo;
		this.birthDate = birthDate;
		this.location = location;
		this.city = city;
		this.gender = gender;
		this.nbPosts = nbPosts;
		this.type = type;
		this.lastPostDate = lastPost;
		this.registrationDate = registration;
		this.avatarUrl = avatarUrl;
		this.avatarBitmap = null;
		this.smileysUrls = smileysUrls;
	}

	public String getPseudo()
	{
		return pseudo;
	}

	public void setPseudo(String pseudo)
	{
		this.pseudo = pseudo;
	}

	public Date getBirthDate()
	{
		return birthDate;
	}

	public void setBirthDate(Date birthDate)
	{
		this.birthDate = birthDate;
	}

	public String[] getLocation()
	{
		return location;
	}

	public void setLocation(String[] location)
	{
		this.location = location;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public Gender getGender()
	{
		return gender;
	}

	public void setGender(Gender gender)
	{
		this.gender = gender;
	}

	public int getNbPosts()
	{
		return nbPosts;
	}

	public void setNbPosts(int nbPosts)
	{
		this.nbPosts = nbPosts;
	}

	public ProfileType getType()
	{
		return type;
	}

	public void setType(ProfileType type)
	{
		this.type = type;
	}

	public String getAvatarUrl()
	{
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl)
	{
		this.avatarUrl = avatarUrl;
	}

	public Date getLastPostDate()
	{
		return lastPostDate;
	}

	public void setLastPostDate(Date lastPostDate)
	{
		this.lastPostDate = lastPostDate;
	}

	public Date getRegistrationDate()
	{
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate)
	{
		this.registrationDate = registrationDate;
	}

	public Bitmap getAvatarBitmap()
	{
		return avatarBitmap;
	}

	public void setAvatarBitmap(Bitmap avatarBitmap)
	{
		this.avatarBitmap = avatarBitmap;
	}

	public String[] getSmileysUrls()
	{
		return smileysUrls;
	}

	public void setSmileysUrls(String[] smileysUrls)
	{
		this.smileysUrls = smileysUrls;
	}
}
