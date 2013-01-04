package info.toyonos.hfr4droid.lib.util.listener;

public interface OnScreenChangeListener
{
	public abstract void onScreenChange(int oldIndex, int newIndex);
	
	public abstract void onFailForward();
	
	public abstract void onFailRearward();
}