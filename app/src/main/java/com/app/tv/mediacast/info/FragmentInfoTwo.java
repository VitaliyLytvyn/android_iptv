package com.app.tv.mediacast.info;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.app.tv.mediacast.retrofit.data.Data;
import com.app.tv.mediacast.retrofit.data.Package;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.InternetConnection;
import com.app.tv.mediacast.MyApplication;
import com.app.tv.mediacast.R;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataPlan;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by skyver on 11/23/16.
 */

public class FragmentInfoTwo extends Fragment {

    private RadioGroup radioGroup1;
    private ViewPager viewPager;
    private RadioButton rbMonth, rbYears;
    private View mV;
    private boolean isSelectable;
    private ProgressBar progressBar;

    public static List<DataPlan> dataPlanList;
    Call<List<DataPlan>> call;

    @Inject
    Retrofit retrofit;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MyApplication) getActivity().getApplication()).getNetComponent().inject(this);
    }

    @Override
    public void onStop() {

        if(call != null && !call.isCanceled()){
            call.cancel();
            call = null;
        }
        finishProgress();
        super.onStop();
    }

    public static FragmentInfoTwo newInstance(boolean isSelectable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("select", isSelectable);

        FragmentInfoTwo fragment = new FragmentInfoTwo();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            this.isSelectable = bundle.getBoolean("select");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mV = inflater.inflate(R.layout.info_fragment_two, container, false);

        readBundle(getArguments());
        initView(mV);

        if(dataPlanList == null || dataPlanList.isEmpty()){
            getAllPlans();

        } else {
            setupViewPager(viewPager);
        }

        return mV;
    }

    private void setYearlyButtonText(boolean isChecked) {
        if (isChecked) {
            CharSequence text1 = Global.textSize(getResources().getString(R.string.button_save));
            CharSequence text = TextUtils.concat(text1,
                    getResources().getString(R.string.button_yearly_prising));

            rbYears.setText(Global.bold(text));

        } else {
            //make red, littlesized words
            CharSequence text1 = Global.textSize(Global.color(getResources().getColor(R.color.red_dark),
                    getResources().getString(R.string.button_save)));
            CharSequence text = TextUtils.concat(text1,
                    getResources().getString(R.string.button_yearly_prising));

            rbYears.setText(Global.bold(text));
        }

    }

    private void initView(View v) {
        progressBar = (ProgressBar) v.findViewById(R.id.progressBarLoadTable);

        rbMonth = (RadioButton) v.findViewById(R.id.radioMonth);
        rbYears = (RadioButton) v.findViewById(R.id.radioiYear);

        //make red, littlesized words
        CharSequence text1 = Global.textSize(Global.color(getResources().getColor(R.color.red_dark),
                getResources().getString(R.string.button_save)));
        CharSequence text = TextUtils.concat(text1,
                getResources().getString(R.string.button_yearly_prising));

        rbYears.setText(Global.bold(text));

        radioGroup1 = (RadioGroup) v.findViewById(R.id.radioGroup1);
        radioGroup1.setEnabled(false);

        // Checked change Listener for RadioGroup 1
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioMonth:
                        viewPager.setCurrentItem(0, false);
                        setYearlyButtonText(false);
                        break;
                    case R.id.radioiYear:
                        viewPager.setCurrentItem(1, false);
                        setYearlyButtonText(true);
                        break;
                    default:
                        break;
                }
            }
        });

        viewPager = (ViewPager) v.findViewById(R.id.viewpagerMonthYear);

        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        radioGroup1.check(R.id.radioMonth);
                        break;
                    case 1:
                        radioGroup1.check(R.id.radioiYear);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        InfoViewPagerAdapter adapter = new InfoViewPagerAdapter(getChildFragmentManager());

        //'0' and '1' are position of monthly and yearly plans respectevly in infoPlans List
        adapter.addFragment(FragmentInfoTable.newInstance(isSelectable, 0), "");
        adapter.addFragment(FragmentInfoTable.newInstance(isSelectable, 1), "");

        viewPager.setAdapter(adapter);

        radioGroup1.setEnabled(true);
    }

    private void getAllPlans() {



        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }
        startProgress();

        //Create a retrofit call object
        call = retrofit.create(Restapi.class).getListOfPlans(Constant.getHeaders());

        //Enqueue the call
        call.enqueue(new Callback<List<DataPlan>>() {
            @Override
            public void onResponse(Call<List<DataPlan>> call, Response<List<DataPlan>> response) {
                 finishProgress();

                if (response.isSuccessful()) {
                    // use response data and do some fancy stuff :)
                    dataPlanList = response.body();
                    setupViewPager(viewPager);

                } else {
                    // parse the response body …
                    DataError error = Constant.parseError(retrofit, response);
                    if(error.getError() == null){
                        Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
                    } else {
                        Constant.getErrorString(getActivity(), error.getError());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DataPlan>> call, Throwable t) {
                finishProgress();
                Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
            }
        });

    }

    private void startProgress() {
        mV.findViewById(R.id.LinearLayoutFragTwo).setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(2);
    }

    private void finishProgress() {
        mV.findViewById(R.id.LinearLayoutFragTwo).setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

}