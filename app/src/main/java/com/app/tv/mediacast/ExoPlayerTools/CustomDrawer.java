package com.app.tv.mediacast.ExoPlayerTools;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Burning Daylight on 13.02.2015.
 */
public class CustomDrawer extends DrawerLayout {

    public CustomDrawer(Context context) {
        super(context);
    }

    public CustomDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Considering the 2nd child is the ListView
        if (getDrawerLockMode(getChildAt(1)) == LOCK_MODE_LOCKED_OPEN) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }
}
