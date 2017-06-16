package com.app.tv.mediacast;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataAppTimeZones;
import com.app.tv.mediacast.retrofit.data.DataError;
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

import static com.app.tv.mediacast.util.Constant.TIME_ZONE;

public class FragmentTimeZone extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private ListView mListView;
    private CheckedListItemAdapter adapter;
    private String selectedTimeZone;
    private String savedTimeZone;

    private Button selectButton;

    private DataAppTimeZones mDataTimezones;
    Call<Void> call;
    Call<DataAppTimeZones> call2;

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
        //finishProgress();
        super.onStop();
    }


    public static FragmentTimeZone newInstance(int sectionNumber) {
        FragmentTimeZone fragment = new FragmentTimeZone();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentTimeZone() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finishProgress();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_zone, container, false);

        mListView = (ListView) view.findViewById(R.id.listView_timezones);

        selectButton = (Button) view.findViewById(R.id.button_timezones);
        selectButton.setEnabled(false);
        selectButton.setVisibility(View.INVISIBLE);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButtonPressed();
            }
        });

        getTimeZones();

        savedTimeZone = Global.getSharedPreferences(getActivity(), Constant.KEY_TIMEZONE);
        if(savedTimeZone.equals(Constant.NULL_STRING)){
            savedTimeZone = Constant.TIME_ZONE_EUROPE_KIEV;
        }

        selectedTimeZone = savedTimeZone;
        return view;
    }

    private void getTimeZones() {

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }

        startProgress();

        //Create a retrofit call object
        call2 = retrofit.create(Restapi.class).getAppTimeZones(Constant.getHeaders());

        //Enqueue the call
        call2.enqueue(new Callback<DataAppTimeZones>() {
            @Override
            public void onResponse(Call<DataAppTimeZones> call, Response<DataAppTimeZones> response) {
                finishProgress();

                if (response.isSuccessful()) {
                    // use response data and do some fancy stuff :)
                    mDataTimezones = response.body();

                    onGetTimeListSetAdapter();
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
            public void onFailure(Call<DataAppTimeZones> call, Throwable t) {
                finishProgress();
                Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
            }
        });

    }

    private void onGetTimeListSetAdapter(){
        adapter = new CheckedListItemAdapter(
                getActivity(),
                R.layout.listview_item,
                mDataTimezones.getZonesList());


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTimeZone = mDataTimezones.getZonesList().get(position);
            }
        });
        mListView.setAdapter(adapter);
        selectButton.setVisibility(View.VISIBLE);
        selectButton.setEnabled(true);
    }

    private void selectButtonPressed(){
        if(selectedTimeZone.equals(savedTimeZone)){
            return;
        }

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }
        //prevent clicks during server request
        controlsEnable(false);
        startProgress();

        Map<String, String> mapOfData = new HashMap<>();
        mapOfData.put(TIME_ZONE, selectedTimeZone);
        //Create a retrofit call object
        call = retrofit.create(Restapi.class).setUserTimeZone(Constant.getHeaders(), mapOfData);

        //Enqueue the call
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                finishProgress();
                controlsEnable(true);

                if (response.isSuccessful()) {
                    // use response data and do some fancy stuff :)
                    selectButton.setText(R.string.timezones_select);
                    selectButton.setBackgroundColor(getResources().getColor(R.color.button_not_active_grey));
                    selectButton.setTextColor(getResources().getColor(R.color.white));

                    Constant.toastIt(getActivity(), getString(R.string.timezones_success));
                    Global.replaceSharedPreferences(getActivity(), Constant.KEY_TIMEZONE, selectedTimeZone);
                    savedTimeZone = selectedTimeZone;

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

    private class CheckedListItemAdapter extends ArrayAdapter<String> {
        private View savedView = null;
        private Activity myContext;
        private ArrayList<String> datas;


        public CheckedListItemAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
            super(context, textViewResourceId, objects);

            myContext = (Activity) context;
            datas = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = myContext.getLayoutInflater();

                convertView = inflater.inflate(R.layout.listview_item, null);

                viewHolder = new ViewHolder();
                viewHolder.postNameView = (TextView) convertView.findViewById(R.id.listview_item);
                viewHolder.postSelectedMarkTextView = (TextView) convertView.findViewById(R.id.selected_text_mark);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.postNameView.setText(datas.get(position));

            if (datas.get(position).equals(selectedTimeZone)) {
                viewHolder.postSelectedMarkTextView.setVisibility(View.VISIBLE);
                savedView = viewHolder.postSelectedMarkTextView;
            } else {
                viewHolder.postSelectedMarkTextView.setVisibility(View.INVISIBLE);
            }

            viewHolder.postNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    View parentRow = (View) v.getParent();
                    ViewHolder viewHolder = (ViewHolder) parentRow.getTag();

                    if (viewHolder.postSelectedMarkTextView.getVisibility() != View.VISIBLE) {

                        if (savedView != null) {
                            savedView.setVisibility(View.INVISIBLE);
                        }

                        selectedTimeZone = viewHolder.postNameView.getText().toString();
                        viewHolder.postSelectedMarkTextView.setVisibility(View.VISIBLE);
                        savedView = viewHolder.postSelectedMarkTextView;

                        adapter.notifyDataSetChanged();

                        ////Change color and text on Button
                        if(selectedTimeZone.equals(savedTimeZone)){
                            selectButton.setText(R.string.timezones_select);
                            selectButton.setBackgroundColor(getResources().getColor(R.color.button_not_active_grey));
                            selectButton.setTextColor(getResources().getColor(R.color.white));
                        } else {
                            selectButton.setText(R.string.timezones_button_savetext);
                            selectButton.setBackgroundColor(getResources().getColor(R.color.white));
                            selectButton.setTextColor(getResources().getColor(R.color.black));
                        }
                    }

                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        TextView postNameView;
        TextView postSelectedMarkTextView;
    }

    private void controlsEnable(boolean ifEnable){
        mListView.setEnabled(ifEnable);
        selectButton.setEnabled(ifEnable);
    }

    private void startProgress() {
        GlobalProgressDialog.show(getActivity(), getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }


}


