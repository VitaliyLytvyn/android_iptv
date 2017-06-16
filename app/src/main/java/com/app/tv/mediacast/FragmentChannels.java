package com.app.tv.mediacast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.tv.mediacast.retrofit.data.DataChannelsByLang;

import java.util.ArrayList;


public class FragmentChannels extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ArrayList<DataChannelsByLang> dataChannelsList;

    //Views
    private ViewPager viewPager;

    //Adapters
    private ChannelsFragmentPagerAdapter mAdapter;

    public static FragmentChannels newInstance(int sectionNumber) {
        FragmentChannels fragment = new FragmentChannels();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentChannels() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_channels, container, false);

        if (ActivityLoading.channelsByLangsList == null) {

            Intent myIntent = new Intent(getActivity(), ActivityLoading.class);
            startActivity(myIntent);
            getActivity().finish();
        } else {
            dataChannelsList = ActivityLoading.channelsByLangsList;
            viewPager = (ViewPager) rootView.findViewById(R.id.pager);
            mAdapter = new ChannelsFragmentPagerAdapter(getFragmentManager());

            viewPager.setAdapter(mAdapter);

            //SlidingTabLayout ff = n
        }
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((ActivityNavigation) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private class ChannelsFragmentPagerAdapter extends FragmentStatePagerAdapter {
        final int PAGE_COUNT = dataChannelsList.size();

        public ChannelsFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return dataChannelsList.get(position).getLang();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentChannelsUkr.newInstance(/*position + 1*/);
//                case 1:
//                    return
//                case 2:
//                    return
                default:
                    return null;
            }
        }
    }
}





