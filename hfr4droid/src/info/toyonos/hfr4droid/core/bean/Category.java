package info.toyonos.hfr4droid.core.bean;


/**
 * <p>Bean modélisant un catégorie</p>
 * 
 * @author ToYonos
 *
 */
public class Category extends BasicElement
{
	private static final long serialVersionUID = -3959729659502403788L;

	public static final Category MODO_CAT	= new Category(0, "Modos / Admins / Supadmins");
	public static final Category MPS_CAT	= new Category(998, "Messages privés");
	public static final Category ALL_CATS	= new Category(999, "Toutes les catégories");

	private String code;

	public Category(long id)
	{
		super(id);
		this.code = null;
	}

	public Category(long id, String name)
	{
		this(id);
		this.name = name;
	}

	public Category(long id, String code, String name)
	{
		this(id, name);
		this.code = code;
	}

	public Category(Category c)
	{
		super(c);
		if (c.getCode() != null) this.code = new String(c.getCode());
	}

	public String getRealId()
	{
		return this.equals(MPS_CAT) ? "prive" : String.valueOf(id);
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}
	
	public long getSubCatId()
	{
		return -1;
	}
	
	@Override
	public int hashCode()
	{
		return getSubCatId() == -1 ? (int) id : (int) getSubCatId();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Category)
		{
			Category otherCat = (Category) o;
			long realId1 = getSubCatId() == -1 ? id : getSubCatId();
			long realId2 = otherCat.getSubCatId() == -1 ? otherCat.id : otherCat.getSubCatId();
			return realId1 == realId2 && this.getClass().equals(o.getClass());
		}
		return false;
	}
}