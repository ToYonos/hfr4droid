package info.toyonos.hfr4droid.core.auth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

public class SerializableCookie implements Serializable
{
	private static final long serialVersionUID = 246395343356359942L;
	private transient Cookie cookie;

	public SerializableCookie(Cookie cookie)
	{
		this.cookie = cookie;
	}

	public Cookie getCookie()
	{
		return cookie;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		// On augmente la durée de vie des cookies à 10 ans
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(Calendar.YEAR, 10);

		oos.defaultWriteObject();
		oos.writeObject(cookie.getName());
		oos.writeObject(cookie.getComment());
		oos.writeObject(cookie.getDomain());
		//oos.writeObject(cookie.getExpiryDate());
		oos.writeObject(calendar.getTime());
		oos.writeObject(cookie.getPath());
		oos.writeObject(cookie.getValue());
		oos.writeInt(cookie.getVersion());
	}		

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		ois.defaultReadObject();
		BasicClientCookie theCookie = new BasicClientCookie((String)ois.readObject(), null);
		theCookie.setComment((String) ois.readObject());
		theCookie.setDomain((String) ois.readObject());
		theCookie.setExpiryDate((Date) ois.readObject());
		theCookie.setPath((String) ois.readObject());
		theCookie.setValue((String) ois.readObject());
		theCookie.setVersion((int) ois.readInt());
		cookie = theCookie;
	}
}
