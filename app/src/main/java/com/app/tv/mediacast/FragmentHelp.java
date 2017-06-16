package com.app.tv.mediacast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.tv.mediacast.info.InfoShowTermsOfUseActivity;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;


public class FragmentHelp extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static FragmentHelp newInstance(int sectionNumber) {
        FragmentHelp fragment = new FragmentHelp();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentHelp() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalProgressDialog.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        final TextView email = (TextView) view.findViewById(R.id.textViewEmail13);
        email.setText(Constant.SITE_HELP_EMAIL);
        final TextView help = (TextView) view.findViewById(R.id.textView_help_contact_us);
        help.setText(Constant.SITE_URL);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InternetConnection.isConnected(getActivity())) {
                    //startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constant.SITE_URL)));
                    Intent myIntent = InfoShowTermsOfUseActivity.makeMyIntent(getActivity(), Constant.SITE_URL, false);
                    startActivity(myIntent);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((ActivityNavigation) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

    }
}
