package com.app.tv.mediacast.info;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;
import com.app.tv.mediacast.MyApplication;
import com.app.tv.mediacast.R;
import com.app.tv.mediacast.databinding.InfoConfirmWithoutPlayBtnBinding;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataPlan;
import com.app.tv.mediacast.retrofit.data.DataTermsOfUseLink;
import com.app.tv.mediacast.retrofit.data.Package;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class InfoConfirmActivity extends AppCompatActivity {

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    static final int TERMS_REQUEST = 10002;
    static final String CHECK = "check";

    private boolean mIsAbleGooglePay;
    private Button mPlayMarketButton;
    private String mPackageId;
    private String  mEmail;
    private String  mDate;

    CheckBox mCheckBox;

    Package infoPackage = null;
    DataPlan infoPlan=null;
    private boolean isRenew = false;

    Call<DataTermsOfUseLink> call;

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
        //setContentView(R.layout.info_confirm_with_play_btn);

        ((MyApplication) getApplication()).getNetComponent().inject(this);

        if(savedInstanceState == null){
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
            mPackageId = getIntent().getStringExtra(Constant.PACKAGE_ID);

            if(mPackageId == null){
                //something went wrong - go to beginning
                startActivity(new Intent(this, InfoMainActivity.class)
                        .putExtra(InfoMainActivity.RENEW, isRenew));
                finish();
            }
        } else {
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW, false);
            mPackageId = savedInstanceState.getString(Constant.PACKAGE_ID);
        }

        if(FragmentInfoTwo.dataPlanList == null){
            //something went wrong - go to beginning
            startActivity(new Intent(this, InfoSelectPriceActivity.class)
                    .putExtra(InfoMainActivity.RENEW, isRenew));
            finish();
            return;
        }

        prepareDataForBinding();

        //InfoConfirmWithPlayBtnBinding binding = DataBindingUtil.setContentView(this, R.layout.info_confirm_with_play_btn);
        InfoConfirmWithoutPlayBtnBinding binding = DataBindingUtil.setContentView(this, R.layout.info_confirm_without_play_btn);
        binding.setInfoPackage(infoPackage);
        binding.setInfoPlan(infoPlan);
        binding.setEmail(mEmail);
        binding.setDate(mDate);

        mCheckBox = (CheckBox)findViewById(R.id.checkBoxConfirmActivity);

        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InfoConfirmActivity.this, InfoMainActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    intent.putExtra(InfoMainActivity.RENEW, isRenew);
                    startActivity(intent);
                    InfoConfirmActivity.this.finish();
                }
            });
        }

        //TODO uncomment when GooglePlay Payment
