package info.toyonos.hfr4droid.core.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * La classe qui gère les thèmes de l'application
 * 
 * @author ToYonos
 *
 */
public class Theme
{ 
	public static final Map<Integer, Theme> themes = new HashMap<Integer, Theme>();

	static
	{
		themes.put(1, new Theme(1, "default", "#F7F7F7", "#C0C0C0", "#336699", "-1", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAyCAIAAAASmSbdAAAACXBIWXMAAAsSAAALEgHS3X78AAAAr0lEQVR42i3D61IBYQCA4fc%2B%2FMNiR2qVU7sxzbgFHZaEQU27qG6hf7ElRDmMQ5juilvp%2B%2BKZeQibL5w%2F%2F5J6WpN6XO02liTFs%2FrPf6O%2BwKgt0O05ujXj1JqSkB%2BmxO8nxOS7MVExUh0RqQw5KX9zXP5Ck0sDtGKfo8Inh4UeodseB%2Fmu2CF4I%2BY%2BUHNt1KxovhMw3%2FBfO%2FjkKwflsoVy0cQrZ17x7LszTVxpm8128wedbTsQqibZlwAAAABJRU5ErkJggg%3D%3D", "#FFFFFF", "#CDCDCD", "#000000", "#000080", "#DEDFDE", "#555555", "#FFFFFF", "#FFEEEE"));
		themes.put(2, new Theme(2, "dark", "#303030", "#676767", "#999999", "-1", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAyCAIAAAASmSbdAAAACXBIWXMAAAsSAAALEgHS3X78AAAAoElEQVR42h3NwapEAABA0fvRVoQQIoQQGkKIEBPi86b3Fmd9KIqC933%2FPc%2FDfd9c18V5nny%2FX47jYN93tm1jXVeWZWGeZ6ZpYhxHhmGg73u6rqNtW5qmoa5rqqri8%2FlQliV%2FR57nZFlGmqYkSUIcx0RRRBiGBEGA7%2Ft4nofrujiOg23bWJaFaZoYhoGu62iahqqqKIqCLMtIkoQoigiC8ANwt1mVLwpzywAAAABJRU5ErkJggg%3D%3D", "#000000", "#323232", "#F7F7F7", "#B0B0FF", "#454545", "#DADADA", "#606060", "#FFEEEE"));
	}
	
	public static Theme getThemeById(int themeId)
	{
		return themes.get(themeId);
	}
	
	private int themeId;
	private String themeKey;
	
	private String listBackground;
	private String listDivider;
	
	private String headerBackground;
	private String headerColor;
	
	private String postHeaderData;
	private String postPseudoColor;
	private String postDateColor;
	private String postColor;
	private String postLinkColor;
	private String postEditQuoteBackground;
	private String postEditQuoteColor;
	private String postBlockBackground;
	private String modoPostBackground;

	public Theme(int themeId, String themeKey, String listBackground,
			String listDivider, String headerBackground, String headerColor,
			String postHeaderData, String postPseudoColor,
			String postDateColor, String postColor, String postLinkColor,
			String postEditQuoteBackground, String postEditQuoteColor,
			String postBlockBackground, String modoPostBackground)
	{
		super();
		this.themeId = themeId;
		this.themeKey = themeKey;
		this.listBackground = listBackground;
		this.listDivider = listDivider;
		this.headerBackground = headerBackground;
		this.headerColor = headerColor;
		this.postHeaderData = postHeaderData;
		this.postPseudoColor = postPseudoColor;
		this.postDateColor = postDateColor;
		this.postColor = postColor;
		this.postLinkColor = postLinkColor;
		this.postEditQuoteBackground = postEditQuoteBackground;
		this.postEditQuoteColor = postEditQuoteColor;
		this.postBlockBackground = postBlockBackground;
		this.modoPostBackground = modoPostBackground;
	}

	public int getThemeId()
	{
		return themeId;
	}

	public void setThemeId(int themeId)
	{
		this.themeId = themeId;
	}

	public String getThemeKey()
	{
		return themeKey;
	}

	public void setThemeKey(String themeKey)
	{
		this.themeKey = themeKey;
	}

	public String getListBackground()
	{
		return listBackground;
	}

	public void setListBackground(String listBackground)
	{
		this.listBackground = listBackground;
	}

	public String getListDivider()
	{
		return listDivider;
	}

	public void setListDivider(String listDivider)
	{
		this.listDivider = listDivider;
	}

	public String getHeaderBackground()
	{
		return headerBackground;
	}

	public void setHeaderBackground(String headerBackground)
	{
		this.headerBackground = headerBackground;
	}

	public String getHeaderColor()
	{
		return headerColor;
	}

	public void setHeaderColor(String headerColor)
	{
		this.headerColor = headerColor;
	}

	public String getPostHeaderData()
	{
		return postHeaderData;
	}

	public void setPostHeaderData(String postHeaderData)
	{
		this.postHeaderData = postHeaderData;
	}

	public String getPostPseudoColor()
	{
		return postPseudoColor;
	}

	public void setPostPseudoColor(String postPseudoColor)
	{
		this.postPseudoColor = postPseudoColor;
	}

	public String getPostDateColor()
	{
		return postDateColor;
	}

	public void setPostDateColor(String postDateColor)
	{
		this.postDateColor = postDateColor;
	}

	public String getPostColor()
	{
		return postColor;
	}

	public void setPostColor(String postColor)
	{
		this.postColor = postColor;
	}

	public String getPostLinkColor()
	{
		return postLinkColor;
	}

	public void setPostLinkColor(String postLinkColor)
	{
		this.postLinkColor = postLinkColor;
	}

	public String getPostEditQuoteBackground()
	{
		return postEditQuoteBackground;
	}

	public void setPostEditQuoteBackground(String postEditQuoteBackground)
	{
		this.postEditQuoteBackground = postEditQuoteBackground;
	}

	public String getPostEditQuoteColor()
	{
		return postEditQuoteColor;
	}

	public void setPostEditQuoteColor(String postEditQuoteColor)
	{
		this.postEditQuoteColor = postEditQuoteColor;
	}

	public String getPostBlockBackground()
	{
		return postBlockBackground;
	}

	public void setPostBlockBackground(String postBlockBackground)
	{
		this.postBlockBackground = postBlockBackground;
	}

	public String getModoPostBackground()
	{
		return modoPostBackground;
	}

	public void setModoPostBackground(String modoPostBackground)
	{
		this.modoPostBackground = modoPostBackground;
	}
}
