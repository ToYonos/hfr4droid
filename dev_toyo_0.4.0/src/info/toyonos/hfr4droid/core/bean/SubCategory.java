package info.toyonos.hfr4droid.core.bean;

import android.text.Html;

/**
 * <p>Bean modélisant une sous-catégorie</p>
 * 
 * @author ToYonos
 *
 */
public class SubCategory extends Category
{
	private static final long serialVersionUID = 267571710469223260L;
	
	private BasicElement subCat;

	public SubCategory(Category c)
	{
		super(c);
		subCat = new BasicElement();
	}

	public SubCategory(Category c, long id, String name)
	{
		super(c);
		subCat = new BasicElement(id, name);
	}

	public SubCategory(Category c, long id)
	{
		super(c);
		subCat = new BasicElement(id);
	}
	
	@Override
	public String toString()
	{
		return subCat.name != null ? Html.fromHtml(name).toString() + " - " + super.toString() : super.toString();
	}

	@Override
	public long getSubCatId()
	{
		return subCat.id;
	}

	public void setSubCatId(long id)
	{
		this.subCat.id = id;
	}

	public String getSubCatName()
	{
		return subCat.name;
	}

	public void setSubCatName(String name)
	{
		this.subCat.name = name;
	}
}