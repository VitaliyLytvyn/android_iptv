package com.app.tv.mediacast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.app.tv.mediacast.info.InfoMainActivity;
import com.app.tv.mediacast.info.InfoSelectPriceActivity;
import com.app.tv.mediacast.util.GlobalProgressDialog;

/**
 * Created by skyver on 1/23/17.
 */

public class FragmentPlanSettingsRenew extends Fragment {

    public static FragmentPlanSettingsRenew newInstance() {
        FragmentPlanSettingsRenew fragment = new FragmentPlanSettingsRenew();
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.plan_settings_renew_layout, container, false);

        Button mNewPlanButton = (Button)view.findViewById(R.id.buttonPickNewPlan);
        mNewPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InfoSelectPriceActivity.class);
                intent.putExtra(InfoMainActivity.RENEW, true);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalProgressDialog.dismiss();
    }

}
