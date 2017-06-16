package com.app.tv.mediacast.info;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.R;

public class InfoShowTermsOfUseActivity extends AppCompatActivity {

    private final static String URL = "url";
    WebView mWebView;
    String mLink;
    private boolean isRenew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.info_terms_of_use);

        mWebView = (WebView) findViewById(R.id.webview);

        if(savedInstanceState == null){
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
            mLink = getIntent().getStringExtra(URL);

            if(mLink == null){
                finish();
                return;
            }
            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.setWebChromeClient(new WebChromeClient());
            mWebView.loadUrl(mLink != null ? mLink : Constant.TERMS_OF_USE_URL);
        } else {
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW, false);
            mLink = savedInstanceState.getString(URL);
            mWebView.restoreState(savedInstanceState);
        }

        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(InfoConfirmActivity.CHECK, true);
                intent.putExtra(InfoMainActivity.RENEW, isRenew);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(mWebView != null){
            mWebView.stopLoading();
            mWebView.setWebViewClient(null);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        outState.putString(URL, mLink);
        mWebView.saveState(outState);

        mWebView.stopLoading();
        mWebView.setWebViewClient(null);
        mWebView.destroy();
        mWebView = null;
        super.onSaveInstanceState(outState);
    }

    public static Intent makeMyIntent(Context context, String url, boolean isRenew){
        Intent newIntent = new Intent(context, InfoShowTermsOfUseActivity.class);
        newIntent.putExtra(URL, url);
        newIntent.putExtra(InfoMainActivity.RENEW, isRenew);
        return newIntent;
    }
}
