package com.app.tv.mediacast.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.app.tv.mediacast.ActivityNavigation;
import com.app.tv.mediacast.R;
import com.app.tv.mediacast.util.Constant;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Global {


    private static SharedPreferences.Editor editor;
    private static SharedPreferences sharedPreferences;

    private static String sId = null;
    private static String sMac = null;


    public static void putSharedPreferences(Activity activity,
                                            String userId,
                                            String name,
                                            String surname,
                                            String auth) {
        editor = activity.getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(Constant.KEY_USER_ID, userId);
        editor.putString(Constant.KEY_NAME, name);
        editor.putString(Constant.KEY_SURNAME, surname);
        editor.putString(Constant.KEY_TOKEN_AUTH, auth);
        editor.commit();
        editor = null;//
    }

    public static void addToSharedPreferences(Activity activity, String resitoryName, String key, String value) {
        editor = activity.getSharedPreferences(resitoryName, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
        editor = null;//
    }

    public static void replaceSharedPreferences(Context activity, String key, String value) {
        editor = activity.getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
        editor = null;//
    }

    public static String getSharedPreferences(Context activity, String keyName) {
        sharedPreferences = activity.getSharedPreferences(Constant.PREFS_NAME, Activity.MODE_PRIVATE);
        String result = sharedPreferences.getString(keyName, Constant.NULL_STRING);
        sharedPreferences = null;
        return result;
    }

    public static void clearSharedPreferences(Context activity) {
        editor = activity.getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
        editor = null;//
    }

    public static String getID(Context activity){
        if(sId == null){
            return extractID(activity);
        } else
            return sId;
    }

    private static String extractID(Context activity) {
        String tmp = null;

        //extract _id from prefernces
        tmp = getSharedPreferences(activity, Constant.KEY_DEVICE_ID);
        if(!tmp.equals(Constant.NULL_STRING)){
            sId = tmp;
            return sId;
        }
        //there is no _id in preferences so obtain it and only if exists put it in preferences
        //default _id is obtained from Build.SERIAL
        tmp = Build.SERIAL;

        if(tmp != null && !tmp.equals("") && !tmp.equals("unknown")){
            replaceSharedPreferences(activity, Constant.KEY_DEVICE_ID, tmp);
            sId = tmp;
            return sId;
        }
        //Build.SERIAL is not defined for this device so try using Secure.ANDROID_ID
        tmp = Settings.Secure.getString(activity.getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        if(tmp != null && !tmp.equals("")){
            replaceSharedPreferences(activity, Constant.KEY_DEVICE_ID, tmp);
            sId = tmp;
            return sId;
        }
        //at this point we failed to have _id so use "id_undefined" constant

        return Constant.ID_UNDEFINED;
    }

    public static String getMAC(Context activity){
        if(sMac == null){
            return extractMAC(activity);
        } else
            return sMac;
    }

    private static String extractMAC(Context activity) {
        String tmp = null;

        //extract MAC from prefernces
        tmp = getSharedPreferences(activity, Constant.KEY_DEVICE_MAC);
        if(!tmp.equals(Constant.NULL_STRING)){
            sMac = tmp;
            return sMac;
        }
        //there is no MAC in preferences so obtain it and put in preferences
        //obtain MAC
        WifiInfo wifiInf = null;
        WifiManager wifiMan = (WifiManager) activity.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if(wifiMan != null)
            wifiInf = wifiMan.getConnectionInfo();

        if(wifiInf != null) {

            String mac = wifiInf.getMacAddress();
            //if that is STATIC_MAC "02:00:00:00:00:00" then consider it as UNDEFINED
            if (mac == null || mac.equals(Constant.STATIC_MAC)) {
                replaceSharedPreferences(activity, Constant.KEY_DEVICE_MAC, Constant.MAC_UNDEFINED);
                sMac = Constant.MAC_UNDEFINED;
                return sMac;
            } else {
                replaceSharedPreferences(activity, Constant.KEY_DEVICE_MAC, tmp);
                sMac = mac;
                return sMac;
            }
        }

        //at this point we faild to have _id so use "id_undefined"
        return Constant.MAC_UNDEFINED;
    }

    public static String getPlatform(){return Constant.PLATFORM;}

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isAppRunning(Context context) {
        String activityStr = ActivityNavigation.class.getName();
        ActivityManager activityManager = (ActivityManager) context.
                getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.
                getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityStr.equals(task.baseActivity.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void hideKeyBoard(Context context, View view){
        //Constant.hideKeyBoard(FragmentInfoEmailDialog());
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if(view != null){
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private static CharSequence apply(CharSequence[] content, Object... tags) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        openTags(text, tags);
        for (CharSequence item : content) {
            text.append(item);
        }
        closeTags(text, tags);
        return text;
    }

    private static void openTags(Spannable text, Object[] tags) {
        for (Object tag : tags) {
            text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
        }
    }

    private static void closeTags(Spannable text, Object[] tags) {
        int len = text.length();
        for (Object tag : tags) {
            if (len > 0) {
                text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                text.removeSpan(tag);
            }
        }
    }

    public static CharSequence bold(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.BOLD));
    }

    public static CharSequence color(int color, CharSequence... content) {
        return apply(content, new ForegroundColorSpan(color));
    }

    public static CharSequence italic(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.ITALIC));
    }

    //public static CharSequence textSize(CharSequence content, int size) {
    public static CharSequence textSize(CharSequence content) {
        SpannableString span = new SpannableString(content);
        //span.setSpan(new AbsoluteSizeSpan(size), 0, content.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan(25), 0, content.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return span;
    }
    public static CharSequence textSize(CharSequence content, int size) {
        SpannableString span = new SpannableString(content);
        //span.setSpan(new AbsoluteSizeSpan(size), 0, content.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan(size), 0, content.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return span;
    }

    private static class EmailValidator {
        // Regex pattern to valid email address
        private static final String EMAIL_REGEX=
                "^[\\w!#$%&’*+/=\\-?^_`{|}~]+(\\.[\\w!#$%&’*+/=\\-?^_`{|}~]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
        //static Pattern object, since pattern is fixed
        private static Pattern pattern;
        //non-static Matcher object because it's created from the input String
        private Matcher matcher;

        EmailValidator(){
            //initialize the Pattern object
            pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        }
        /**
         * This method validates the input email address with EMAIL_REGEX pattern
         * @param email
         * @return boolean
         */
        boolean validateEmail(String email){
            matcher = pattern.matcher(email);
            return matcher.matches();
        }
    }

    public static boolean validateEmail(String email){
        EmailValidator emailValidator = new EmailValidator();
        return emailValidator.validateEmail(email);
    }

    public static int getScreenOrientation(Activity activity) {
        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    public static void replaceFragment(Fragment fragment, FragmentManager fragmentManager, boolean toBackStack) {
        String backStateName = fragment.getClass().getName();
        String fragmentTag = backStateName;

        FragmentManager manager = fragmentManager;
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        //solves duplicate back stack entries
        if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null) { //if fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.container, fragment, fragmentTag);
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            if (toBackStack) {
                ft.addToBackStack(backStateName);
            }
            ft.commit();
        }
    }
}
