package info.toyonos.hfr4droid.common.util.listener;

public interface OnScreenChangeListener
{
	public abstract void onScreenChange(int oldIndex, int newIndex);
	
	public abstract void onFailForward();
	
	public abstract void onFailRearward();
}