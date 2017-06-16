package com.app.tv.mediacast.ExoPlayerTools;

/**
 * Created by AnZyuZya on 01-Apr-15.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.app.tv.mediacast.R;

import java.lang.ref.WeakReference;

public class CustomVideoControllerLand extends MediaController {

    private View.OnClickListener buttonListener;
    private ImageButton fullScreenBtn;
    private Context mContext;

    private ImageView mChannelIcon;
    private TextView mChannelText;
    private TextView mChanneProgrammlText;
    private View mViewControlBarFullScreen;
    private FrameLayout.LayoutParams frameParams;

    public CustomVideoControllerLand(Context context, View.OnClickListener buttonListener) {
        super(new ContextThemeWrapper(context, R.style.VideoPlayerThemeLand));

        this.buttonListener = buttonListener;
        mContext = context;

        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewControlBarFullScreen = inflater.inflate(R.layout.controlls_bar_full_screen, null);

        mChannelIcon = (ImageView)mViewControlBarFullScreen.findViewById(R.id.imgIconActionBarMy);
        mChannelText = (TextView) mViewControlBarFullScreen.findViewById(R.id.txtChannelNameControlsBarMy);
        mChanneProgrammlText = (TextView) mViewControlBarFullScreen.findViewById(R.id.txtChannelProgrammControlsBarMy);

        View viewButton = mViewControlBarFullScreen.findViewById(R.id.imgButtonControlsBarMy);
        viewButton.setOnClickListener(buttonListener);

        frameParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        frameParams.gravity = Gravity.TOP;
    }

    public void setMyControlsFullScreenVisability(boolean isArchive){
        int visability;
        if(isArchive){
            visability = INVISIBLE;
        } else {
            visability = VISIBLE;
        }
        mChannelIcon.setVisibility(visability);
        mChannelText.setVisibility(visability);
        mChanneProgrammlText.setVisibility(visability);
    }

    public void setMyChannelText(String text) {
        mChannelText.setText(text);
    }

    public void setMyChanneProgrammlText(String text) {
        mChanneProgrammlText.setText(text);
    }

    public void setMyChannelIcon(Bitmap icon) {
        mChannelIcon.setImageBitmap(icon);
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

//            FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
//                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//            frameParams.gravity = Gravity.RIGHT | Gravity.TOP;
//            frameParams.setMargins(12, 14, 10, 12);
//            View v = AddFullScreenBtn();
//            addView(v, frameParams);////

            //((ViewGroup) view.getParent()).removeView(view);
            addView(mViewControlBarFullScreen, frameParams);
    }


//    private View AddFullScreenBtn() {
//        fullScreenBtn = new ImageButton(mContext);
//        fullScreenBtn.setImageResource(R.drawable.ic_media_fullscreen_shrink);
//        fullScreenBtn.setBackgroundColor(Color.TRANSPARENT);
//        fullScreenBtn.setOnClickListener(buttonListener);
//        return fullScreenBtn;
//    }

//    public View getButton(){
//        return fullScreenBtn;
//    }

}
