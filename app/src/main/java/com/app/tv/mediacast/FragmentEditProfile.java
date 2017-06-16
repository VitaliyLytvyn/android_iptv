package com.app.tv.mediacast;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.*;
import com.app.tv.mediacast.retrofit.data.User;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.FIRST_NAME;
import static com.app.tv.mediacast.util.Constant.LANGUAGE;
import static com.app.tv.mediacast.util.Constant.LAST_NAME;
import static com.app.tv.mediacast.util.Constant.PHONE;
import static com.app.tv.mediacast.util.Constant.TIME_ZONE;


public class  FragmentEditProfile extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    //views
    private EditText editTextName;
    private EditText editTextSurname;
    private EditText editTextPhone;
    private Button button;

    private boolean isDownloading;

    Call<Void> call;
    Call<DataUserDetails> call2;

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
        if(call2 != null && !call2.isCanceled()){
            call2.cancel();
            call2 = null;
        }
        finishProgress();
        super.onStop();
    }

    public static FragmentEditProfile newInstance(int sectionNumber) {
        FragmentEditProfile fragment = new FragmentEditProfile();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentEditProfile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalProgressDialog.dismiss();

        String name = Global.getSharedPreferences(getActivity(), Constant.KEY_NAME);
        if(name.equals(Constant.NULL_STRING)){
            goForUserDetails();
            isDownloading = true;
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editTextName = (EditText) view.findViewById(R.id.editText_edit_user_name);
        editTextSurname = (EditText) view.findViewById(R.id.editText_edit_user_surname);
        editTextPhone = (EditText) view.findViewById(R.id.editText_edit_user_phone);
        button = (Button) view.findViewById(R.id.button_edit_user_confirm);


        if(!isDownloading && savedInstanceState == null){

            populateFields();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //hide keyBoard
                Global.hideKeyBoard(getActivity(), v);

                //CHECK FOR ACCESS
                if (!InternetConnection.isConnected(getActivity())) {
                    //no internet connection
                    Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
                    return;
                }
                controlsEnable(false);
                startProgress();

                /**
                 *
                 * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                 *
                 */

                Map<String, String> mapOfData = new HashMap<>();
                /**
                 *
                 * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                 *
                 */
                //Create a retrofit call object
                call = retrofit.create(Restapi.class).updateUser(Constant.getHeaders(), mapOfData);

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
        });


        return view;
    }

    private void populateFields(){

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

    }

    private void controlsEnable(boolean ifEnable){
        editTextName.setEnabled(ifEnable);
        editTextSurname.setEnabled(ifEnable);
        editTextPhone.setEnabled(ifEnable);
        button.setEnabled(ifEnable);
    }

    private String getLocale(){
        String savedLanguage = Global.getSharedPreferences(getActivity(), Constant.KEY_LOCALE);
        if(savedLanguage.equals(Constant.NULL_STRING)){

            String tmp=Locale.getDefault().toString().substring(0, 2);
            if(tmp.equalsIgnoreCase("uk")) {
                return "ua";
            } else if(tmp.equalsIgnoreCase("ru")){
                return "ru";
            } else {
                return "en";
            }

        } else {
            return savedLanguage;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(isDownloading){
            controlsEnable(false);
            startProgress();
        }
    }

    private void startProgress() {
        GlobalProgressDialog.show(getActivity(), getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private void goForUserDetails(){

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }
        startProgress();

        //Create a retrofit call object
        call2 = retrofit.create(Restapi.class).showUserDetails(Constant.getHeaders());

        //Enqueue the call
        call2.enqueue(new Callback<DataUserDetails>() {
            @Override
            public void onResponse(Call<DataUserDetails> call, Response<DataUserDetails> response) {
                finishProgress();
                isDownloading = false;
                controlsEnable(true);

                if (response.isSuccessful()) {


                    /**
                     *
                     * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                     *
                     */


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
            public void onFailure(Call<DataUserDetails> call, Throwable t) {
                finishProgress();
                isDownloading = false;
                controlsEnable(true);
                Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
            }
        });

    }

}


