package info.toyonos.hfr4droid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	/**
	 * Renvoie le premier match ou le second si le premier est null, du premier groupe trouvé dans une chaine donnée.
	 * @param pattern La regexp à appliquer
	 * @param content Le contenu à analyser
	 * @return La chaine trouvée, null sinon
	 */
	public static String getSingleElement(String pattern, String content)
	{
		Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(content);
		return m.find() ? (m.group(1) != null ? m.group(1) : m.group(2)) : null;
	}
	
	/**
	 * Convertit un <code>InputStream</code> en <code>String</code>
	 * @param is Le flux d'entrée
	 * @param cr Conserver les retours charriot
	 * @return La chaine ainsi obtenu
	 * @throws IOException Si un problème d'entrée/sortie intervient
	 */
	public static String streamToString(InputStream is, boolean cr) throws IOException
	{
		if (is != null)
		{
			StringBuilder sb = new StringBuilder();
			String line;
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null)
				{
					sb.append(line);
					if (cr) sb.append("\n");
				}
			}
			finally
			{
				is.close();
			}
			return sb.toString();
		}
		else
		{        
			return "";
		}
	}
}
