package com.app.tv.mediacast.info;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.ASTERISKS;
import static com.app.tv.mediacast.util.Constant.CARD_ID;
import static com.app.tv.mediacast.util.Constant.KEY_BRAND;
import static com.app.tv.mediacast.util.Constant.KEY_CARD_ID;
import static com.app.tv.mediacast.util.Constant.KEY_LAST4;
import static com.app.tv.mediacast.util.Constant.PACKAGE_ID;
import static com.app.tv.mediacast.util.Constant.STRIPE;

public class InfoUpgradeChooseCardActivity extends AppCompatActivity {

    private String mCardId;
    private String mPackageId;
    TextView txtCardPlaceholder;
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
        setContentView(R.layout.info_subscribe_stripe_renew_layout);

        ((MyApplication) getApplication()).getNetComponent().inject(this);

        if(savedInstanceState != null){
            mPackageId = savedInstanceState.getString(PACKAGE_ID);
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW, false);
        } else {
            mPackageId = getIntent().getStringExtra(PACKAGE_ID);
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
        }

        String mBrand = Global.getSharedPreferences(this, KEY_BRAND);
        String mLast4 = Global.getSharedPreferences(this, KEY_LAST4);


        mCardId = Global.getSharedPreferences(this, KEY_CARD_ID);

        txtCardPlaceholder = (TextView) findViewById(R.id.textViewCardPlaceHolder);
        txtCardPlaceholder.setText(Html
                .fromHtml(mBrand.toUpperCase() + "<sup> " + ASTERISKS + "</sup> - " + mLast4));

        TextView headerText = (TextView) findViewById(R.id.textViewHead);
        headerText.setText(getString(R.string.upgrade_choose_card_header));

        TextView txtStripe = (TextView) findViewById(R.id.textView18);
        CharSequence text1 = Global.bold(Global.textSize(getString(R.string.upgrade_choose_card_powered_by), 25));
        CharSequence text2 = Global.bold(Global.textSize(STRIPE, 35));
        CharSequence text3 = TextUtils.concat(text1, "  ", text2);
        txtStripe.setText(text3);

        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InfoUpgradeChooseCardActivity.this, InfoMainActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    intent.putExtra(InfoMainActivity.RENEW, isRenew);
                    startActivity(intent);
                    InfoUpgradeChooseCardActivity.this.finish();
                }
            });
        }

        findViewById(R.id.useThisCardButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onThisButtonProcess();
            }
        });

        findViewById(R.id.useAnotherCardButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoUpgradeChooseCardActivity.this,
                        InfoSubscribeStripeActivity.class)
                        .putExtra(InfoMainActivity.RENEW, isRenew)
                        .putExtra(InfoSubscribeStripeActivity.PACKAGE, mPackageId));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PACKAGE_ID, mPackageId);
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        super.onSaveInstanceState(outState);
    }

    private void startProgress() {
        GlobalProgressDialog.show(this, getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private void onThisButtonProcess(){

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */


    }

}
