package com.app.tv.mediacast.info;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;
import com.app.tv.mediacast.MyApplication;
import com.app.tv.mediacast.R;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataUserCard;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.PACKAGE_ID;

public class InfoSelectPriceActivity extends AppCompatActivity
        implements FragmentInfoTable.OnJoinButtonResultListener {

    private boolean isDownLoading;
    private boolean isRenew;
    private boolean isCardReceived;
    private String mPackageId;
    private static final String CARD_RECEIVED = "card_received";

    DataUserCard mDataUserCreditCard;

    Call<DataUserCard> call;

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
        setContentView(R.layout.info_select_price_activity);

        ((MyApplication) getApplication()).getNetComponent().inject(this);

        TextView hederText = (TextView)findViewById(R.id.textViewHead);
        hederText.setText(getResources().getString(R.string.info_header_choose_plan));

        if(findViewById(R.id.frame_for_fragment_two) != null){
            if(savedInstanceState == null){
                FragmentInfoTwo fr2 = FragmentInfoTwo.newInstance(true);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame_for_fragment_two, fr2).commit();
            }
        }

        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InfoSelectPriceActivity.this, InfoMainActivity.class);
                    intent.putExtra(InfoMainActivity.RENEW, isRenew);
                    startActivity(intent);
                    InfoSelectPriceActivity.this.finish();
                }
            });
        }

        if(savedInstanceState != null){
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW);
            isCardReceived = savedInstanceState.getBoolean(CARD_RECEIVED);
            mPackageId = savedInstanceState.getString(PACKAGE_ID, null);
        } else {
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
        }

        if(isRenew && !isCardReceived){
            isDownLoading = true;
            retrievePreviousCreditCard();
        } else if(mPackageId != null){
            //goFarther();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        outState.putBoolean(CARD_RECEIVED, isCardReceived);
        outState.putString(PACKAGE_ID, mPackageId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onJoinButtonResult(int position, int selected) {

        if(selected == -1 || position == -1){
            //something went wrong go to beginning
            startActivity(new Intent(this, InfoMainActivity.class).putExtra(InfoMainActivity.RENEW, isRenew));
            finish();
        }else{
            mPackageId = FragmentInfoTwo.dataPlanList.get(position)
                .getPackages().get(selected).getPackageId();

            if(!isDownLoading){
                goFarther();
            } else {
                startProgress();
            }
        }
    }

    private void startProgress() {
        GlobalProgressDialog.show(this, getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private void goFarther(){

        finishProgress();
        if(isRenew && isCardReceived){
            startActivity(new Intent(this, InfoUpgradeChooseCardActivity.class)
                    .putExtra(PACKAGE_ID, mPackageId)
                    .putExtra(InfoMainActivity.RENEW, isRenew));
            return;
        }
        String token = Global.getSharedPreferences(this, Constant.KEY_TOKEN_AUTH);
        String subscr_id = Global.getSharedPreferences(this, Constant.KEY_SUBSCRIPTION_ID);
        if(!token.equals(Constant.NULL_STRING) || !subscr_id.equals(Constant.NULL_STRING)){
            startActivity(InfoConfirmActivity.makeMyIntent(this, mPackageId, isRenew));
        }else {
            startActivity(InfoCreateAccount.makeMyIntent(this, mPackageId, isRenew));
        }
    }

    private  void retrievePreviousCreditCard(){

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

        goFarther();
    }
}


