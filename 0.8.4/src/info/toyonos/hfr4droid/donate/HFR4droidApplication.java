package info.toyonos.hfr4droid.donate;

public class HFR4droidApplication extends info.toyonos.hfr4droid.common.HFR4droidApplication
{
	private boolean birthdayOk = false;

	public boolean isBirthdayOk()
	{
		return birthdayOk;
	}

	public void setBirthdayOk(boolean birthdayOk)
	{
		this.birthdayOk = birthdayOk;
	}
}
