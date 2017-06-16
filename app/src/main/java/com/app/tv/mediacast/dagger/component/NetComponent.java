package com.app.tv.mediacast.dagger.component;

import com.app.tv.mediacast.ActivityAuthorization;
import com.app.tv.mediacast.ActivityCalendar;
import com.app.tv.mediacast.ActivityExoPlayer;
import com.app.tv.mediacast.ActivityForgotPassword;
import com.app.tv.mediacast.ActivityLoading;
import com.app.tv.mediacast.FragmentChangePassword;
import com.app.tv.mediacast.FragmentChannelsUkr;
import com.app.tv.mediacast.FragmentEditProfile;
import com.app.tv.mediacast.FragmentLanguage;
import com.app.tv.mediacast.FragmentPlanSettings;
import com.app.tv.mediacast.FragmentPlanSettingsCurrent;
import com.app.tv.mediacast.FragmentTimeZone;
import com.app.tv.mediacast.dagger.module.AppModule;
import com.app.tv.mediacast.dagger.module.NetModule;
import com.app.tv.mediacast.info.FragmentInfoTwo;
import com.app.tv.mediacast.info.InfoConfirmActivity;
import com.app.tv.mediacast.info.InfoCreateAccount;
import com.app.tv.mediacast.info.InfoSelectPriceActivity;
import com.app.tv.mediacast.info.InfoSubscribeStripeActivity;
import com.app.tv.mediacast.info.InfoUpgradeChooseCardActivity;

import javax.inject.Singleton;
import dagger.Component;
/**
 * Created by skyver on 3/19/17.
 */

@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface NetComponent {
    void inject(FragmentInfoTwo fragmentInfoTwo);
    void inject(InfoConfirmActivity infoConfirmActivity);
    void inject(InfoCreateAccount infoCreateAccount);
    void inject(InfoSelectPriceActivity infoSelectPriceActivity);
    void inject(InfoSubscribeStripeActivity infoSubscribeStripeActivity);
    void inject(InfoUpgradeChooseCardActivity infoUpgradeChooseCardActivity);
    void inject(ActivityAuthorization activityAuthorization);
    void inject(ActivityCalendar activityCalendar);
    void inject(ActivityExoPlayer activityExoPlayer);
    void inject(ActivityForgotPassword activityForgotPassword);
    void inject(ActivityLoading activityLoading);
    void inject(FragmentChangePassword fragmentChangePassword);
    void inject(FragmentChannelsUkr fragmentChannelsUkr);
    void inject(FragmentEditProfile fragmentEditProfile);
    void inject(FragmentLanguage fragmentLanguage);
    void inject(FragmentPlanSettings fragmentPlanSettings);
    void inject(FragmentPlanSettingsCurrent fragmentPlanSettingsCurrent);
    void inject(FragmentTimeZone fragmentTimeZone);

}
