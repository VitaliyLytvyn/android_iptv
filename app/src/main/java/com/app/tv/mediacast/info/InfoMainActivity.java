package com.app.tv.mediacast.info;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.tv.mediacast.ActivityAuthorization;
import com.app.tv.mediacast.ActivityLoading;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

public class InfoMainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener, FragmentInfoTable.OnJoinButtonResultListener {


    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView mViewHead;
    public static final String RENEW = "renew";

    private boolean isRenew = false;


    private static final int[] tabIcons = {
            R.drawable.ic_logout_05,
            R.drawable.ic_tv_lable_05,
            R.drawable.ic_price_tag_05
    };
    private static final int[] tabIconsBlue = {
            R.drawable.ic_blue_logout_05,
            R.drawable.ic_blue_tv_lable_05,
            R.drawable.ic_blue_price_tag_05
    };


    ///////////////////todo when vector draw
//    private static final int[] tabIcons = {
//            R.drawable.icv_tab_logout_black,
//            R.drawable.icv_tab_tv_black,
//            R.drawable.icv_tab_price_tag_black
//    };
//    private static final int[] tabIconsBlue = {
//            R.drawable.icv_tab_logout_blue,
//            R.drawable.icv_tab_tv_blue,
//            R.drawable.icv_tab_price_tag_blue
//    };

    private  final Drawable[] tabIconsD = new Drawable[tabIcons.length];
    private  final Drawable[] tabIconsBlueD = new Drawable[tabIcons.length];

    private int mCurrentPosition = 0;

    public Button btnJoin;
    public Button btnLogin;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RENEW, isRenew);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        //CHECKING if this is renew case
        if(savedInstanceState != null){
            isRenew = savedInstanceState.getBoolean(RENEW, false);
        } else {
            isRenew = getIntent().getBooleanExtra(RENEW, false);
        }

        if(!isRenew){
            String token = Global.getSharedPreferences(this, Constant.KEY_TOKEN_AUTH);

            if(!token.equals(Constant.NULL_STRING)){
                //user logged in so go in
                Intent myIntent = new Intent(this, ActivityLoading.class);
                startActivity(myIntent);
                finish();

                return;
            }
        }

        setContentView(R.layout.info_main_layout);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(this);

        btnJoin = (Button) findViewById(R.id.buttonJoin);
        btnLogin = (Button) findViewById(R.id.buttonLogin);

        mViewHead = (TextView)findViewById(R.id.textViewHead);
        mViewHead.setText(getResources().getString(R.string.info_header_main));


        //populateTabArrays();//todo when vector draw
        setupTabIcons();
    }

    //todo when vector draw
    private void populateTabArrays(){
        for(int i=0; i < tabIcons.length; i++){
            tabIconsD[i] = AppCompatResources.getDrawable(this, tabIcons[i] );
            tabIconsBlueD[i] = AppCompatResources.getDrawable(this, tabIconsBlue[i] );
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void buttonJoinClicked(View v){
        Intent myIntent = new Intent(InfoMainActivity.this, InfoSelectPriceActivity.class);
        myIntent.putExtra(RENEW, isRenew);
        startActivity(myIntent);
    }

    public void buttonLoginClicked(View v){

        Intent myIntent = new Intent(InfoMainActivity.this, ActivityAuthorization.class);
        myIntent.putExtra(RENEW, isRenew);
        startActivity(myIntent);

    }

    private void setupTabIcons() {

        mCurrentPosition = tabLayout.getSelectedTabPosition();
        TextView tab0;
        tab0= (TextView) LayoutInflater.from(this).inflate(R.layout.info_custom_tab, null);

        tab0.setText(getResources().getString(R.string.info_tab0_tabtext));
        tab0.setTextColor(getResources().getColor(R.color.color_selected_tab));

        tab0.setCompoundDrawablesWithIntrinsicBounds(0, tabIconsBlue[0], 0, 0);
        //tab0.setCompoundDrawablesWithIntrinsicBounds(null, tabIconsBlueD[0], null, null);//todo when vector draw

        TabLayout.Tab tab;
        tab = tabLayout.getTabAt(0);
        if(tab != null){
            tab.setCustomView(tab0);
        }

        TextView tab1;
        tab1 = (TextView) LayoutInflater.from(this).inflate(R.layout.info_custom_tab, null);
        tab1.setText(getResources().getString(R.string.info_tab1_tabtext));
        tab1.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[1], 0, 0);
        //tab1.setCompoundDrawablesWithIntrinsicBounds(null, tabIconsD[1], null, null);//todo when vector draw

        tab = tabLayout.getTabAt(1);
        if(tab != null){
            tab.setCustomView(tab1);
        }

        TextView tab2;
        tab2 = (TextView) LayoutInflater.from(this).inflate(R.layout.info_custom_tab, null);
        tab2.setText(getResources().getString(R.string.info_tab2_tabtext));

        tab2.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[2], 0, 0);
        //tab2.setCompoundDrawablesWithIntrinsicBounds(null, tabIconsD[2], null, null);//todo when vector draw

        tab = tabLayout.getTabAt(2);
        if(tab != null){
            tab.setCustomView(tab2);
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        InfoViewPagerAdapter adapter = new InfoViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentInfoZero(), "");
        adapter.addFragment(new FragmentInfoOne(), "");
        adapter.addFragment(FragmentInfoTwo.newInstance(false), "");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        int selectedPosition = tabLayout.getSelectedTabPosition();
        if(mCurrentPosition == selectedPosition || selectedPosition < 0 || selectedPosition > 2){
            return;
        }

        TextView txt=null;
        TabLayout.Tab tabCurrent =  tabLayout.getTabAt(mCurrentPosition);
        if(tabCurrent != null){
            txt = (TextView) tabCurrent.getCustomView();
        }
        if(txt != null){
            txt.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[mCurrentPosition], 0, 0);
            //txt.setCompoundDrawablesWithIntrinsicBounds(null, tabIconsD[mCurrentPosition], null, null);//todo when vector draw
            txt.setTextColor(getResources().getColor(R.color.text_table_dark));
        }

        txt = (TextView)tab.getCustomView();
        if(txt != null){
            txt.setCompoundDrawablesWithIntrinsicBounds(0, tabIconsBlue[selectedPosition], 0, 0);
            //txt.setCompoundDrawablesWithIntrinsicBounds(null, tabIconsBlueD[selectedPosition], null, null);//todo when vector draw
            txt.setTextColor(getResources().getColor(R.color.color_selected_tab));
        }

        mCurrentPosition = selectedPosition;

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onJoinButtonResult(int position, int selected) {
        buttonJoinClicked(null);
    }
}