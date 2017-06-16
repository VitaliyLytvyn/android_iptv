package com.app.tv.mediacast;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.tv.mediacast.ExoPlayerTools.CustomVideoController;
import com.app.tv.mediacast.ExoPlayerTools.CustomVideoControllerLand;
import com.app.tv.mediacast.info.InfoMainActivity;
import com.app.tv.mediacast.info.InfoSelectPriceActivity;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataUrl;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.metadata.TxxxMetadata;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.app.tv.mediacast.ExoPlayerTools.DemoPlayer;
import com.app.tv.mediacast.ExoPlayerTools.DemoPlayer.RendererBuilder;
import com.app.tv.mediacast.ExoPlayerTools.DemoUtil;
import com.app.tv.mediacast.ExoPlayerTools.EventLogger;
import com.app.tv.mediacast.ExoPlayerTools.HlsRendererBuilder;
import com.app.tv.mediacast.ExoPlayerTools.UnsupportedDrmException;
import com.squareup.picasso.Target;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.ERROR_SUBSCRIPTION_NOT_FOUND;


public class ActivityExoPlayer extends Activity implements SurfaceHolder.Callback, View.OnClickListener,
        DemoPlayer.Listener, DemoPlayer.Id3MetadataListener {

    public static final String CONTENT_TYPE_EXTRA = "content_type";
    public static final String CONTENT_ID_EXTRA = "content_id";

    private static final String TAG = "PlayerActivity";

    private EventLogger eventLogger;
    private CustomVideoController mc;
    private CustomVideoControllerLand mcRoot;
    private View shutterView;
    private VideoSurfaceView surfaceView;
    private View root;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;
    private boolean enableBackgroundAudio;

    private Uri contentUri;
    private int contentType;
    private String contentId;

    //vars
    private String url;
    private String title;
    private int lang;
    private String channelId;
    private boolean full = false;
    private boolean isArchive = false;
    private int positionInChannelList;
    private Target loadtarget;

    private LinearLayout myGallery;
    private FrameLayout surfaceWrapper;
    private FrameLayout controlsWrapper;

    //dimens
    private float controlsWeight;
    private float surfaceWeight;

    private ImageView imageIcon;
    private ImageView imageArchive;
    private TextView textTitle;
    private Bitmap mIconBitmap;

    private android.app.AlertDialog mDialog;

    Call<DataUrl> call;

    @Inject
    Retrofit retrofit;

    @Override
    public void onStop() {
        if(call != null && !call.isCanceled()){
            call.cancel();
            call = null;
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_exo_player);

        if(getActionBar() != null){
            getActionBar().setDisplayShowCustomEnabled(true);
        }
        getActionBar().setCustomView(R.layout.actionbar_my_constrlayout);
        getActionBar().setIcon(null);
        getActionBar().setLogo(null);
        getActionBar().setDisplayUseLogoEnabled(false);

        imageIcon = (ImageView) findViewById(R.id.imgIconActionBarMy);
        textTitle = (TextView) findViewById(R.id.txtTitleActionBarMy);
        imageArchive = (ImageView) findViewById(R.id.imgArchiveActionBarMy);
        imageArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToArchiveActivity();
            }
        });


        //get dimens according to screen density
        //surface
        TypedValue surfaceOutValue = new TypedValue();
        getResources().getValue(R.dimen.layout_weight_surface, surfaceOutValue, true);
        surfaceWeight = surfaceOutValue.getFloat();

        //controls
        TypedValue controlsOutValue = new TypedValue();
        getResources().getValue(R.dimen.layout_weight_controls, controlsOutValue, true);
        controlsWeight = controlsOutValue.getFloat();

        Intent intent = getIntent();
        url = intent.getStringExtra("Url");

        title = intent.getStringExtra("ChannelName");
        lang = intent.getIntExtra("Lang", -1);
        channelId = intent.getStringExtra("ChannelId");
        isArchive = intent.getBooleanExtra("IsArchive", false);//show controls
        positionInChannelList = intent.getIntExtra("Position", -1);//show controls

        if (savedInstanceState != null) {
            url = savedInstanceState.getString("Url");
            title = savedInstanceState.getString("Title");
        }

        if(title != null){
            textTitle.setText(" " + title);
        }

        //new getChannelIcon
        getChannelIcon();

        contentUri = intent.getData();
        contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA, DemoUtil.TYPE_OTHER);
        contentId = intent.getStringExtra(CONTENT_ID_EXTRA);

        //Horizontal Scroll view
        setUpHorizontalView();
        root = findViewById(R.id.root);

        shutterView = findViewById(R.id.shutter);
        surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleControlsVisibility();
            }
        });
        surfaceWrapper = (FrameLayout) findViewById(R.id.surface_wrapper);
        surfaceView.getHolder().addCallback(this);

        View.OnClickListener buttonListenerHide = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //indicator that its fullscreen
                full = true;

                //hide actionbar
                getActionBar().hide();

                //hide gallery
                if (myGallery != null) {
                    myGallery.setVisibility(View.GONE);
                }

                //disable surface controls
                if (mc != null) {
                    mc.hide();
                    mc.setVisibility(View.GONE);
                    mc.setEnabled(false);
                }

                //enable rootView controls
                if (mcRoot != null) {
                    mcRoot.show();
                    mcRoot.setVisibility(View.VISIBLE);
                    mcRoot.setEnabled(true);

                    mcRoot.setMyControlsFullScreenVisability(isArchive);
                    if(!isArchive){
                        mcRoot.setMyChannelText(title);
                        mcRoot.setMyChannelIcon(mIconBitmap);
                    }
                }

                //change weight of layout
                if (surfaceWrapper != null && controlsWrapper != null) {
                    surfaceWrapper.setLayoutParams(
                            new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT, 0));
                    controlsWrapper.setLayoutParams(
                            new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT, 1));
                }
            }
        };

        View.OnClickListener buttonListenerRestore = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //indicator that its not fullscreen
                full = false;

                //show actionbar
                getActionBar().show();

                //enable surface controls
                if (mc != null) {
                    mc.show();
                    mc.setVisibility(View.VISIBLE);
                    mc.setEnabled(true);
                }

                //disable root controls
                if (mcRoot != null) {
                    mcRoot.hide();
                    mcRoot.setVisibility(View.GONE);
                    mcRoot.setEnabled(false);

                }

                // if portrait - restore Gallery and set weight according to dimens
                if (Global.getScreenOrientation(ActivityExoPlayer.this)
                        == Configuration.ORIENTATION_PORTRAIT) {
                    //show gallery
                    if (myGallery != null) {
                        myGallery.setVisibility(View.VISIBLE);
                    }

                    //change weight of layout
                    if (surfaceWrapper != null && controlsWrapper != null) {
                        surfaceWrapper.setLayoutParams(
                                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT, surfaceWeight));
                        controlsWrapper.setLayoutParams(
                                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT, controlsWeight));
                    }
                    //if landscape - set 0.35f weight to controls
                } else if (Global.getScreenOrientation(ActivityExoPlayer.this)
                        == Configuration.ORIENTATION_LANDSCAPE) {
                    if (Global.isTablet(ActivityExoPlayer.this)) {
                        //change weight of layout for tablet
                        if (surfaceWrapper != null && controlsWrapper != null) {
                            surfaceWrapper.setLayoutParams(
                                    new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT, 0.15f));
                            controlsWrapper.setLayoutParams(
                                    new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT, 0.85f));
                        }
                    } else {
                        //change weight of layout for phone
                        if (surfaceWrapper != null && controlsWrapper != null) {
                            surfaceWrapper.setLayoutParams(
                                    new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT, 0.35f));
                            controlsWrapper.setLayoutParams(
                                    new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT, 0.65f));
                        }
                    }
                }
            }
        };


        mc = new CustomVideoController(ActivityExoPlayer.this, buttonListenerHide);
        mc.setAnchorView(root);
        mcRoot = new CustomVideoControllerLand(ActivityExoPlayer.this, buttonListenerRestore);
        mcRoot.setAnchorView(root);

        setViewControlls(isArchive);

        DemoUtil.setDefaultCookieManager();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        ///same url - return
        if(url != null && url.equals(intent.getStringExtra("Url"))){
            preparePlayer();
            return;
        }
        url = intent.getStringExtra("Url");

        channelId = intent.getStringExtra("ChannelId");
        title = intent.getStringExtra("ChannelName");
        lang = intent.getIntExtra("Lang", -1);
        isArchive = intent.getBooleanExtra("IsArchive", false);////////show controls
        positionInChannelList = intent.getIntExtra("Position", -1);////////show controls

        getChannelIcon();

        if(title != null){
            textTitle.setText(" " + title);
        }

        contentUri = intent.getData();
        contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA, DemoUtil.TYPE_OTHER);
        contentId = intent.getStringExtra(CONTENT_ID_EXTRA);

        setViewControlls(isArchive);

        playerNeedsPrepare = true;
        //player.seekTo(0);
        releasePlayer();
        playerPosition = 0;
        preparePlayer();

    }

    private void setViewControlls(boolean isArchive){
        int visability;
        if(! isArchive)
            visability = View.INVISIBLE;
         else
            visability = View.VISIBLE;

        int topId;
        topId = getResources().getIdentifier("mediacontroller_progress", "id", "android");
        if(topId != 0){
            mc.findViewById(topId).setVisibility(visability);
            mcRoot.findViewById(topId).setVisibility(visability);
        }
        topId = getResources().getIdentifier("time", "id", "android");
        if(topId != 0){
            mc.findViewById(topId).setVisibility(visability);
            mcRoot.findViewById(topId).setVisibility(visability);
        }
        topId = getResources().getIdentifier("time_current", "id", "android");
        if(topId != 0){
            mc.findViewById(topId).setVisibility(visability);
            mcRoot.findViewById(topId).setVisibility(visability);
        }
        topId = getResources().getIdentifier("rew", "id", "android");
        if(topId != 0){
            mc.findViewById(topId).setVisibility(visability);
            mcRoot.findViewById(topId).setVisibility(visability);
        }
        topId = getResources().getIdentifier("ffwd", "id", "android");
        if(topId != 0){
            mc.findViewById(topId).setVisibility(visability);
            mcRoot.findViewById(topId).setVisibility(visability);
        }
        topId = getResources().getIdentifier("pause", "id", "android");
        if(topId != 0){
            mc.findViewById(topId).setVisibility(visability);
            mcRoot.findViewById(topId).setVisibility(visability);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (player == null) {
            preparePlayer();
        } else if (player != null) {
            player.setBackgrounded(false);
        }
    }

    @Override
    public void onPause() {

        if (enableBackgroundAudio) {
            releasePlayer();
        } else {
            player.setBackgrounded(true);
        }
        shutterView.setVisibility(View.VISIBLE);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    // OnClickListener methods
    @Override
    public void onClick(View view) {
    }

    // Internal methods

    private RendererBuilder getRendererBuilder() {
        String userAgent = DemoUtil.getUserAgent(this);
        return new HlsRendererBuilder(userAgent, contentUri.toString(), contentId);
    }

    private void preparePlayer() {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.seekTo(playerPosition);
            player.addListener(this);
            player.setMetadataListener(this);

            playerNeedsPrepare = true;

            mc.setMediaPlayer(player.getPlayerControl());
            mc.setEnabled(true);

            if(mc.getParent() != null){
                ((ViewGroup) mc.getParent()).removeView(mc);
            }

            controlsWrapper = (FrameLayout) findViewById(R.id.controls_wrapper);
            controlsWrapper.addView(mc);// show buttons and scroll

            mc.setVisibility(View.VISIBLE);
            mc.setEnabled(true);

            mcRoot.setMediaPlayer(player.getPlayerControl());
            mcRoot.setEnabled(false);
            mcRoot.setVisibility(View.GONE);

            eventLogger = new EventLogger();
            eventLogger.startSession();

            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }

        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(true);
    }

    private void releasePlayer() {

        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    // DemoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
    }

    @Override
    public void onError(Exception e) {

        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId = unsupportedDrmException.reason == UnsupportedDrmException.REASON_NO_DRM
                    ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme
                    : R.string.drm_error_unknown;
            Toast.makeText(getApplicationContext(), stringId, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;

        if (player == null)
            preparePlayer();

        showControls();

    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {

        shutterView.setVisibility(View.GONE);
        surfaceView.setVideoWidthHeightRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    // User controls

    private void toggleControlsVisibility() {

        if (mcRoot.isShowing()) {
            mcRoot.hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mcRoot.show();
    }

    // DemoPlayer.MetadataListener implementation

    @Override
    public void onId3Metadata(Map<String, Object> metadata) {

        for (int i = 0; i < metadata.size(); i++) {
            if (metadata.containsKey(TxxxMetadata.TYPE)) {
                TxxxMetadata txxxMetadata = (TxxxMetadata) metadata.get(TxxxMetadata.TYPE);
                Log.i(TAG, String.format("ID3 TimedMetadata: description=%s, value=%s",
                        txxxMetadata.description, txxxMetadata.value));
            }
        }
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (Global.getScreenOrientation(this) == Configuration.ORIENTATION_PORTRAIT) {
            if (!full) {
                //hide gallery
                if (myGallery != null) {
                    myGallery.setVisibility(View.VISIBLE);
                }

                //disable root controls
                if (mcRoot != null) {
                    mcRoot.hide();
                    mcRoot.setVisibility(View.GONE);
                    mcRoot.setEnabled(false);
                }

                //change weight of layout
                if (surfaceWrapper != null && controlsWrapper != null) {
                    surfaceWrapper.setLayoutParams(
                            new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT, surfaceWeight));
                    controlsWrapper.setLayoutParams(
                            new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT, controlsWeight));
                }
            }
        }
        if (Global.getScreenOrientation(this) == Configuration.ORIENTATION_LANDSCAPE) {
            if (!full) {
                //hide gallery
                if (myGallery != null) {
                    myGallery.setVisibility(View.GONE);
                }

                //disable root controls
                if (mcRoot != null) {
                    mcRoot.hide();
                    mcRoot.setVisibility(View.GONE);
                    mcRoot.setEnabled(false);
                }

                if (Global.isTablet(this)) {
                    //change weight of layout for tablet
                    if (surfaceWrapper != null && controlsWrapper != null) {
                        surfaceWrapper.setLayoutParams(
                                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT, 0.15f));
                        controlsWrapper.setLayoutParams(
                                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT, 0.85f));
                    }
                } else {
                    //change weight of layout for phone
                    if (surfaceWrapper != null && controlsWrapper != null) {
                        surfaceWrapper.setLayoutParams(
                                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT, 0.35f));
                        controlsWrapper.setLayoutParams(
                                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT, 0.65f));
                    }
                }
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (player != null) {
            player.blockingClearSurface();
        }
    }


    private void setUpHorizontalView() {

        myGallery = (LinearLayout) findViewById(R.id.channelsScrollView);

        //if activity started in landscape - hide gallery and set the custom weight
        if (Global.getScreenOrientation(this) == Configuration.ORIENTATION_LANDSCAPE) {
            myGallery.setVisibility(View.GONE);
            //change weight of layout

        }
        populateImages();
    }

    private void populateImages() {

        final List<String> urls = new ArrayList<>();

        if (lang == -1) {
            Log.e("PopulateImages   ", "Language didn't set properly: lang =  " + Integer.toString(lang));
        }

        //getting list of urls
        for (int i = 0; i < ActivityLoading.channelsByLangsList.get(lang).getChannels().size(); i++) {

            urls.add(Constant.HOST
                    + ActivityLoading.channelsByLangsList.get(lang).getChannels().get(i).getLogo());
        }

        //add views to horizontal scrollView by the amount of urls
        myGallery = (LinearLayout) findViewById(R.id.channelsScrollView);
        for (int i = 0; i < urls.size(); i++) {
            myGallery.addView(insertPhoto(urls.get(i), i));
        }
    }

    public View insertPhoto(String path, final int listPosition) {

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setGravity(Gravity.CENTER);
        final SquaredImageView squaredImageView = new SquaredImageView(getApplicationContext());
        squaredImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        squaredImageView.setPadding(12, 12, 12, 12);
        Picasso.with(this)
                .load(path)
                .resize((int) getResources().getDimension(R.dimen.crop_placeholder_size), (int) getResources().getDimension(R.dimen.crop_placeholder_size))
                .centerInside()
                .into(squaredImageView);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = ActivityLoading.channelsByLangsList.get(lang).getChannels().get(listPosition).getId();
                title = ActivityLoading.channelsByLangsList.get(lang).getChannels().get(listPosition).getName();

                //same channel - do nothing
                if(channelId.equals(id))
                    return;

                //new AsyncTask to load channel and start new activity
                getChannelLive(id, listPosition);
            }
        });
        layout.addView(squaredImageView);
        return layout;
    }

    @Override
    public void onBackPressed() {

        if (player != null) {
            player.release();
        }
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_calendar) {
            goToArchiveActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToArchiveActivity(){
            Intent myIntent = new Intent(this, ActivityCalendar.class);
            myIntent.putExtra("ChannelId", channelId);
            myIntent.putExtra("Lang", lang);
            myIntent.putExtra("ChannelName", title);
            myIntent.putExtra("Url", url);
            myIntent.putExtra("Position", positionInChannelList);
            this.startActivity(myIntent);
    }


    //get image of the channel
    private void getChannelIcon(){

        if(positionInChannelList != -1){
            String path = Constant.HOST
                + ActivityLoading.channelsByLangsList.get(lang)
                    .getChannels().get(positionInChannelList).getLogo();

            if (loadtarget == null) loadtarget = new Target() {
                 @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // do something with the Bitmap
                    setIcon(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            Picasso.with(this).load(path).into(loadtarget);

        }
    }

    private void setIcon(Bitmap res){

        if (res != null) {
            int width = res.getWidth();
            int height = res.getHeight();

            int newWidth = 80;
            int newHeight = 80;

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            Bitmap resizedBitmap = Bitmap.createBitmap(res, 0, 0, width, height, matrix, true);

            mIconBitmap = resizedBitmap;
            imageIcon.setImageBitmap(resizedBitmap);

        }
    }

    private void getChannelLive(final String id, final int listPosition){

        //CHECK FOR ACCESS
        if (InternetConnection.isConnected(this)) {

            //Create a retrofit call object
            call = retrofit.create(Restapi.class).getTranslateUrl(Constant.getHeaders(), id);

            //Enqueue the call
            call.enqueue(new Callback<DataUrl>() {
                @Override
                public void onResponse(Call<DataUrl> call, Response<DataUrl> response) {
                    GlobalProgressDialog.dismiss();
                    if (response.isSuccessful()) {
                        // use response data and do some fancy stuff :)

                        String urlToGo = response.body().getUrl();

                        Class<?> playerActivityClass = ActivityExoPlayer.class;
                        Intent mpdIntent = new Intent(ActivityExoPlayer.this, playerActivityClass)
                                .setData(Uri.parse(urlToGo))
                                .putExtra(ActivityExoPlayer.CONTENT_ID_EXTRA, Integer.toString(urlToGo.hashCode()))
                                .putExtra(ActivityExoPlayer.CONTENT_TYPE_EXTRA, DemoUtil.TYPE_HLS);

                        mpdIntent.putExtra("Url", urlToGo);
                        mpdIntent.putExtra("ChannelName", title);
                        mpdIntent.putExtra("Lang", lang);
                        mpdIntent.putExtra("ChannelId", id);
                        mpdIntent.putExtra("Position", listPosition);
                        startActivity(mpdIntent);

                    } else {
                        // parse the response body …
                        DataError error = Constant.parseError(retrofit, response);
                        if(error.getError() == null){
                            Constant.toastIt(ActivityExoPlayer.this, getString(R.string.user_info_fail_retry));
                        } else if(error.getError().equals(ERROR_SUBSCRIPTION_NOT_FOUND)){

                            showNoSubscriptionDialog();
                        }else {
                            Constant.getErrorString(ActivityExoPlayer.this, error.getError());
                        }
                    }
                }

                @Override
                public void onFailure(Call<DataUrl> call, Throwable t) {
                    GlobalProgressDialog.dismiss();
                    Constant.toastIt(ActivityExoPlayer.this, getString(R.string.user_info_fail_retry));
                }
            });
        } else {
            //no internet connection
            Constant.toastIt(ActivityExoPlayer.this, getString(R.string.no_internet_connection));
        }

    }

    private void showNoSubscriptionDialog(){
        //create no subscription notification dialog
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder
                (this, R.style.AlertDialogCustom);
        builder.setMessage(getString(R.string.no_subscription_dialog_message));

        builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                Intent intent = new Intent(ActivityExoPlayer.this, InfoSelectPriceActivity.class);
                intent.putExtra(InfoMainActivity.RENEW, true);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        mDialog = builder.create();
        mDialog.show();
    }
}
