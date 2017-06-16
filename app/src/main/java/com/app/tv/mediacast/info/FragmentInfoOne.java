package com.app.tv.mediacast.info;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.tv.mediacast.R;

/**
 * Created by skyver on 11/23/16.
 */

public class FragmentInfoOne extends Fragment {

    public FragmentInfoOne() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.info_fragment_one, container, false);

        v.findViewById(R.id.button_join_bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InfoMainActivity)getActivity()).buttonJoinClicked(view);
            }
        });

        return v;
    }

}
