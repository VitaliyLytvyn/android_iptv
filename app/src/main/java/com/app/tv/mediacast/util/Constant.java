package com.app.tv.mediacast.util;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.app.tv.mediacast.R;
import com.app.tv.mediacast.retrofit.data.DataError;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by skyver on 10/4/16.
 */

public class Constant {

    public static final String SITE_URL = "http://www.xxx.xx";
    public static final String HOST = "http://api.xxx.xx";//for downloading images
    public static final String BASE_URL = "https://api.xxx.xx";//for Retrofit
    public static final String TERMS_OF_USE_URL = "https://xxx.xx";
    public static final String SITE_HELP_EMAIL = "support@email.xx";
    public static final String HOSTNAME_VERIFIER_API = "api.xxx.xx";
    public static final String HOSTNAME_VERIFIER_MAIN = "xxx.xx";

    public static final String HEADER_PLATFORM = "Platform";
    public static final String HEADER_PLATFORM_API_LEVEL = "PlatformApiLevel";
    public static final String HEADER_DEVICE_UID = "DeviceUID";
    public static final String HEADER_DEVICE_MAC = "DeviceMac";
    public static final String HEADER_AUTHORISATION = "Authorization";
    public static final String AUTHORISATION_TYPE_BASIC = "Basic ";

    ////////////////////////// RETROFIT
    public static final String GET_LIST_OF_PLANS = "plan/list";
    public static final String GET_TERMS_OF_USE_LINK = "terms-of-use";
    public static final String REGISTER_USER = "users/register";
    public static final String REGISTER_USER_BY_FACEBOOK = "users/facebook-register";
    public static final String GET_USER_CREDIT_CARD = "card/last";
    public static final String RENEW_PLAN_BY_STRIPE = "users/stripe-renew";
    public static final String APPROVAL_REGISTER_USER_BY_STRIPE = "users/stripe-register-approval";
    public static final String RENEW_PLAN_BY_EXIST_CREDIT_CARD = "users/stripe-renew-by-card";
    public static final String LOGIN_USER = "users/login";
    public static final String LOGIN_USER_BY_FACEBOOK = "users/facebook-login";
    public static final String GET_PROGRAMMS_BY_CHANNEL_AND_DAY = "programs/{channel_id}/{day}";
    public static final String GET_PROGRAMMS_STREAM_URL = "programs/{id}";
    public static final String GET_TRANSLATE_URL = "channels/translate/{id}";
    public static final String RESET_USER_PASSWORD = "users/reset";
    public static final String CHECK_USER_ACCESS = "users/access";
    public static final String GET_APP_TRANSLATE_LANGUAGES = "translate-langs";
    public static final String GET_BY_LANG = "channels/{lang}";
    public static final String CHANGE_USER_PASSWORD = "users/change-password";
    public static final String GET_LIVE_URL = "channels/live/{id}";
    public static final String UPDATE_USER = "users/update";
    public static final String SHOW_USER_DETAILS = "users/show";
    public static final String SET_USER_LANGUAGE = "users/set-lang";
    public static final String GET_APP_LANGUAGES = "languages";
    public static final String SHOW_USER_PLAN_DATA = "users/plan";
    public static final String CANCEL_SUBSCRIPTION =  "users/subscription-cancel";
    public static final String SET_USER_TIMEZONE = "users/set-timezone";
    public static final String GET_APP_TIMEZONES = "timezones";

    //NEW
    public static final String ADD_TO_FAVORITES_LIST = HOST + "/channels/favorites/add";
    public static final String DELETE_FROM_FAVORITE_LIST = HOST + "/channels/favorites/";//:id
    public static final String GET_CHANNEL_BY_ID = HOST + "/channels/";//:id
    public static final String GET_FAVORITES_LIST = HOST + "/channels/favorites/list";
    public static final String GET_LIST_OF_PACKAGES = HOST + "/plan/packages";
    public static final String GET_PROGRAM_STREAM_BY_POSITION = HOST + "/programs/";//:id/position-:position
    public static final String GET_PROGRAMS_BY_CHANNEL_ID = HOST + "/programs/all/";//:channel_id
    public static final String CHECK_USER_EMAIL = HOST + "/users/check";
    public static final String APPROVAL_REGISTER_USER = HOST + "/users/register-approval";

