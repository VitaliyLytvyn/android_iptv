package com.app.tv.mediacast.info;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


//import com.app.tv.mediacast.databinding.FbButtonLayoutBinding;
import com.app.tv.mediacast.R;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by skyver on 12/10/16.
 */

public class FragmentFbLogin extends android.support.v4.app.Fragment{

    OnFbLoginResultListener mFbLoginResultListener;
    private CallbackManager callbackManager;
    private LoginButton fbLoginButton;

    public boolean isCreate;

    private static final String ADD_PARAMETERS_FROM_FB = ",cover,name,age_range,link,gender,locale,picture,timezone,updated_time,verified";

    public interface OnFbLoginResultListener{
        void onFbLoginResult();
    }

    public static FragmentFbLogin newInstance(boolean isCreate) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isCreate", isCreate);

        FragmentFbLogin fragment = new FragmentFbLogin();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            this.isCreate = bundle.getBoolean("isCreate");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mFbLoginResultListener = (OnFbLoginResultListener)context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " must implement OnFbLoginResultListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Init before setContentView!
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        readBundle(getArguments());

        View v;
        if(isCreate){
            v = inflater.inflate(R.layout.fb_button_register_layout, container, false);
        } else {
            v = inflater.inflate(R.layout.fb_button_login_layout, container, false);
        }

        setFBLoginCallbacks(v);

        //observe profile changes
        new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    final Profile oldProfile,
                    final Profile currentProfile) {
                updateUI();
            }
        };

        //adjust fb button size and icon
        tryAdjustFbButton();

        return v;
    }

    private void tryAdjustFbButton(){
        //float fbIconScale = 1.45F;
        float fbIconScale = 1F;
        Drawable drawable = getContext().getResources().getDrawable(
                com.facebook.R.drawable.com_facebook_button_icon);
        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*fbIconScale),
                (int)(drawable.getIntrinsicHeight()*fbIconScale));
        fbLoginButton.setCompoundDrawables(drawable, null, null, null);
        fbLoginButton.setCompoundDrawablePadding(getActivity().getResources().
                getDimensionPixelSize(R.dimen.fb_btn_margin_override_textpadding));
        fbLoginButton.setPadding(
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_btn_margin_override_lr),
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_btn_margin_override_top),
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_btn_margin_override_lr),
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_btn_margin_override_bottom));
    }

    private void setFBLoginCallbacks(View v) {
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton = (LoginButton) v.findViewById(R.id.login_fb_button);

        fbLoginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        fbLoginButton.setFragment(this);

        // Callback registration
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                // App code
                Global.replaceSharedPreferences(getActivity(),
                        Constant.KEY_FB_TOKEN, loginResult.getAccessToken().getToken());

                //Profile profile = Profile.getCurrentProfile();
                //updateUI();
                getDataFromFB();
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(
                        getActivity(),
                        R.string.cancel,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(final FacebookException exception) {
                // App code
                Toast.makeText(getActivity(), getString(R.string.no_internet_connection),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDataFromFB(){

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code

                        String sFname = "";
                        String sLname = "";
                        String sEmail = "";
                        try {
                            if(object.has("first_name")){
                                sFname = object.getString("first_name");
                                Global.replaceSharedPreferences(getActivity(),
                                        Constant.KEY_NAME, sFname);

                            }
                            if(object.has("last_name")){
                                sLname = object.getString("last_name");
                                Global.replaceSharedPreferences(getActivity(),
                                        Constant.KEY_SURNAME, sLname);

                            }
                            if(object.has("email")){
                                sEmail = object.getString("email");
                                Global.replaceSharedPreferences(getActivity(),
                                        Constant.KEY_EMAIL, sEmail);

                            }

                            Global.replaceSharedPreferences(getActivity(),
                                    Constant.KEY_RAW_FB_JSON, object.toString());

                            mFbLoginResultListener.onFbLoginResult();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Log out FB 'cause something wrong
                            LoginManager.getInstance().logOut();
                        }

                    }
                });
        Bundle parameters = new Bundle();
        //INFO REQUESTED FROM FB
        parameters.putString("fields", "id,first_name,last_name,email" + ADD_PARAMETERS_FROM_FB);
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public static boolean isLoggedIn() {
        AccessToken accesstoken = AccessToken.getCurrentAccessToken();
        return !(accesstoken == null || accesstoken.getPermissions().isEmpty());
    }

    private void updateUI() {
        Profile profile = Profile.getCurrentProfile();
        if (profile != null) {

            //profilePictureView.setProfileId(profile.getId());
            //userNameView.setText(String.format("%s %s",profile.getFirstName(), profile.getLastName()));
        } else {
            //profilePictureView.setProfileId(null);
            //userNameView.setText(getString(R.string.welcome));
        }
    }
}

