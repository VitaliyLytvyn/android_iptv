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
import com.app.tv.mediacast.retrofit.data.DataAuthToken;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.NEW_PASSWORD;


public class FragmentChangePassword extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";

//    private OnFragmentInteractionListener mListener;

    private EditText editTextNewPass;
    private EditText editTextNewPassrepeat;
    private Button button;

    Call<DataAuthToken> call;

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
        finishProgress();
        super.onStop();
    }
    public static FragmentChangePassword newInstance(int sectionNumber) {
        FragmentChangePassword fragment = new FragmentChangePassword();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentChangePassword() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finishProgress();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        editTextNewPass = (EditText) view.findViewById(R.id.editText_change_pass_new_pass);
        editTextNewPassrepeat = (EditText) view.findViewById(R.id.editText_change_pass_repeat_pass);
        button = (Button) view.findViewById(R.id.button_change_pass_confirm);

        final ArrayList<EditText> editTexts = new ArrayList<>();
        editTexts.add(editTextNewPass);
        editTexts.add(editTextNewPassrepeat);

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
                boolean empty = false;
                //check all editTexts for emptiness
                for (EditText e : editTexts) {
                    if (e.getText().toString().equals("")) {
                        empty = true;
                    }
                }
                if (empty) {//not all field are filled with text
                    Constant.toastIt(getActivity(), getString(R.string.fill_all_edittexts));
                    return;
                }
                //check if new password and repeated one match
                String newPassField = editTextNewPass.getText().toString().trim();
                String newPassRepeatField = editTextNewPassrepeat.getText().toString().trim();
                if (!newPassField.equals(newPassRepeatField)) {
                    Constant.toastIt(getActivity(), getString(R.string.repeated_pass_not_match));
                    return;
                }

                startProgress();
                controllsEnable(false);

                Map<String, String> mapOfData = new HashMap<>();

                /**
                 *
                 * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                 *
                 */

                //Create a retrofit call object
                call = retrofit.create(Restapi.class).changeUserPassword(Constant.getHeaders(), mapOfData);

                //Enqueue the call
                call.enqueue(new Callback<DataAuthToken>() {
                    @Override
                    public void onResponse(Call<DataAuthToken> call, Response<DataAuthToken> response) {
                        finishProgress();
                        controllsEnable(true);

                        if (response.isSuccessful()) {

                            /**
                             *
                             * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                             *
                             */
                            Constant.toastIt(getActivity(), getString(R.string.change_pass_success));

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
                    public void onFailure(Call<DataAuthToken> call, Throwable t) {
                        finishProgress();
                        controllsEnable(true);
                        Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
                    }
                });

            }
        });

        return view;
    }
    private void controllsEnable(boolean set){
        button.setEnabled(set);
        editTextNewPass.setEnabled(set);
        editTextNewPassrepeat.setEnabled(set);
    }

    private void startProgress() {
        GlobalProgressDialog.show(getActivity(), getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

}