//        mPlayMarketButton = (Button)findViewById(R.id.buttonPlayMarket);
//        tryAdjustPlayMarketButton();
//        initGooglePlay();

    }

    private void prepareDataForBinding(){
        boolean flag = false;
        for(DataPlan plan : FragmentInfoTwo.dataPlanList){
            if(flag) break;

            for(Package pkg : plan.getPackages()){

                if(pkg.getPackageId().equals(mPackageId)){

                    infoPackage = pkg;
                    infoPlan = plan;
                    flag = true;
                    break;
                }
            }
        }
        String str = Global.getSharedPreferences(this, Constant.KEY_EMAIL);
        mEmail = str.equals(Constant.NULL_STRING) ? "" : str;

        Calendar clnd = Calendar.getInstance();
        clnd.setTimeZone(TimeZone.getDefault());
        clnd.setTime(new Date());
        clnd.add(Calendar.DATE, 7);
        DateFormat format = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.getDefault());
        mDate = format.format(clnd.getTime());
    }

    private void startProgress() {
        GlobalProgressDialog.show(this, getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Constant.PACKAGE_ID, mPackageId);
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        super.onSaveInstanceState(outState);
    }

    private void tryAdjustPlayMarketButton(){

        Drawable drawable2 = AppCompatResources.getDrawable(this, R.drawable.ic_google_play_vector_2 );
        mPlayMarketButton.setCompoundDrawablesWithIntrinsicBounds(drawable2, null, null, null);
        mPlayMarketButton.setCompoundDrawablePadding(this.getResources().
                getDimensionPixelSize(R.dimen.google_btn_margin_override_textpadding));
        mPlayMarketButton.setPadding(
                this.getResources().getDimensionPixelSize(
                        R.dimen.google_btn_margin_override_lr),
                this.getResources().getDimensionPixelSize(
                        R.dimen.google_btn_margin_override_top),
                this.getResources().getDimensionPixelSize(
                        R.dimen.google_btn_margin_override_rr),
                this.getResources().getDimensionPixelSize(
                        R.dimen.google_btn_margin_override_bottom));

    }

    // "Subscribe with GooglePlay button clicked. Explain to user, then start purchase
    // flow for subscription.
    public void playMarketButtonClicked(View v){

        //TODO DELETE WHEN GOOGLEPLAY WORKS
        if(true)return;

        if(mCheckBox.isChecked()){
            //startGoogleCheckoOut();

        } else{
            Toast.makeText(InfoConfirmActivity.this, getString(R.string.you_need_agree_terms_of_use),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void subscribeButtonClicked(View v){
        if(mCheckBox.isChecked()){
            Intent myIntent = new Intent(InfoConfirmActivity.this, InfoSubscribeStripeActivity.class);
            myIntent.putExtra(InfoMainActivity.RENEW, isRenew);
            InfoConfirmActivity.this.startActivity(myIntent);

            finish();

        } else{
            Toast.makeText(InfoConfirmActivity.this, getString(R.string.you_need_agree_terms_of_use),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void showTermsOfUse(View v){

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(this)) {
            //no internet connection
            Constant.toastIt(InfoConfirmActivity.this, getString(R.string.no_internet_connection));
            return;
        }
        startProgress();

        //Create a retrofit call object
        call = retrofit.create(Restapi.class).getTermsOfUseLink(Constant.getHeaders());

        //Enqueue the call
        call.enqueue(new Callback<DataTermsOfUseLink>() {
            @Override
            public void onResponse(Call<DataTermsOfUseLink> call, Response<DataTermsOfUseLink> response) {

                finishProgress();
                if (response.isSuccessful()) {
                    // use response data and do some fancy stuff :)
                     showTermsOfUse(response.body().getLink());

                } else {
                    // parse the response body …
                    DataError error = Constant.parseError(retrofit, response);
                    if(error.getError() == null){
                        Constant.toastIt(InfoConfirmActivity.this, getString(R.string.user_info_fail_retry));
                    } else {
                        Constant.getErrorString(InfoConfirmActivity.this, error.getError());
                    }
                }
            }

            @Override
            public void onFailure(Call<DataTermsOfUseLink> call, Throwable t) {
                finishProgress();
                Constant.toastIt(InfoConfirmActivity.this, getString(R.string.user_info_fail_retry));
            }
        });

    }

    private void showTermsOfUse(String link){

        Intent myIntent = InfoShowTermsOfUseActivity.makeMyIntent(this, link, isRenew);
        startActivityForResult(myIntent, TERMS_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TERMS_REQUEST){
            if(resultCode == RESULT_OK){
                boolean ifSetCheck = data.getBooleanExtra(CHECK, false);
                if(ifSetCheck){
                    mCheckBox.setChecked(true);
                }
            }
        }
    }

    public void changePlan(View v){
        Intent myIntent = new Intent(this, InfoSelectPriceActivity.class);
        myIntent.putExtra(InfoMainActivity.RENEW, isRenew);
        startActivity(myIntent);
        finish();
    }

    public static Intent makeMyIntent(Context context, String packageId, boolean isRenew){
        Intent newIntent = new Intent(context, InfoConfirmActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        newIntent.putExtra(Constant.PACKAGE_ID, packageId);
        newIntent.putExtra(InfoMainActivity.RENEW, isRenew);
        return newIntent;
    }

    private void initGooglePlay() {

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

    }

}
