package com.app.tv.mediacast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.tv.mediacast.info.InfoMainActivity;
import com.app.tv.mediacast.info.InfoSelectPriceActivity;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;
import com.facebook.login.LoginManager;


public class ActivityNavigation extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnDialogNeededListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private AlertDialog mDialog;

    public DrawerLayout mDrawerLayout;

    public static final String ACCESS = "access";
    public static boolean isAccessGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if(savedInstanceState == null){
            String key = Global.getSharedPreferences(this, Constant.KEY_ACCESS);
            isAccessGranted = (!key.equals(Constant.NULL_STRING) && key.equals(Constant.ACCESS_SUCCESS));
        } else {
            isAccessGranted = savedInstanceState.getBoolean(ACCESS, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ACCESS, isAccessGranted);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }

        GlobalProgressDialog.dismiss();
        super.onStop();
    }

    @Override
    public void onDialogNeeded() {
        //create no subscription notification dialog
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder
                (this, R.style.AlertDialogCustom);
        builder.setMessage(getString(R.string.no_subscription_dialog_message));

        builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                Intent intent = new Intent(ActivityNavigation.this, InfoSelectPriceActivity.class);
                intent.putExtra(InfoMainActivity.RENEW, true);
                startActivity(intent);
            }
        });

        builder.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        mDialog = builder.create();
        mDialog.show();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        switch (position) {
            case 0://channels

                Global.replaceFragment(FragmentChannels.newInstance(position + 1),
                        getSupportFragmentManager(),
                        false);
                break;
            case 1://timezone
                if (InternetConnection.isConnected(this)) {
                    Global.replaceFragment(FragmentTimeZone.newInstance(position + 1),
                            getSupportFragmentManager(),
                            false);
                } else {
                    //no internet connection
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                break;
            case 2://language
                if (InternetConnection.isConnected(this)) {
                    Global.replaceFragment(FragmentLanguage.newInstance(position + 1),
                            getSupportFragmentManager(),
                            false);
                } else {
                    //no internet connection
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                break;

            case 3: //change password
                if (InternetConnection.isConnected(this)) {
                    Global.replaceFragment(FragmentChangePassword.newInstance(position + 1),
                        getSupportFragmentManager(),
                        false);
                } else {
                    //no internet connection
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                break;
            case 4: //edit profile
                if (InternetConnection.isConnected(this)) {
                    Global.replaceFragment(FragmentEditProfile.newInstance(position + 1),
                            getSupportFragmentManager(),
                            false);
                } else {
                    //no internet connection
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                break;
            case 5: //plan settings
                if (InternetConnection.isConnected(this)) {
                    Global.replaceFragment(FragmentPlanSettings.newInstance(position + 1),
                            getSupportFragmentManager(),
                            false);
                } else {
                    //no internet connection
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                break;
            case 6: //help
                Global.replaceFragment(FragmentHelp.newInstance(position + 1),
                        getSupportFragmentManager(),
                        false);
                break;
            case 7: //logout
                //set title of section
                mTitle = getString(R.string.title_section_logout);
                restoreActionBar();

                //create alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
                builder.setMessage(getString(R.string.alert_dialog_logout_text))
                        .setTitle(getString(R.string.alert_dialog_logout_title));

                builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //clear token
                        Global.clearSharedPreferences(ActivityNavigation.this);

                        //Log out FB
                        LoginManager.getInstance().logOut();
                        dialog.dismiss();

                        //start authorization activity
                        Intent myIntent = new Intent(ActivityNavigation.this, ActivityAuthorization.class);

                        ActivityNavigation.this.startActivity(myIntent);
                        //close this activity
                        ActivityNavigation.this.finish();

                    }
                });

                builder.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });

                AlertDialog dialog = builder.create();
                //AppCompatDialog dialog = builder.create();
                dialog.show();

                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section_channels);
                break;
            case 2:
                mTitle = getString(R.string.title_section_time_zone);
                break;
            case 3:
                mTitle = getString(R.string.title_section_language);
                break;
            case 4:
                mTitle = getString(R.string.title_section_change_password);
                break;
            case 5:
                mTitle = getString(R.string.title_section_edit_profile);
                break;
            case 6:
                mTitle = getString(R.string.plan_settings_title);
                break;
            case 7:
                mTitle = getString(R.string.title_section_help);
                break;
            case 8:
                mTitle = getString(R.string.title_section_logout);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mTitle != null) {
            setTitle(mTitle);
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.container).getLayoutParams();
        ActionBar actionBar = getSupportActionBar();
        if (Global.isTablet(this) &&
                getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_LANDSCAPE) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            mDrawerLayout.setScrimColor(Color.TRANSPARENT);
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
            params.leftMargin = 240;
            mDrawerLayout.clearFocus();
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerLayout.closeDrawers();
            mDrawerLayout.setScrimColor(0x99000000);
            actionBar.setHomeButtonEnabled(true); // enable the button
            actionBar.setDisplayHomeAsUpEnabled(true); // add the left caret
            actionBar.setDisplayShowHomeEnabled(true); // add the icon
            params.leftMargin = 0;
        }
    }
}
