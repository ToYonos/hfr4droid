package info.toyonos.hfr4droid.core.bean;

/**
 * <p>Bean mod�lisant un cat�gorie</p>
 * 
 * @author ToYonos
 *
 */
public class Category extends BasicElement
{
	private static final long serialVersionUID = -3959729659502403788L;

	public static final Category MODO_CAT	= new Category(0, "Modos / Admins / Supadmins");
	public static final Category MPS_CAT	= new Category(998, "Messages priv�s");
	public static final Category ALL_CATS	= new Category(999, "Toutes les cat�gories");

	private String code;

	public Category(long id)
	{
		super(id);
		this.code = null;
	}

	public Category(long id, String name)
	{
		super(id, name);
		this.code = null;
	}

	public Category(long id, String code, String name)
	{
		super(id, name);
		this.code = code;
	}

	public Category(Category c)
	{
		super(c);
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
}