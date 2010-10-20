package com.naholyr.android.ui;

import java.util.List;

import android.app.Activity;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class HFR4droidQuickActionWindow extends QuickActionWindow
{
	protected HFR4droidQuickActionWindow(LayoutInflater inflater, SparseIntArray config, List<Item> items)
	{
		super(inflater, config, items);
	}

	public static HFR4droidQuickActionWindow getWindow(Activity activity, SparseIntArray config)
	{
		return getWindow(activity, config, null);
	}

	public static HFR4droidQuickActionWindow getWindow(Activity activity, SparseIntArray config, List<Item> items)
	{
		LayoutInflater inflater = activity.getLayoutInflater();
		return new HFR4droidQuickActionWindow(inflater, config, items);
	}

	public void show(View anchor, int yOffset)
	{
		for (Item item : getItems())
		{
			item.setAnchor(anchor);
			item.setWindow(this);
		}

		showAtLocation(anchor, Gravity.NO_GRAVITY, 0, 0);

		// http://github.com/ruqqq/WorldHeritageSite/blob/master/src/sg/ruqqq/WHSFinder/QuickActionWindow.java
		if (isShowing())
		{
			int yoff;
			final ViewGroup contentView = (ViewGroup) getContentView();
			contentView.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			final int blockHeight = contentView.getMeasuredHeight();
			// Display above post
			yoff = - blockHeight + mConfig.get(Config.ARROW_OFFSET, 0);
			int windowBackground = mConfig.get(Config.WINDOW_BACKGROUND_IF_BELOW, -1);
			if (windowBackground != -1)
			{
				contentView.setBackgroundResource(windowBackground);
			}
			update(anchor, 0, yoff + yOffset, -1, blockHeight);

			// Animation for all views
			int itemAnimation = mConfig.get(Config.ITEM_APPEAR_ANIMATION, -1);
			if (itemAnimation != -1)
			{
				Animation anim = AnimationUtils.loadAnimation(anchor.getContext(), itemAnimation);
				for (int i = 0; i < contentView.getChildCount(); i++)
				{
					View v = contentView.getChildAt(i);
					v.startAnimation(anim);
				}
			}
		}
	}
}