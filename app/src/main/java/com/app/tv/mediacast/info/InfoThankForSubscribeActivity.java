package com.app.tv.mediacast.info;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.app.tv.mediacast.ActivityLoading;
import com.app.tv.mediacast.R;

public class InfoThankForSubscribeActivity extends AppCompatActivity {

    private boolean isRenew = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.info_thank_for_subcribe_layout);

        if(savedInstanceState != null){
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW, false);
        } else {
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
        }

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(InfoThankForSubscribeActivity.this, ActivityLoading.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                InfoThankForSubscribeActivity.this.startActivity(myIntent);
                finish();
            }
        });

        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InfoThankForSubscribeActivity.this, InfoMainActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    intent.putExtra(InfoMainActivity.RENEW, isRenew);
                    startActivity(intent);
                    InfoThankForSubscribeActivity.this.finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        super.onSaveInstanceState(outState);
    }
}