    //other
    public static final String ASTERISKS = "****";
    public static final String STRIPE = "stripe";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String NEW_PASSWORD = "new_password";
    public static final String NAME = "name";
    public static final String FIRST_NAME = "first_name";
    public static final String PHONE = "phone";
    public static final String TIME_ZONE = "time_zone";
    public static final String LANGUAGE = "language";
    public static final String TOKEN = "token";
    public static final String PACKAGE_ID = "package_id";
    public static final String FACEBOOK_TOKEN = "facebook_token";
    public static final String FACEBOOK_DATA = "facebook_data";
    public static final String LAST_NAME = "last_name";
    public static final String ACCESS_SUCCESS = "granted";
    public static final String ACCESS_DENIED = "denied";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    public static final String CARD_ID = "card_id";
    public static final String MERCHANT_APPLE = "apple";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    //ERRORS
    public static final String ERROR_SUBSCRIPTION_NOT_EXISTS = "subscription.not.exist";
    public static final String ERROR_SUBSCRIPTION_NOT_FOUND = "subscription.not.found";
    public static final String ERROR_UNAUTHORIZED  = "Unauthorized";

    //used in data binding xml
    public static final String YEARLY = "yearly";

    public static final String TIME_ZONE_EUROPE_KIEV = "Europe/Kiev";
    public static final String LANGUAGE_ENGLISH = "English";
    public static final String KEY_LAST4 = "last_4";
    public static final String KEY_BRAND = "brand";
    public static final String KEY_CARD_ID = "card_id";
    public static final String KEY_ACCESS = "AccessKey";
    public static final String KEY_RAW_FB_JSON = "FbJsonKey";
    public static final String KEY_PACKAGE_ID = "package_id";
    public static final String KEY_SUBSCRIPTION_ID = "SubscriptionIdKey";
    public static final String KEY_FB_TOKEN = "TokenFBAuthKey";
    public static final String KEY_DAYS_LEFT = "DaysLeftKey";
    public static final String KEY_LOCALE = "LocaleKey";
    public static final String KEY_LANGUAGE = "LanguageKey";
    public static final String KEY_TIMEZONE = "TimezoneKey";
    public static final String KEY_PASS = "PasswordKey";
    public static final String KEY_ACTIVE = "ActiveKey";
    public static final String KEY_PHONE = "PhoneKey";
    public static final String KEY_EMAIL = "EmailKey";
    public static final String KEY_SURNAME = "SurnameKey";
    public static final String KEY_NAME = "NameKey";
    public static final String KEY_USER_ID = "UserIdKey";
    public static final String KEY_TOKEN_AUTH = "TokenAuthKey";
    public static final String KEY_DEVICE_MAC = "Device_Mac";
    public static final String KEY_DEVICE_ID = "Device_Id";
    public static final String PREFS_NAME = "GlobalPreferences";
    public static final String PLATFORM = "android";
    public static final String MAC_UNDEFINED = "mac_undefined";
    public static final String STATIC_MAC = "02:00:00:00:00:00";
    public static final String ID_UNDEFINED = "_id_undefined";
    public static final String UA_TAG = "ua";

    public static final String NULL_STRING = "null";

    private static final HashMap<String, Integer> errorsMap = new HashMap<>(50);//~45

