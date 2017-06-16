package com.app.tv.mediacast;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataUserPlanData;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by skyver on 1/18/17.
 */

public class FragmentPlanSettings extends Fragment
        implements FragmentPlanSettingsCurrent.OnCancelSubscriptionListener{

    private static final String ARG_SECTION_NUMBER = "section_number";

    private View mView;
    private boolean isDownloading = false;
    Bundle mBundle = null;

    static DataUserPlanData mDataUserPlanData;
    DataError mError;
    Call<DataUserPlanData> call;

    @Inject
    Retrofit retrofit;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MyApplication) getActivity().getApplication()).getNetComponent().inject(this);
        ((ActivityNavigation) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onStop() {
        if(call != null && !call.isCanceled()){
            call.cancel();
            call = null;
        }
        //finishProgress();
        super.onStop();
    }


    public static FragmentPlanSettings newInstance(int sectionNumber) {
        FragmentPlanSettings fragment = new FragmentPlanSettings();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentPlanSettings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finishProgress();

        mBundle = savedInstanceState;

        if(savedInstanceState == null){
            mDataUserPlanData = null;
            getUserPlanSettings();
        } else if(mDataUserPlanData == null){
            getUserPlanSettings();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.plan_settings_base, container, false);

        if(!isDownloading && mDataUserPlanData!= null){
            chooseLayout();
        }
        return mView;
    }

    private void startProgress() {
        GlobalProgressDialog.show(getActivity(), getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private void chooseLayout(){

        //we are before onCreateView() so -> return
        if(mView == null){
            return;
        }

        if(mView.findViewById(R.id.planSettingsFrame) != null
                && mBundle == null){
            if(mDataUserPlanData.getStatus().equals(Constant.STATUS_SUCCESS)){
                mDataUserPlanData.setStart(formatDate(mDataUserPlanData.getStart()));
                mDataUserPlanData.setEnd(formatDate(mDataUserPlanData.getEnd()));
                FragmentPlanSettingsCurrent current = FragmentPlanSettingsCurrent.newInstance();
                getChildFragmentManager().beginTransaction()
                        .add(R.id.planSettingsFrame, current).commit();

            } else if(mDataUserPlanData.getStatus().equals(Constant.STATUS_ERROR)) {
                if(mError.getError()
                        .equals(Constant.ERROR_SUBSCRIPTION_NOT_EXISTS)){
                    FragmentPlanSettingsRenew renew = FragmentPlanSettingsRenew.newInstance();
                    getChildFragmentManager().beginTransaction()
                        .add(R.id.planSettingsFrame, renew).commit();

                }
            }
        }

    }

    private void getUserPlanSettings() {

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }
        startProgress();
        isDownloading = true;

        //Create a retrofit call object
        call = retrofit.create(Restapi.class).showUserPlanData(Constant.getHeaders());

        //Enqueue the call
        call.enqueue(new Callback<DataUserPlanData>() {
            @Override
            public void onResponse(Call<DataUserPlanData> call, Response<DataUserPlanData> response) {
                finishProgress();

                if (response.isSuccessful()) {

                    /**
                     *
                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                     *
                     */

                    isDownloading = false;
                    chooseLayout();

                } else {
                    // parse the response body …
                    mError = Constant.parseError(retrofit, response);
                    if(mError.getError() == null){
                        Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
                    } else {
                        mDataUserPlanData = new DataUserPlanData();
                        mDataUserPlanData.setStatus(Constant.STATUS_ERROR);

                        isDownloading = false;
                        chooseLayout();
                    }
                }
            }

            @Override
            public void onFailure(Call<DataUserPlanData> call, Throwable t) {
                isDownloading = false;
                finishProgress();
                Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
            }
        });

    }

    private String formatDate(String row){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Date date = null;
        String formattedDate = null;
        try {
            date = format.parse(row);

            DateFormat format2 = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.getDefault());
            formattedDate  = format2.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate;
    }

    @Override
    public void onCancelSubscription() {
        if(mView.findViewById(R.id.planSettingsFrame) != null){

                FragmentPlanSettingsRenew renew = FragmentPlanSettingsRenew.newInstance();

                //getFragmentManager().beginTransaction()
            getChildFragmentManager().beginTransaction()
                        .replace(R.id.planSettingsFrame, renew).commit();

        }
    }
}
