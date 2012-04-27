package info.toyonos.hfr4droid.util.listener;

import info.toyonos.hfr4droid.activity.HFR4droidActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

// TODO virer
public abstract class SimpleNavOnGestureListener extends SimpleOnGestureListener
{
	private HFR4droidActivity context;
	
	public SimpleNavOnGestureListener(HFR4droidActivity context)
	{
		this.context = context;
	}
	
	private int getVelocity()
	{
		int swipeMinVelocity;
		switch (context.getSwipe())
		{
			case 2:
				swipeMinVelocity = 350;
				break;
				
			case 3:
				swipeMinVelocity = 200;
				break;					

			default:
				swipeMinVelocity = 500;
				break;			
		}
		return swipeMinVelocity;
	}
	
	private float getWidthPercent()
	{
		float widthPercent;
		switch (context.getSwipe())
		{
			case 2:
				widthPercent = (float)0.6;
				break;
				
			case 3:
				widthPercent = (float)0.4;
				break;					

			default:
				widthPercent = (float)0.75;
				break;			
		}
		return widthPercent;
	}
	
	protected abstract void onLeftToRight(MotionEvent e1, MotionEvent e2);

	protected abstract void onRightToLeft(MotionEvent e1, MotionEvent e2);
	
	public abstract boolean onDoubleTap(MotionEvent e);

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		if (Math.abs(velocityX) > getVelocity() && Math.abs(e1.getX() - e2.getX()) > (metrics.widthPixels * getWidthPercent()))
		{
			if (e1.getX() < e2.getX())
			{
				onLeftToRight(e1, e2);
			} 
			else if (e1.getX() > e2.getX())
			{
				onRightToLeft(e1, e2);
			}
			return true;
		}
		return false;
	}
}