    static{
        errorsMap.put("channel_id.required", R.string.channel_id_required);
        errorsMap.put("channel_id.invalid", R.string.channel_id_invalid);
        errorsMap.put("channel.not.found", R.string.channel_not_found);
        errorsMap.put("channels.not.found", R.string.channels_not_found);
        errorsMap.put("channel.already.added", R.string.channel_already_added);
        errorsMap.put("lang.required", R.string.lang_required);
        errorsMap.put("lang.invalid", R.string.lang_invalid);
        errorsMap.put("id.required", R.string.id_required);
        errorsMap.put("id.invalid", R.string.id_invalid);
        errorsMap.put("access.invalid", R.string.access_invalid);
        errorsMap.put("facebook_token.required", R.string.facebook_token_required);
        errorsMap.put("user.not.exist", R.string.user_not_exist);
        errorsMap.put("user.not.active", R.string.user_not_active);
        errorsMap.put("email.required", R.string.email_required);
        errorsMap.put("email.invalid", R.string.email_invalid);
        errorsMap.put("name.required", R.string.name_required);
        errorsMap.put("last_name.required", R.string.last_name_required);
        errorsMap.put("package_id.required", R.string.package_id_required);
        errorsMap.put("package_id.invalid", R.string.package_id_invalid);
        errorsMap.put("package.not.exist", R.string.package_not_exist);
        errorsMap.put("package.not.found", R.string.package_not_found);
        errorsMap.put("program.not.found", R.string.program_not_found);
        errorsMap.put("programs.not.found", R.string.programs_not_found);
        errorsMap.put("day.invalid", R.string.day_invalid);
        errorsMap.put("subscription_id.required", R.string.subscription_id_required);
        errorsMap.put("subscription_id.invalid", R.string.subscription_id_invalid);
        errorsMap.put("subscription.invalid", R.string.subscription_invalid);
        errorsMap.put("token.required", R.string.token_required);
        errorsMap.put("new_password.required", R.string.new_password_required);
        errorsMap.put("new_password.length", R.string.new_password_length);
        errorsMap.put("language.invalid", R.string.language_invalid);
        errorsMap.put("language.required", R.string.language_required);
        errorsMap.put("language.busy", R.string.language_busy);
        errorsMap.put("password.required", R.string.password_required);
        errorsMap.put("password.length", R.string.password_length);
        errorsMap.put("stripe_id.required", R.string.stripe_id_required);
        errorsMap.put("user.exist", R.string.user_exist);
        errorsMap.put("time_zone.required", R.string.time_zone_required);
        errorsMap.put("time_zone.invalid", R.string.time_zone_invalid);
        errorsMap.put("first_name.required", R.string.first_name_required);
        errorsMap.put("phone.required", R.string.phone_required);
        errorsMap.put("subscription.already.cancelled", R.string.subscription_already_cancelled);
        errorsMap.put("card.not.found", R.string.card_not_found);
        errorsMap.put("card_id.required", R.string.card_id_required);
        errorsMap.put("card_id.invalid", R.string.card_id_invalid);
        errorsMap.put("subscription.not.found", R.string.subscription_not_found);

    }

    public static void getErrorString(Context context, String error){
        if(errorsMap.containsKey(error)){
            toastIt(context, context.getString(errorsMap.get(error)));

        } else {
            toastIt(context, error);
        }
    }

    public static void toastIt(Context context, String error){
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
    }

    //Retrofit - parsing error to show to user
    public static DataError parseError(Retrofit retrofit, Response<?> response) {
        //paranoid
        if(response.errorBody() == null){
            return new DataError();
        }

        Converter<ResponseBody, DataError> converter =
                retrofit.responseBodyConverter(DataError.class, new Annotation[0]);

        DataError error;
        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new DataError();
        }
        return error;
    }

    private static Map<String, String> mapOfHeaders = new HashMap<>();
    public static void initHeaders(final Context context) {
        if(context != null){
            String mId  = Global.getID(context);
            String mMac = Global.getMAC(context);
            String mPlatform = Global.getPlatform();
            String mToken = Global.getSharedPreferences(context, KEY_TOKEN_AUTH);
            String mPlatformApiLevel = Integer.toString(android.os.Build.VERSION.SDK_INT);
            mapOfHeaders.put(HEADER_PLATFORM, mPlatform);
            mapOfHeaders.put(HEADER_DEVICE_UID, mId);
            mapOfHeaders.put(HEADER_DEVICE_MAC, mMac);
            mapOfHeaders.put(HEADER_PLATFORM_API_LEVEL, mPlatformApiLevel);
            mapOfHeaders.put(HEADER_AUTHORISATION, AUTHORISATION_TYPE_BASIC + mToken);
        }
    }
    public static void replaceHeader(String header, String headerValue){
        mapOfHeaders.put(header, headerValue);
    }

    public static Map<String, String> getHeaders(){
        return mapOfHeaders;
    }

}
