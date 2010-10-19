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

	public Category(long id)
	{
		super(id);
	}
	
	public Category(long id, String name)
	{
		super(id, name);
	}
	
	public Category(Category c)
	{
		super(c);
	}
	
	public String getRealId()
	{
		return this.equals(MPS_CAT) ? "prive" : String.valueOf(id);
	}
}