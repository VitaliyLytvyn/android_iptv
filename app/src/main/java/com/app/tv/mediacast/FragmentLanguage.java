package com.app.tv.mediacast;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
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
import com.app.tv.mediacast.retrofit.data.DataAppLanguages;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.LANGUAGE;


public class FragmentLanguage extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private ArrayList<String> dataLanguageList;
    private ListView mListView;
    private CheckedListItemAdapter adapter;
    private Locale locale;
    private Button selectButton;

    //selectedLanguage and savedLanguage as "Українська"
    private String selectedLanguage = "";
    private String savedLanguage = "";

    private DataAppLanguages mDataLang;
    Call<Void> call;
    Call<DataAppLanguages> call2;

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


    public static FragmentLanguage newInstance(int sectionNumber) {
        FragmentLanguage fragment = new FragmentLanguage();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentLanguage() {
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
        View view = inflater.inflate(R.layout.fragment_language, container, false);

        mListView = (ListView) view.findViewById(R.id.listView_languages);

        getLanguages();

        savedLanguage = Global.getSharedPreferences(getActivity(), Constant.KEY_LANGUAGE);
        if(savedLanguage.equals(Constant.NULL_STRING)){

            String tmp=Locale.getDefault().toString().substring(0, 2);
            if(tmp.equalsIgnoreCase("uk") || tmp.equalsIgnoreCase("ru")){
                savedLanguage = getLanguageFromLocale(tmp);
            } else {
                savedLanguage = Constant.LANGUAGE_ENGLISH;
            }
        }

        selectedLanguage = savedLanguage;

        selectButton = (Button) view.findViewById(R.id.button_languages);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButtonClicked();
            }
        });

        return view;
    }

    private void selectButtonClicked(){
        if (selectedLanguage.equals(savedLanguage)) {
            return;
        }

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }
        controlsEnable(false);
        startProgress();

        Map<String, String> mapOfData = new HashMap<>();
        mapOfData.put(LANGUAGE, getLanguageUser(selectedLanguage));

        //Create a retrofit call object
        call = retrofit.create(Restapi.class).setUserLanguage(Constant.getHeaders(), mapOfData);

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

    private void getLanguages() {

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }
        startProgress();

        //Create a retrofit call object
        call2 = retrofit.create(Restapi.class).getAppLanguages(Constant.getHeaders());

        //Enqueue the call
        call2.enqueue(new Callback<DataAppLanguages>() {
            @Override
            public void onResponse(Call<DataAppLanguages> call, Response<DataAppLanguages> response) {
                finishProgress();

                if (response.isSuccessful()) {
                    // use response data and do some fancy stuff :)
                    mDataLang = response.body();
                    onGetLangSetAdapter();

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
            public void onFailure(Call<DataAppLanguages> call, Throwable t) {
                finishProgress();
                Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
            }
        });

    }

    private void onGetLangSetAdapter(){
        dataLanguageList = new ArrayList<>();

        dataLanguageList.add(mDataLang.getUa());
        dataLanguageList.add(mDataLang.getRu());
        dataLanguageList.add(mDataLang.getEn());

        adapter = new CheckedListItemAdapter(
                getActivity(),
                R.layout.listview_item,
                dataLanguageList);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguage = dataLanguageList.get(position);
            }
        });

        mListView.setAdapter(adapter);
    }

    private void controlsEnable(boolean ifEnable){
        mListView.setEnabled(ifEnable);
        selectButton.setEnabled(ifEnable);
    }

    private class CheckedListItemAdapter extends ArrayAdapter<String> {
        private Activity myContext;
        private ArrayList<String> datas;
        private View savedView;


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

            if (datas.get(position).equals(selectedLanguage)) {
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
                        //clear last clicked image

                        if (savedView != null) {
                            savedView.setVisibility(View.INVISIBLE);
                        }

                        selectedLanguage = viewHolder.postNameView.getText().toString();
                        viewHolder.postSelectedMarkTextView.setVisibility(View.VISIBLE);
                        savedView = viewHolder.postSelectedMarkTextView;

                        adapter.notifyDataSetChanged();

                        ////Change color and text on Button
                        if(selectedLanguage.equals(savedLanguage)){
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

    private static class ViewHolder {
        TextView postNameView;
        TextView postSelectedMarkTextView;
    }

    public static String getLanguageLocale(String in) {

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

        return in;

    }

    public static String getLanguageFromLocale(String in) {

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

        return in;

    }

    private String getLanguageUser(String in) {

        /**
         *
         * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
         *
         */

        return in;
    }

    private void setLocale(String localeCode) {
//        Log.d(TAG + "set location function: " + localeCode);
        locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());
        getActivity().getApplicationContext().getResources().updateConfiguration(config, getActivity().getApplicationContext().getResources().getDisplayMetrics());
        getActivity().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());
        onCreate(null);
    }

    private void startProgress() {
        GlobalProgressDialog.show(getActivity(), getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }
}