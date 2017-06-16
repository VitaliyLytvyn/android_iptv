package com.app.tv.mediacast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.*;
import com.app.tv.mediacast.retrofit.data.Channel;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.InternetConnection;
import com.facebook.login.LoginManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.ERROR_UNAUTHORIZED;


public class ActivityLoading extends AppCompatActivity{

    AlertDialog mDialog;

    public static ArrayList<DataChannelsByLang> channelsByLangsList;
    private DataTranslateLanguages dataTranslateLanguages;

    Call<DataAccess> call;
    Call<DataTranslateLanguages> call2;
    Call<DataChannelsByLang> call3;

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
        if(call3 != null && !call3.isCanceled()){
            call3.cancel();
            call3 = null;
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MyApplication) getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_loading);

        channelsByLangsList = new ArrayList<>(3);
        checkAccess();

    }

    private void checkAccess(){
        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(this)) {
            //no internet connection
            showNoInternetDialogAndFinish();
            return;
        }

        //Create a retrofit call object
        call = retrofit.create(Restapi.class).checkUserAccess(Constant.getHeaders());

        //Enqueue the call
        call.enqueue(new Callback<DataAccess>() {
            @Override
            public void onResponse(Call<DataAccess> call, Response<DataAccess> response) {

                if (response.isSuccessful()) {

                    // use response data and do some fancy stuff :)
                    DataAccess dataAccess = response.body();

                    /**
                     *
                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                     *
                     */

                    getChannelsLang();

                } else {

                    // parse the response body …
                    DataError error = Constant.parseError(retrofit, response);

                    if(response.code() == 401 || error.getError().equalsIgnoreCase(ERROR_UNAUTHORIZED)){
                        goToAuthorizationActivity();
                    } else if (error.getError().equalsIgnoreCase(Constant.ERROR_SUBSCRIPTION_NOT_FOUND)) {
                        Global.replaceSharedPreferences(ActivityLoading.this,
                                Constant.KEY_ACCESS, Constant.ACCESS_DENIED);
                        getChannelsLang();
                    } else{
                        showSomethingBadDialogAndFinish();
                    }
                }
            }

            @Override
            public void onFailure(Call<DataAccess> call, Throwable t) {
                showSomethingBadDialogAndFinish();
            }
        });

    }

    private void getChannelsLang(){

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(this)) {
            //no internet connection
            showNoInternetDialogAndFinish();
            return;
        }

        //Create a retrofit call object
        call2 = retrofit.create(Restapi.class).getTranslateLanguages(Constant.getHeaders());

        //Enqueue the call
        call2.enqueue(new Callback<DataTranslateLanguages>() {
            @Override
            public void onResponse(Call<DataTranslateLanguages> call, Response<DataTranslateLanguages> response) {

                if (response.isSuccessful()) {
                    // use response data and do some fancy stuff :)
                    dataTranslateLanguages = response.body();
                    getChannelSetsByLanguageUa();

                } else {
                    showSomethingBadDialogAndFinish();
                }
            }

            @Override
            public void onFailure(Call<DataTranslateLanguages> call, Throwable t) {
                showSomethingBadDialogAndFinish();
            }
        });

    }

    private void getChannelSetsByLanguageUa(){
        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(this)) {
            //no internet connection
            showNoInternetDialogAndFinish();
            return;
        }

        if(dataTranslateLanguages.getUa() == null){
            Log.e(ActivityLoading.class.getName(), "No UA language in response");
            //getChannelSetsByLanguageRu();
            return;
        }
        //Create a retrofit call object
        call3 = retrofit.create(Restapi.class).getChannelsByLang(Constant.getHeaders(), Constant.UA_TAG);

        //Enqueue the call
        call3.enqueue(new Callback<DataChannelsByLang>() {
            @Override
            public void onResponse(Call<DataChannelsByLang> call, Response<DataChannelsByLang> response) {

                if (response.isSuccessful()) {
                    // use response data and do some fancy stuff :)
                    DataChannelsByLang channelsByLang = response.body();
                    Channel channel0 = channelsByLang.getChannels().get(0);
                    channelsByLang.setLang(titleFromLanguageTag(channel0.getLang()));
                    channelsByLangsList.add(channelsByLang);

                    /**
                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                     */

                } else {
                    /**
                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                     */
                }
            }

            @Override
            public void onFailure(Call<DataChannelsByLang> call, Throwable t) {
                showSomethingBadDialogAndFinish();
            }
        });
    }

    /**
     *
     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
     *
     */

    private String titleFromLanguageTag(String tag){

            /**
             *
             * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
             *
             */
        return tag;

    }

    private  void goToAuthorizationActivity(){

        Global.clearSharedPreferences(this);
        //Log out FB
        LoginManager.getInstance().logOut();

        //start authorization activity
        Intent myIntent = new Intent(this, ActivityAuthorization.class);
        startActivity(myIntent);
        //close this activity
        finish();
    }

    private void showNoInternetDialogAndFinish(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setMessage(getString(R.string.alert_dialog_no_internet_message))
                .setTitle(getString(R.string.alert_dialog_no_internet_title));

        builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();

            }
        });

        mDialog = builder.create();
        mDialog.show();
    }
    private void showSomethingBadDialogAndFinish(){

        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setMessage(getString(R.string.alert_dialog_server_down_message))
                .setTitle(getString(R.string.alert_dialog_server_down_title));

        builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();

            }
        });

        mDialog = builder.create();
        mDialog.show();
    }

    public void onPostAllLoadings() {

        if (!channelsByLangsList.isEmpty()) {

            Intent myIntent = new Intent(this, ActivityNavigation.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
            finish();

        } else {
            //create alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
            builder.setMessage(getString(R.string.alert_dialog_server_down_message))
                    .setTitle(getString(R.string.alert_dialog_server_down_title));

            builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    channelsByLangsList = null;
                    finish();

                }
            });

            mDialog = builder.create();
            mDialog.show();
        }
    }
}
