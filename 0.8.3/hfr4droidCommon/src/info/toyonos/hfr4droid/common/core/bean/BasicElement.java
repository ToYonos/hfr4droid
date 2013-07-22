package info.toyonos.hfr4droid.common.core.bean;

import java.io.Serializable;

import android.text.Html;

/**
 * <p>Bean abstrait générique</p>
 * 
 * @author ToYonos
 *
 */
public class BasicElement implements Serializable
{
	private static final long serialVersionUID = -2107268897920840964L;

	protected long id;

	protected String name;

	public BasicElement()
	{
		this.id = -1;
		this.name = "undefined";
	}

	public BasicElement(long id)
	{
		this();
		this.id = id;
	}

	public BasicElement(long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public BasicElement(BasicElement element)
	{
		this.id = element.id;
		this.name = new String(element.name);
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name != null ? Html.fromHtml(name).toString() : "[undefined]";
	}

	@Override
	public int hashCode()
	{
		return (int) id;
	}

	@Override
	public boolean equals(Object o)
	{
		return (o instanceof BasicElement) && ((BasicElement) o).getId() == this.id; 
	}
}
