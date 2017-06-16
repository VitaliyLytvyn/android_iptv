package com.app.tv.mediacast.info;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.R;

public class InfoBackToCheckOutActivity extends AppCompatActivity {

    private boolean isRenew = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_back_to_check_out_activity);

        if(savedInstanceState != null){
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW, false);
        } else {
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
        }

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String packageId = Global.getSharedPreferences(InfoBackToCheckOutActivity.this, Constant.KEY_PACKAGE_ID);
                if(packageId != null && !packageId.equals(Constant.NULL_STRING)){
                    Intent myIntent =  InfoConfirmActivity.makeMyIntent(InfoBackToCheckOutActivity.this, packageId, isRenew);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                    finish();
                } else {
                    Intent myIntent =  new Intent(InfoBackToCheckOutActivity.this, InfoMainActivity.class)
                            .putExtra(InfoMainActivity.RENEW, isRenew);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                    finish();
                }
            }
        });

        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InfoBackToCheckOutActivity.this, InfoMainActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    intent.putExtra(InfoMainActivity.RENEW, isRenew);
                    startActivity(intent);
                    InfoBackToCheckOutActivity.this.finish();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        super.onSaveInstanceState(outState);
    }
}
