package com.app.tv.mediacast;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.tv.mediacast.info.FragmentFbLogin;
import com.app.tv.mediacast.info.InfoMainActivity;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataLoginUser;
import com.app.tv.mediacast.retrofit.data.DataLoginUserByFb;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;
import com.facebook.login.LoginManager;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.EMAIL;
import static com.app.tv.mediacast.util.Constant.FACEBOOK_TOKEN;
import static com.app.tv.mediacast.util.Constant.PASSWORD;
import static com.app.tv.mediacast.info.InfoMainActivity.RENEW;


public class ActivityAuthorization extends AppCompatActivity
        implements FragmentFbLogin.OnFbLoginResultListener{

    private Button buttonLogIn;
    private Button buttonBackHome;
    private EditText editTextEmail;
    private EditText editTextPass;
    private TextView textViewForgotPass;

    AlertDialog mDialog = null;
    private boolean isRenew = false;

    Call<DataLoginUser> call;
    Call<DataLoginUserByFb> call2;

    @Inject
    Retrofit retrofit;

    @Override
    public void onStop() {
        if(call != null && !call.isCanceled()){
            call.cancel();
            call = null;
        }
        if(call2 != null && !call2.isCanceled()){
            call2.cancel();
            call2 = null;
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        finishProgress();
        super.onStop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_authorization);

        ((MyApplication) getApplication()).getNetComponent().inject(this);

        if(findViewById(R.id.frame_for_login_fb_button) != null){
            if(savedInstanceState == null){
                FragmentFbLogin fbButtonFragment = FragmentFbLogin.newInstance(false);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame_for_login_fb_button, fbButtonFragment).commit();
            }
        }

        //CHECKING if this is renew case
        if(savedInstanceState != null){
            isRenew = savedInstanceState.getBoolean(RENEW, false);
        } else {
            isRenew = getIntent().getBooleanExtra(RENEW, false);
        }

        editTextEmail = (EditText) findViewById(R.id.emailEditTextInfo);
        editTextPass = (EditText) findViewById(R.id.editText_passwordInfo);
        textViewForgotPass = (TextView) findViewById(R.id.textViewForgotPassword);

        TextView headerText = (TextView) findViewById(R.id.textViewHead);
        headerText.setText(getString(R.string.authorization_text_login_with_existing_account));

        buttonLogIn = (Button) findViewById(R.id.button_create_info);
        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //disable resending requests while in progress
                buttonLogIn.setEnabled(false);
                //hide keyBoard
                Global.hideKeyBoard(ActivityAuthorization.this, v);

                //check if all fields are filled
                if (editTextEmail.getText().toString().equals("") && editTextPass.getText().toString().equals("")) {
                    Toast.makeText(ActivityAuthorization.this, getString(R.string.authorization_empty_fields_en), Toast.LENGTH_SHORT).show();
                } else if (editTextEmail.getText().toString().equals("")) {
                    Toast.makeText(ActivityAuthorization.this, getString(R.string.authorization_empty_email_en), Toast.LENGTH_SHORT).show();
                } else if (editTextPass.getText().toString().equals("")) {
                    Toast.makeText(ActivityAuthorization.this, getString(R.string.authorization_empty_pass_en), Toast.LENGTH_SHORT).show();
                } else {

                    //CHECK FOR ACCESS
                    if (InternetConnection.isConnected(ActivityAuthorization.this)) {
                        startProgress();

                        controllsEnable(false);

                        Map<String, String> mapOfData = new HashMap<>();


                        /**
                         *
                         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                         *
                         */

                        //Create a retrofit call object
                        call = retrofit.create(Restapi.class).loginUser(Constant.getHeaders(), mapOfData);

                        //Enqueue the call
                        call.enqueue(new Callback<DataLoginUser>() {
                            @Override
                            public void onResponse(Call<DataLoginUser> call, Response<DataLoginUser> response) {

                                finishProgress();
                                if (response.isSuccessful()) {
                                    // use response data and do some fancy stuff :)

                                    /**
                                     *
                                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                                     *
                                     */

                                    onRegisterUserSuccess();

                                } else {
                                    // parse the response body …
                                    DataError error = Constant.parseError(retrofit, response);
                                    if(error.getError() == null){
                                        Constant.toastIt(ActivityAuthorization.this, getString(R.string.user_info_fail_retry));
                                    } else {
                                        Constant.getErrorString(ActivityAuthorization.this, error.getError());
                                    }
                                    controllsEnable(true);
                                }
                            }

                            @Override
                            public void onFailure(Call<DataLoginUser> call, Throwable t) {
                                finishProgress();
                                Constant.toastIt(ActivityAuthorization.this, getString(R.string.user_info_fail_retry));
                                controllsEnable(true);
                            }
                        });
                    } else {
                        //no internet connection
                        Constant.toastIt(ActivityAuthorization.this, getString(R.string.no_internet_connection));
                    }

                }

                buttonLogIn.setEnabled(true);
            }
        });


        textViewForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ActivityAuthorization.this, ActivityForgotPassword.class);
                myIntent.putExtra(RENEW, isRenew);
                ActivityAuthorization.this.startActivity(myIntent);
            }
        });

        buttonBackHome = (Button)findViewById(R.id.button_back_home);
        tryAdjustHomeButton();
        buttonBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHomeActivity();
            }
        });
        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToHomeActivity();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(RENEW, isRenew);
        super.onSaveInstanceState(outState);
    }

    private void startProgress() {
        GlobalProgressDialog.show(this, getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private void goToHomeActivity(){
        startActivity(new Intent(ActivityAuthorization.this, InfoMainActivity.class).putExtra(RENEW, isRenew));
        ActivityAuthorization.this.finish();
    }

    private void tryAdjustHomeButton(){

        Drawable drawable2 = AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back_black_24dp );

        buttonBackHome.setCompoundDrawablesWithIntrinsicBounds(drawable2, null, null, null);
        buttonBackHome.setCompoundDrawablePadding(this.getResources().
                getDimensionPixelSize(R.dimen.back_home_btn_margin_override_textpadding));
        buttonBackHome.setPadding(
                this.getResources().getDimensionPixelSize(
                        R.dimen.google_btn_margin_override_lr),
                this.getResources().getDimensionPixelSize(
                        R.dimen.google_btn_margin_override_top),
                this.getResources().getDimensionPixelSize(
                        R.dimen.google_btn_margin_override_rr),
                this.getResources().getDimensionPixelSize(
                        R.dimen.google_btn_margin_override_bottom));
    }

    private void registerThisFBUser() {

        //CHECK FOR ACCESS
        if (InternetConnection.isConnected(ActivityAuthorization.this)) {
            startProgress();

            controllsEnable(false);

            String sToken = Global.getSharedPreferences(ActivityAuthorization.this, Constant.KEY_FB_TOKEN);

            Map<String, String> mapOfData = new HashMap<>();
            mapOfData.put(FACEBOOK_TOKEN, sToken );
            //Create a retrofit call object
            call2 = retrofit.create(Restapi.class).loginUserByFb(Constant.getHeaders(), mapOfData);

            //Enqueue the call
            call2.enqueue(new Callback<DataLoginUserByFb>() {
                @Override
                public void onResponse(Call<DataLoginUserByFb> call, Response<DataLoginUserByFb> response) {

                    finishProgress();
                    if (response.isSuccessful()) {

                        /**
                         *
                         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                         *
                         */

                        onRegisterUserSuccess();

                    } else {
                        //registration failed, so Log out FB
                        LoginManager.getInstance().logOut();
                        // parse the response body …
                        DataError error = Constant.parseError(retrofit, response);
                        if(error.getError() == null){
                            Constant.toastIt(ActivityAuthorization.this, getString(R.string.user_info_fail_retry));
                        } else {
                            Constant.getErrorString(ActivityAuthorization.this, error.getError());
                        }

                        controllsEnable(true);
                    }
                }

                @Override
                public void onFailure(Call<DataLoginUserByFb> call, Throwable t) {
                    finishProgress();
                    Constant.toastIt(ActivityAuthorization.this, getString(R.string.user_info_fail_retry));

                    //registration failed, so Log out FB
                    LoginManager.getInstance().logOut();
                    controllsEnable(true);
                }
            });
        } else {
            //no internet connection
            Constant.toastIt(ActivityAuthorization.this, getString(R.string.no_internet_connection));
        }
    }

    private void onRegisterUserSuccess(){

        Intent myIntent = new Intent(ActivityAuthorization.this, ActivityLoading.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityAuthorization.this.startActivity(myIntent);
        ActivityAuthorization.this.finish();
    }

    private void controllsEnable(boolean set){
        buttonLogIn.setEnabled(set);
        editTextEmail.setEnabled(set);
        editTextPass.setEnabled(set);
    }

    //callback method from Fb login fragment
    @Override
    public void onFbLoginResult() {
        registerThisFBUser();
    }
}

