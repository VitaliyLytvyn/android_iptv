package com.app.tv.mediacast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataChannelsByLang;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataUrl;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.app.tv.mediacast.ExoPlayerTools.DemoUtil;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.ERROR_SUBSCRIPTION_NOT_FOUND;


public class FragmentChannelsUkr extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private GridViewAdapter mAdapter;
    private ArrayList<DataChannelsByLang> dataChannelsList;
    OnDialogNeededListener dialogNeededListener;

    Call<DataUrl> call;

    @Inject
    Retrofit retrofit;

    @Override
    public void onStop() {
        if(call != null && !call.isCanceled()){
            call.cancel();
            call = null;
        }
        super.onStop();
    }

    public static FragmentChannelsUkr newInstance() {
        FragmentChannelsUkr fragment = new FragmentChannelsUkr();
        return fragment;
    }

    public FragmentChannelsUkr() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MyApplication) getActivity().getApplication()).getNetComponent().inject(this);

        try {
            dialogNeededListener = (OnDialogNeededListener)activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement OnDialogNeededListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalProgressDialog.dismiss();

        if (ActivityLoading.channelsByLangsList != null) {
            dataChannelsList = ActivityLoading.channelsByLangsList;
        } else {
            Toast.makeText(getActivity(), getString(R.string.failed_to_load_data_restart), Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(getActivity(), ActivityLoading.class);
            startActivity(myIntent);
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_channels_ukr, container, false);

        // initialize the adapter
        mAdapter = new GridViewAdapter(getActivity());

        // initialize the GridView
        GridView gridView = (GridView) fragmentView.findViewById(R.id.gridView_ua);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                //CHECK FOR ACCESS//
                if(ActivityNavigation.isAccessGranted){
                    loadAndShowChannel(position);
                } else {
                    dialogNeededListener.onDialogNeeded();
                }

            }
        });

        gridView.setAdapter(mAdapter);

        return fragmentView;
    }

    private void loadAndShowChannel(final int position){

        //CHECK FOR ACCESS
        if (!InternetConnection.isConnected(getActivity())) {
            //no internet connection
            Constant.toastIt(getActivity(), getString(R.string.no_internet_connection));
            return;
        }

        String idChannel = dataChannelsList.get(0).getChannels().get(position).getId();
        //Create a retrofit call object
        call = retrofit.create(Restapi.class).getLiveUrl(Constant.getHeaders(), idChannel);

        //Enqueue the call
        call.enqueue(new Callback<DataUrl>() {
            @Override
            public void onResponse(Call<DataUrl> call, Response<DataUrl> response) {

                if (response.isSuccessful()) {
                    // use response data and do some fancy stuff :)
                    String url = response.body().getUrl();
                    formIntentAndStartPlayerActivity(url, position);

                } else {
                    // parse the response body …
                    DataError error = Constant.parseError(retrofit, response);
                    if(error.getError() == null){
                        Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
                    } else if(error.getError().equals(ERROR_SUBSCRIPTION_NOT_FOUND)){
                        dialogNeededListener.onDialogNeeded();

                    }else {
                        Constant.getErrorString(getActivity(), error.getError());
                    }
                }
            }

            @Override
            public void onFailure(Call<DataUrl> call, Throwable t) {
                Constant.toastIt(getActivity(), getString(R.string.user_info_fail_retry));
            }
        });

    }

    private void formIntentAndStartPlayerActivity(String url, int position){
        Class<?> playerActivityClass = ActivityExoPlayer.class;
        Intent mpdIntent = new Intent(getActivity(), playerActivityClass)
                .setData(Uri.parse(url))
                .putExtra(ActivityExoPlayer.CONTENT_ID_EXTRA, Integer.toString(url.hashCode()))
                .putExtra(ActivityExoPlayer.CONTENT_TYPE_EXTRA, DemoUtil.TYPE_HLS);


        mpdIntent.putExtra("Url", url);
        mpdIntent.putExtra("ChannelName", dataChannelsList.get(0).getChannels().get(position).getName());
        mpdIntent.putExtra("Lang", 0);
        mpdIntent.putExtra("ChannelId", dataChannelsList.get(0).getChannels().get(position).getId());
        mpdIntent.putExtra("Position", position);
        startActivity(mpdIntent);
    }


    public class GridViewAdapter extends BaseAdapter {
        private Context mContext;

        final List<String> urls = new ArrayList<>();

        public GridViewAdapter(Context context) {
            mContext = context;
            if (dataChannelsList != null) {
                for (int i = 0; i < dataChannelsList.get(0).getChannels().size(); i++) {
                    urls.add(Constant.HOST  + dataChannelsList.get(0).getChannels().get(i).getLogo());
                }
            }
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object getItem(int position) {
            return urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SquaredImageView view = (SquaredImageView) convertView;
            String url = urls.get(position);

            if (view == null) {
                view = new SquaredImageView(mContext);
                //view.setImageBitmap(dataChannelsList.get(0).getChannelArrayList().get(position).getImage());
            }

            Picasso.with(getActivity())
                    .load(url)
                    .fit()
                    .noFade()
                    //.placeholder(R.drawable.loading_placeholder)
                    .placeholder(R.drawable.loading_placeholder_2_fullsize)
                    .into(view);
            return view;

        }
    }
}
