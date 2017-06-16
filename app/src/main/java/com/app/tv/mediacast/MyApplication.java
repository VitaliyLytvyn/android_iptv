package com.app.tv.mediacast;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.app.tv.mediacast.dagger.component.DaggerNetComponent;
import com.app.tv.mediacast.dagger.component.NetComponent;
import com.app.tv.mediacast.dagger.module.AppModule;
import com.app.tv.mediacast.dagger.module.NetModule;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.Locale;

import static com.app.tv.mediacast.util.Constant.BASE_URL;


public class MyApplication extends Application {

    public static final String FORCE_LOCAL = "force_local";

    private NetComponent mNetComponent;

    @Override
    public void onCreate()
    {
        updateLanguage(this,null);
        super.onCreate();

        // Initialize the FB SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mNetComponent = DaggerNetComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule(BASE_URL))
                .build();

        Constant.initHeaders(this);
    }


    public NetComponent getNetComponent() {
        return mNetComponent;
    }

    public static void updateLanguage(Context ctx, String lang)
    {
        Configuration cfg = new Configuration();
        SharedPreferences force_pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String language = force_pref.getString(FORCE_LOCAL, "");

        if(TextUtils.isEmpty(language) && lang == null){
            cfg.locale = Locale.getDefault();

            SharedPreferences.Editor edit = force_pref.edit();
            String tmp="";
            tmp=Locale.getDefault().toString().substring(0, 2);

            edit.putString(FORCE_LOCAL, tmp);
            edit.commit();

        }else if(lang!=null){
            cfg.locale = new Locale(lang);
            SharedPreferences.Editor edit = force_pref.edit();
            edit.putString(FORCE_LOCAL, lang);
            edit.commit();

        }else if(!TextUtils.isEmpty(language)){
            cfg.locale = new Locale(language);
        }

        ctx.getResources().updateConfiguration(cfg, null);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SharedPreferences force_pref = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext().getApplicationContext());

        String language = force_pref.getString(FORCE_LOCAL, "");

        super.onConfigurationChanged(newConfig);
        updateLanguage(this,language);
    }
}