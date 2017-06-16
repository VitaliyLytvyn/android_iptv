package com.app.tv.mediacast;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.app.tv.mediacast.databinding.PlanSettingsCurrentLayoutBinding;
import com.app.tv.mediacast.info.InfoMainActivity;
import com.app.tv.mediacast.info.InfoSelectPriceActivity;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.SUBSCRIPTION_ID;
import static com.app.tv.mediacast.FragmentPlanSettings.mDataUserPlanData;

/**
 * Created by skyver on 1/23/17.
 */

public class FragmentPlanSettingsCurrent extends Fragment {

    PlanSettingsCurrentLayoutBinding mBinding;
    private View mView;
    private TextView mUnsubscribe;
    private Button mUpgradeButton;
    private android.app.AlertDialog mDialog;

    static OnCancelSubscriptionListener cancelSubscriptionListener;
    public interface OnCancelSubscriptionListener{
        void onCancelSubscription();
    }

    Call<Void> call;

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
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
        //finishProgress();
        super.onStop();
    }

    public static FragmentPlanSettingsCurrent newInstance() {
        FragmentPlanSettingsCurrent fragment = new FragmentPlanSettingsCurrent();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finishProgress();

        // something wrong
        if(mDataUserPlanData  == null){
            onDetach();
        }

        onAttachMyParentFragment(getParentFragment());
    }

    private void onAttachMyParentFragment(Fragment fragment){
        try {
            cancelSubscriptionListener = (OnCancelSubscriptionListener)fragment;
        } catch (ClassCastException e){
            throw new ClassCastException(fragment.toString()
                    + " must implement OnCancelSubscriptionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.plan_settings_current_layout, container, false);
        mView = mBinding.getRoot();
        mUnsubscribe = (TextView)mView.findViewById(R.id.textViewCancelSubscription);

        mUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If subscribed on Apple device show dialog message
                if(mDataUserPlanData.getMerchant()
                        .equals(Constant.MERCHANT_APPLE)){

                    showAppleSubscribedDialog();
                } else {
                    controlsEnable(true);
                    showUnsubscribeDialog();
                }
            }
        });

        mUpgradeButton = (Button)mView.findViewById(R.id.buttonUpgrade);
        mUpgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), InfoSelectPriceActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(InfoMainActivity.RENEW, true);
                startActivity(intent);
            }
        });

        mBinding.setModel(mDataUserPlanData);
        return mView;
    }


    private void startProgress() {
        GlobalProgressDialog.show(getActivity(), getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private void startUnsubscribeProcess(){

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }
        startProgress();
        controlsEnable(false);

        Map<String, String> mapOfData = new HashMap<>();

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

        //Create a retrofit call object
        call = retrofit.create(Restapi.class).cancelSubscription(Constant.getHeaders(), mapOfData);

        //Enqueue the call
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                finishProgress();
                controlsEnable(true);

                if (response.isSuccessful()) {

                    /**
                     *
                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                     *
                     */

                    mDataUserPlanData = null;
                    cancelSubscriptionListener.onCancelSubscription();

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
            public void onFailure(Call<Void> call, Throwable t) {
                finishProgress();
                controlsEnable(true);
                Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
            }
        });

    }

    private void controlsEnable(boolean ifEnable){
        mUnsubscribe.setEnabled(ifEnable);
        mUpgradeButton.setEnabled(ifEnable);
    }

    private void showAppleSubscribedDialog(){
        //create apple subscribed notification dialog
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder
                (new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.subscribed_on_apple_dialog_message));
                //.setTitle(getString(R.string.alert_dialog_server_down_title));

        builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        mDialog = builder.create();
        mDialog.show();
    }

    private void showUnsubscribeDialog(){
        //create apple subscribed notification dialog
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder
                (new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.unsubscribe_dialog_message));
        //.setTitle(getString(R.string.alert_dialog_server_down_title));

        builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                startUnsubscribeProcess();
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

}
