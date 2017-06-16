package com.app.tv.mediacast.info;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.R;

/**
 * Created by skyver on 12/20/16.
 */

public class FragmentInfoEmailDialog extends DialogFragment {

    private EditText editTextEmail;
    private String email;
    OnContinueButtonListener onContinueButtonListener;

    public interface OnContinueButtonListener{
        void onContinueButtonResult(String email);
    }
    /**Create a new instance of MyDialogFragment*/
    static FragmentInfoEmailDialog newInstance(Context cnt) {
        //context = cnt;
        FragmentInfoEmailDialog f = new FragmentInfoEmailDialog();
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onContinueButtonListener = (OnContinueButtonListener)activity;

        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement OnContinueButtonListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window widow= getDialog().getWindow();
        if(widow != null){
            widow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.InfoCreateAccTheme);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Constant.EMAIL, editTextEmail.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.info_fragment_dialog_email, container, false);

        editTextEmail = (EditText)view.findViewById(R.id.editText_input_email);

        Button button = (Button) view.findViewById(R.id.button_input_email);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hide keyBoard
                Global.hideKeyBoard(getActivity(), view);

                email = editTextEmail.getText().toString();
                //empty = true means some field is empty
                if (email.equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.fill_edittext),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // check if email correct
                if (Global.validateEmail(email)) {
                    onContinueButtonListener.onContinueButtonResult(email);

                    //finish this fragment
                    getActivity().getFragmentManager().beginTransaction()
                            .remove(FragmentInfoEmailDialog.this).commit();

                } else {
                    // EMAIL is NOT valid
                    Toast.makeText(getActivity(),
                            getString(R.string.email_not_valid), Toast.LENGTH_SHORT).show();
                    editTextEmail.setText("");
                    editTextEmail.setHint(getString(R.string.create_acc_hint_email));
                }

            }
        });

        View logo = view.findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //finish this fragment
                    getActivity().getFragmentManager().beginTransaction()
                            .remove(FragmentInfoEmailDialog.this).commit();

                    startActivity(new Intent(getActivity(), InfoMainActivity.class));
                    getActivity().finish();
                }
            });
        }

        return view;
    }
}
