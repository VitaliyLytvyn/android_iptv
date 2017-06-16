package com.app.tv.mediacast.info;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.tv.mediacast.util.Constant;

import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;
import com.app.tv.mediacast.MyApplication;
import com.app.tv.mediacast.R;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataSubscriptionId;
import com.facebook.login.LoginManager;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.EMAIL;
import static com.app.tv.mediacast.util.Constant.FACEBOOK_DATA;
import static com.app.tv.mediacast.util.Constant.FACEBOOK_TOKEN;
import static com.app.tv.mediacast.util.Constant.LAST_NAME;
import static com.app.tv.mediacast.util.Constant.NAME;
import static com.app.tv.mediacast.util.Constant.PACKAGE_ID;
import static com.app.tv.mediacast.util.Constant.PASSWORD;

public class InfoCreateAccount extends AppCompatActivity
        implements FragmentFbLogin.OnFbLoginResultListener,
        FragmentInfoEmailDialog.OnContinueButtonListener {

    private String mPackageId;
    private String email ;
    private String pass ;
    private String name;
    private Button buttonCreateUser;

    AlertDialog mDialog = null;
    private boolean isRenew = false;

    Call<DataSubscriptionId> call;

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
        }
        finishProgress();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MyApplication) getApplication()).getNetComponent().inject(this);

        if(savedInstanceState == null){
            mPackageId = getIntent().getStringExtra(Constant.PACKAGE_ID);
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
        }else {
            mPackageId = savedInstanceState.getString(Constant.PACKAGE_ID, null);
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW, false);
        }

        checkIfActivityNeeded();

        setContentView(R.layout.info_create_account_layout);

        if(findViewById(R.id.frame_for_login_fb_button) != null){
            if(savedInstanceState == null){
                FragmentFbLogin fbButtonFragment = FragmentFbLogin.newInstance(true);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame_for_login_fb_button, fbButtonFragment).commit();
            }
        }

        TextView headerText = (TextView) findViewById(R.id.textViewHead);
        headerText.setText(getString(R.string.signup_to_start_free_week));

        final EditText editTextEmail = (EditText) findViewById(R.id.emailEditTextInfo);
        final EditText editTextPassword = (EditText) findViewById(R.id.editText_passwordInfo);
        final EditText editTextName = (EditText) findViewById(R.id.editTextName);
        buttonCreateUser = (Button) findViewById(R.id.button_create_info);
        buttonCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = editTextEmail.getText().toString();
                pass = editTextPassword.getText().toString();
                name = editTextName.getText().toString();
                //empty = true means some field is empty
                if (email.equals("") || pass.equals("") || name.equals("")) {
                    Toast.makeText(InfoCreateAccount.this, getString(R.string.fill_all_edittexts),
                            Toast.LENGTH_SHORT).show();
                    buttonCreateUser.setEnabled(true);
                    return;
                }

                //disable resending requests while in progress
                buttonCreateUser.setEnabled(false);
                //hide keyBoard
                Global.hideKeyBoard(InfoCreateAccount.this, v);

                //CHECK FOR ACCESS
                if (InternetConnection.isConnected(InfoCreateAccount.this)) {
                    startProgress();
                    Map<String, String> mapOfQueryData = new HashMap<>();

                    /**
                     *
                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                     *
                     */

                    //Create a retrofit call object
                    call = retrofit.create(Restapi.class).registerUser(Constant.getHeaders(), mapOfQueryData);

                    //Enqueue the call
                    call.enqueue(new Callback<DataSubscriptionId>() {
                        @Override
                        public void onResponse(Call<DataSubscriptionId> call, Response<DataSubscriptionId> response) {

                            finishProgress();
                            buttonCreateUser.setEnabled(true);
                            if (response.isSuccessful()) {

                                // use response data and do some fancy stuff :)
                                onOurServerResult(response.body(), false);

                            } else {
                                // parse the response body …
                                DataError error = Constant.parseError(retrofit, response);

                                if(error.getError() == null){
                                    Constant.toastIt(InfoCreateAccount.this, getString(R.string.user_info_fail_retry));
                                } else {
                                    Constant.getErrorString(InfoCreateAccount.this, error.getError());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DataSubscriptionId> call, Throwable t) {
                            finishProgress();
                            buttonCreateUser.setEnabled(true);
                            Constant.toastIt(InfoCreateAccount.this, getString(R.string.user_info_fail_retry));
                        }
                    });
                } else {
                    //no internet connection
                    Constant.toastIt(InfoCreateAccount.this, getString(R.string.no_internet_connection));
                }
            }
        });

        buttonCreateUser.setEnabled(true);

        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InfoCreateAccount.this, InfoMainActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    intent.putExtra(InfoMainActivity.RENEW, isRenew);
                    startActivity(intent);
                    InfoCreateAccount.this.finish();
                }
            });
        }

        //if FB LOGGED IN BUT WITHOUT EMAIL
        String rowData = Global.getSharedPreferences(this, Constant.KEY_RAW_FB_JSON);
        if(!rowData.equals(Constant.NULL_STRING) && FragmentFbLogin.isLoggedIn()){
            if(savedInstanceState == null){
                showEmailNeedDialog();
            }
        }
    }

    private void checkIfActivityNeeded(){

        if(mPackageId == null){
            //something went wrong go to beginning
            startActivity(new Intent(this, InfoMainActivity.class)
                    .putExtra(InfoMainActivity.RENEW, isRenew));
            finish();
        }
        String token = Global.getSharedPreferences(this, Constant.KEY_TOKEN_AUTH);
        String subscription_id = Global.getSharedPreferences(this, Constant.KEY_SUBSCRIPTION_ID);
        if(!token.equals(Constant.NULL_STRING) || !subscription_id.equals(Constant.NULL_STRING)){
            startActivity(InfoConfirmActivity.makeMyIntent(this, mPackageId, isRenew));
            finish();
        }
    }

    private void startProgress() {
        GlobalProgressDialog.show(this, getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private void onOurServerResult(DataSubscriptionId dataSubscriptionId, boolean isfbRegistered){
        buttonCreateUser.setEnabled(true);

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

                Intent myIntent = InfoConfirmActivity.makeMyIntent(InfoCreateAccount.this, mPackageId, isRenew);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                InfoCreateAccount.this.startActivity(myIntent);
                InfoCreateAccount.this.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Constant.PACKAGE_ID, mPackageId);
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFbLoginResult() {

        email = Global.getSharedPreferences(this, Constant.KEY_EMAIL);
        if(email.equals(Constant.NULL_STRING) || email.equals("")){
            showEmailNeedDialog();
        } else {
            sendFacebookDataOnServer();
        }
    }

    private void showEmailNeedDialog(){
        android.app.FragmentTransaction ft =  getFragmentManager().beginTransaction();
        android.app.Fragment prev = getFragmentManager().findFragmentByTag("emailDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = FragmentInfoEmailDialog.newInstance(this);
        newFragment.show(ft, "emailDialog");
    }

    private void sendFacebookDataOnServer(){

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

        //CHECK FOR ACCESS
        if (InternetConnection.isConnected(InfoCreateAccount.this)) {
            startProgress();
            buttonCreateUser.setEnabled(false);
            Map<String, String> mapOfQueryData = new HashMap<>();

            /**
             *
             * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
             *
             */

            //Create a retrofit call object
            call = retrofit.create(Restapi.class).registerUserByFB(Constant.getHeaders(), mapOfQueryData);

            //Enqueue the call
            call.enqueue(new Callback<DataSubscriptionId>() {
                @Override
                public void onResponse(Call<DataSubscriptionId> call, Response<DataSubscriptionId> response) {

                    finishProgress();
                    buttonCreateUser.setEnabled(true);
                    if (response.isSuccessful()) {
                        // use response data and do some fancy stuff :)
                        onOurServerResult(response.body(), true);

                    } else {
                        //registration failed, so Log out FB
                        LoginManager.getInstance().logOut();
                        // parse the response body …
                        DataError error = Constant.parseError(retrofit, response);
                        if(error.getError() == null){
                            Constant.toastIt(InfoCreateAccount.this, getString(R.string.user_info_fail_retry));
                        } else {
                            Constant.getErrorString(InfoCreateAccount.this, error.getError());
                        }
                    }
                }

                @Override
                public void onFailure(Call<DataSubscriptionId> call, Throwable t) {
                    finishProgress();
                    //registration failed, so Log out FB
                    LoginManager.getInstance().logOut();
                    buttonCreateUser.setEnabled(true);
                    Constant.toastIt(InfoCreateAccount.this, getString(R.string.user_info_fail_retry));
                }
            });
        } else {
            //no internet connection
            Constant.toastIt(InfoCreateAccount.this, getString(R.string.no_internet_connection));
        }
    }

    @Override
    public void onContinueButtonResult(String sEmail) {
        email = sEmail;
        sendFacebookDataOnServer();
    }

    public static Intent makeMyIntent(Context context, String package_id, boolean isRenew){
        Intent newIntent = new Intent(context, InfoCreateAccount.class);
        newIntent.putExtra(Constant.PACKAGE_ID, package_id);
        newIntent.putExtra(InfoMainActivity.RENEW, isRenew);
        return newIntent;
    }
}
