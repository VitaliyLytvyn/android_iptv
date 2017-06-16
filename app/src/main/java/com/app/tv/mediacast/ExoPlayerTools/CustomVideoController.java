package com.app.tv.mediacast.ExoPlayerTools;

/**
 * Created by AnZyuZya on 01-Apr-15.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;

import com.app.tv.mediacast.R;

public class CustomVideoController extends MediaController {
    public static boolean full = false;

    private View.OnClickListener buttonListener;
    private ImageButton fullScreenBtn;
    Context mContext;

    public CustomVideoController(Context context, View.OnClickListener buttonListener) {
        super(new ContextThemeWrapper(context, R.style.VideoPlayerTheme));
        this.buttonListener = buttonListener;
        mContext = context;
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.RIGHT | Gravity.TOP;
        frameParams.setMargins(12, 14, 10, 12);

        View v = AddFullScreenBtn();
        addView(v, frameParams);
    }

//    @Override
//    public View makeControllerView() {
//
//
//        return null;
//    }


    private View AddFullScreenBtn() {
        fullScreenBtn = new ImageButton(mContext);
        fullScreenBtn.setImageResource(R.drawable.ic_media_fullscreen_stretch);
        fullScreenBtn.setBackgroundColor(Color.TRANSPARENT);
        fullScreenBtn.setOnClickListener(buttonListener);
        return fullScreenBtn;
    }

    public View getButton(){
        return fullScreenBtn;
    }
}
