package com.app.tv.mediacast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.app.tv.mediacast.info.InfoMainActivity;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.EMAIL;


public class ActivityForgotPassword extends AppCompatActivity {

    private Button buttonRestoreEmail;
    private EditText editTextEmail;
    private boolean isRenew = false;

    Call<Void> call;

    @Inject
    Retrofit retrofit;

    @Override
    public void onStop() {
        if(call != null && !call.isCanceled()){
            call.cancel();
            call = null;
        }
        finishProgress();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MyApplication) getApplication()).getNetComponent().inject(this);

        setContentView(R.layout.activity_forgot_password);

        //CHECKING if this is renew case
        if(savedInstanceState != null){
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW, false);
        } else {
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
        }

        editTextEmail = (EditText) findViewById(R.id.editText_restore_email);
        buttonRestoreEmail = (Button) findViewById(R.id.button_restore_email);

        buttonRestoreEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if empty field
                String inputEmail = editTextEmail.getText().toString().trim();
                if (inputEmail.equals("")) {
                    Constant.toastIt(ActivityForgotPassword.this,
                            getString(R.string.authorization_empty_email_en));
                    return;
                }
                //CHECK FOR ACCESS
                if (InternetConnection.isConnected(ActivityForgotPassword.this)) {
                    startProgress();
                    controlsEnable(false);
                    //hide keyBoard
                    Global.hideKeyBoard(ActivityForgotPassword.this, v);

                    Map<String, String> mapOfData = new HashMap<>();

                    /**
                     *
                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                     *
                     */

                    //Create a retrofit call object
                    call = retrofit.create(Restapi.class).resetUserPassword(Constant.getHeaders(), mapOfData);

                    //Enqueue the call
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {

                            finishProgress();
                            controlsEnable(true);
                            if (response.isSuccessful()) {
                                // use response data and do some fancy stuff :)

                                Constant.toastIt(ActivityForgotPassword.this,
                                        getString(R.string.forgot_pass_request_confirm));
                                //start Authorization activity
                                Intent myIntent = new Intent(ActivityForgotPassword.this, ActivityAuthorization.class);
                                myIntent.putExtra(InfoMainActivity.RENEW, isRenew);
                                ActivityForgotPassword.this.startActivity(myIntent);
                                finish();

                            } else {
                                // parse the response body …
                                DataError error = Constant.parseError(retrofit, response);
                                if(error.getError() == null){
                                    Constant.toastIt(ActivityForgotPassword.this, getString(R.string.user_info_fail_retry));
                                } else {
                                    Constant.getErrorString(ActivityForgotPassword.this, error.getError());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            finishProgress();
                            controlsEnable(true);
                            Constant.toastIt(ActivityForgotPassword.this, getString(R.string.user_info_fail_retry));
                        }
                    });
                } else {
                    //no internet connection
                    Constant.toastIt(ActivityForgotPassword.this, getString(R.string.no_internet_connection));
                }

            }
        });

        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ActivityForgotPassword.this, InfoMainActivity.class)
                            .putExtra(InfoMainActivity.RENEW, isRenew));
                    ActivityForgotPassword.this.finish();
                }
            });
        }

    }

    private void controlsEnable(boolean enable){
        buttonRestoreEmail.setEnabled(enable);
        editTextEmail.setEnabled(enable);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        super.onSaveInstanceState(outState);
    }

    private void startProgress() {
        GlobalProgressDialog.show(this, getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

}
