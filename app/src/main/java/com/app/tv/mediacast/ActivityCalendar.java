package com.app.tv.mediacast;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.app.tv.mediacast.ExoPlayerTools.DemoUtil;
import com.app.tv.mediacast.info.InfoMainActivity;
import com.app.tv.mediacast.info.InfoSelectPriceActivity;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.app.tv.mediacast.retrofit.data.DataProgrammsByChannelDay;
import com.app.tv.mediacast.retrofit.data.DataUrl;
import com.app.tv.mediacast.retrofit.data.Program;
import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.ERROR_SUBSCRIPTION_NOT_FOUND;


public class ActivityCalendar extends AppCompatActivity {

    private static final int DATE_DIALOG_ID = 1;

    public static String[] MONTHS;

    //views
    private Button btnSelectDay;
    private ListView lwArchive;

    //datepicker
    private int year, month, day;
    private String currentDate;

    //vars
    private String channelId;
    private String url;
    private String title;
    private String oldTitle;
    private int lang;
    private boolean isArchive;
    private int positionInChannelList;

    private int mPositionInProgramList;
    private android.app.AlertDialog mDialog;

    //Channels statuses
    public static final String STATUS_BEFORE = "before";
    public static final String STATUS_AFTER = "after";
    public static final String STATUS_CURRENT_1 = "channel_current";
    public static final String STATUS_CURRENT_2 = "current";

    Call<DataProgrammsByChannelDay> call;
    Call<DataUrl> call2;

    @Inject
    Retrofit retrofit;

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
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
        finishProgress();
        super.onStop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar);
        ((MyApplication) getApplication()).getNetComponent().inject(this);

        setTitle(getString(R.string.archive_title));
        MONTHS = new String[]{
                getString(R.string.month_jan),
                getString(R.string.month_feb),
                getString(R.string.month_mar),
                getString(R.string.month_apr),
                getString(R.string.month_may),
                getString(R.string.month_jun),
                getString(R.string.month_jul),
                getString(R.string.month_aug),
                getString(R.string.month_sep),
                getString(R.string.month_oct),
                getString(R.string.month_nov),
                getString(R.string.month_dec)};

        Intent intent = getIntent();

        if (intent != null) {
            channelId = intent.getStringExtra("ChannelId");
            title = intent.getStringExtra("ChannelName");
            oldTitle = title;
            url = intent.getStringExtra("Url");
            lang = intent.getIntExtra("Lang", -1);
            positionInChannelList = intent.getIntExtra("Position", -1);
        }

        btnSelectDay = (Button) findViewById(R.id.calendar_button_select_day);
        lwArchive = (ListView) findViewById(R.id.calendar_listview);

        btnSelectDay.setText(getCurrentDay());

        //start retrieving program
        getArchiveByDayMethod(channelId, getCurrentDayFormatted());

        btnSelectDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                showDialog(DATE_DIALOG_ID);

            }
        });
    }

    DatePickerDialog.OnDateSetListener myDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int j, int k) {

            year = i;
            month = j;
            day = k;
            updateDisplay();
            btnSelectDay.setText(currentDate);

            String strMonth;
            String strYear = Integer.toString(year);
            String strDay;

            if (month < 9) {
                strMonth = "0" + Integer.toString(month + 1);
            } else {
                strMonth = Integer.toString(month + 1);
            }
            if (day < 10) {
                strDay = "0" + Integer.toString(day);
            } else {
                strDay = Integer.toString(day);
            }

            //start retrieving program
            mPositionInProgramList = 0;
            getArchiveByDayMethod(channelId, strYear + strMonth + strDay);
        }
    };

    private void updateDisplay() {
        currentDate = new StringBuilder().append(day).append(" ")
                .append(MONTHS[month]).append(" ").append(year).toString();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                DatePickerDialog dpd= new DatePickerDialog(this,
                        R.style.AlertDialogCustom,
                        myDateSetListener,
                        year,
                        month,
                        day);
                dpd.getDatePicker().setMaxDate(new Date().getTime());
                dpd.getDatePicker().setMinDate(new Date().getTime() -13 * 24 * 60 * 60 * 1000);
                return dpd;
        }
        return null;
    }

    @Override
    public void onBackPressed() {

        Class<?> playerActivityClass = ActivityExoPlayer.class;
        Intent mpdIntent = new Intent(ActivityCalendar.this, playerActivityClass)
                .setData(Uri.parse(url))
                .putExtra(ActivityExoPlayer.CONTENT_ID_EXTRA, Integer.toString(url.hashCode()))
                .putExtra(ActivityExoPlayer.CONTENT_TYPE_EXTRA, DemoUtil.TYPE_HLS);

        mpdIntent.putExtra("Url", url);
        mpdIntent.putExtra("ChannelName", title);
        mpdIntent.putExtra("Lang", lang);
        mpdIntent.putExtra("ChannelId", channelId);
        mpdIntent.putExtra("Position", positionInChannelList);
        startActivity(mpdIntent);
        finish();

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Class<?> playerActivityClass = ActivityExoPlayer.class;
                Intent mpdIntent = new Intent(ActivityCalendar.this, playerActivityClass)
                        .setData(Uri.parse(url))
                        .putExtra(ActivityExoPlayer.CONTENT_ID_EXTRA, Integer.toString(url.hashCode()))
                        .putExtra(ActivityExoPlayer.CONTENT_TYPE_EXTRA, DemoUtil.TYPE_HLS);

                mpdIntent.putExtra("Url", url);
                mpdIntent.putExtra("ChannelName", title);
                mpdIntent.putExtra("Lang", lang);
                mpdIntent.putExtra("ChannelId", channelId);
                mpdIntent.putExtra("Position", positionInChannelList);
                startActivity(mpdIntent);
                finish();
                return (true);
        }

        return (super.onOptionsItemSelected(item));
    }

    private void startProgress() {
        GlobalProgressDialog.show(this, getString(R.string.progress_spinner_message));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private String getCurrentDay() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        //get current date time with Date()
        Date date = new Date();
        String yourDate = dateFormat.format(date);

        return yourDate;
    }

    private String getCurrentDayFormatted() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        //get current date time with Date()
        Date date = new Date();
        String yourDate = dateFormat.format(date);

        return yourDate;
    }

    public static String getMyCurrentDayFormatted(String rowDate) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        Date date = null;
        String formattedDate = null;
        try {
            date = format.parse(rowDate);

            DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
            LocalTime ltt = new LocalTime(date);
            formattedDate = fmt.print(ltt);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate;
    }


    private class programsItemAdapter extends ArrayAdapter<Program> {
        private Activity myContext;
        private List<Program> datas;


        public programsItemAdapter(Context context, int textViewResourceId, List<Program> objects) {
            super(context, textViewResourceId, objects);

            myContext = (Activity) context;
            datas = objects;

            int counter = 0;
            for(Program res : datas){
                if(res.getStatus().equals(STATUS_CURRENT_1)
                        || res.getStatus().equals(STATUS_CURRENT_2)){
                    mPositionInProgramList = counter;
                    break;
                }
                counter++;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = myContext.getLayoutInflater();
                convertView = inflater.inflate(R.layout.channel_listview_item, null);

                viewHolder = new ViewHolder();
                viewHolder.postNameView = (TextView) convertView.findViewById(R.id.channel_listview_item_name);
                viewHolder.postTimeView = (TextView) convertView.findViewById(R.id.channel_listview_item_time);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //mark items with white and dark white
            String dataStatus = datas.get(position).getStatus();
            if (dataStatus.equalsIgnoreCase(STATUS_AFTER )) {

                viewHolder.postTimeView.setTextColor(getResources().getColor(R.color.darker_grey));
                viewHolder.postNameView.setTextColor(getResources().getColor(R.color.darker_grey));
            } else if (dataStatus.equalsIgnoreCase(STATUS_CURRENT_1)
                    || dataStatus.equalsIgnoreCase(STATUS_CURRENT_2)){
                viewHolder.postTimeView.setTextColor(getResources().getColor(R.color.blue_background));
                viewHolder.postNameView.setTextColor(getResources().getColor(R.color.blue_background));
            }else if (dataStatus.equalsIgnoreCase(STATUS_BEFORE)){
                viewHolder.postTimeView.setTextColor(getResources().getColor(R.color.white));
                viewHolder.postNameView.setTextColor(getResources().getColor(R.color.white));
            }
            viewHolder.postNameView.setText(datas.get(position).getTitle());
            viewHolder.postTimeView.setText(getMyCurrentDayFormatted(datas.get(position).getStart()));
            return convertView;
        }
    }

    static class ViewHolder {
        TextView postTimeView;
        TextView postNameView;
    }

    private void getArchiveByDayMethod(String id, String date){

        //CHECK FOR ACCESS
        if (InternetConnection.isConnected(this)) {
            startProgress();

            //Create a retrofit call object
            call = retrofit.create(Restapi.class).getProgrammsByChannelAndDay(Constant.getHeaders(), id, date);

            //Enqueue the call
            call.enqueue(new Callback<DataProgrammsByChannelDay>() {
                @Override
                public void onResponse(Call<DataProgrammsByChannelDay> call, Response<DataProgrammsByChannelDay> response) {

                    finishProgress();
                    if (response.isSuccessful()) {
                        // use response data and do some fancy stuff :)

                        DataProgrammsByChannelDay dataProgrammsByChannelDay = response.body();
                        setAdapter(dataProgrammsByChannelDay);

                    } else {
                        // parse the response body …
                        DataError error = Constant.parseError(retrofit, response);

                        if(error.getError() == null){
                            Constant.toastIt(ActivityCalendar.this, getString(R.string.user_info_fail_retry));
                        } else if(error.getError().equals(ERROR_SUBSCRIPTION_NOT_FOUND)){

                            showNoSubscriptionDialog();
                        }else {
                            Constant.getErrorString(ActivityCalendar.this, error.getError());
                        }
                    }
                }

                @Override
                public void onFailure(Call<DataProgrammsByChannelDay> call, Throwable t) {
                    finishProgress();
                    Constant.toastIt(ActivityCalendar.this, getString(R.string.user_info_fail_retry));
                }
            });
        } else {
            //no internet connection
            Constant.toastIt(ActivityCalendar.this, getString(R.string.no_internet_connection));
        }
    }

    private void setAdapter(DataProgrammsByChannelDay dataProgrammsByChannelDay){
        lwArchive.setAdapter(new programsItemAdapter(
                ActivityCalendar.this,
                R.layout.channel_listview_item,
                dataProgrammsByChannelDay.getPrograms()
        ));

        int d = mPositionInProgramList >= 2 ? mPositionInProgramList-2 : mPositionInProgramList;
        lwArchive.setSelection(d);

        final DataProgrammsByChannelDay dataPrograms1 = dataProgrammsByChannelDay;
        lwArchive.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (dataPrograms1.getPrograms()
                        .get(position).getStatus().equals(STATUS_AFTER)) {
                    Toast.makeText(ActivityCalendar.this,
                            getString(R.string.programs_program_is_after), Toast.LENGTH_LONG).show();
                } else {
                    title = dataPrograms1.getPrograms().get(position).getTitle();
                    isArchive = dataPrograms1.getPrograms()
                            .get(position).getStatus().equals(STATUS_BEFORE);
                    goToPlayer(dataPrograms1.getPrograms().get(position).getId());
                }
            }
        });

    }

    private void goToPlayer(final String programId) {
        //CHECK FOR ACCESS
        if (InternetConnection.isConnected(this)) {
            startProgress();

            //Create a retrofit call object
            call2 = retrofit.create(Restapi.class).getProgrammStreamUrl(Constant.getHeaders(), programId);

            //Enqueue the call
            call2.enqueue(new Callback<DataUrl>() {
                @Override
                public void onResponse(Call<DataUrl> call, Response<DataUrl> response) {

                    finishProgress();
                    if (response.isSuccessful()) {
                        // use response data and do some fancy stuff :)

                        DataUrl dataUrl = response.body();

                        Class<?> playerActivityClass = ActivityExoPlayer.class;
                        Intent mpdIntent = new Intent(ActivityCalendar.this, playerActivityClass)
                                .setData(Uri.parse(dataUrl.getUrl()))
                                .putExtra(ActivityExoPlayer.CONTENT_ID_EXTRA,
                                        Integer.toString(dataUrl.getUrl().hashCode()))
                                .putExtra(ActivityExoPlayer.CONTENT_TYPE_EXTRA, DemoUtil.TYPE_HLS);

                        mpdIntent.putExtra("Url", dataUrl.getUrl());
                        mpdIntent.putExtra("ChannelName", title);
                        mpdIntent.putExtra("Lang", lang);

                        mpdIntent.putExtra("IsArchive", isArchive);
                        mpdIntent.putExtra("Position", positionInChannelList);
                        mpdIntent.putExtra("ChannelId", channelId);
                        startActivity(mpdIntent);
                        finish();

                    } else {
                        // parse the response body …
                        DataError error = Constant.parseError(retrofit, response);
                        if(error.getError() == null){
                            Constant.toastIt(ActivityCalendar.this, getString(R.string.user_info_fail_retry));
                        } else if(error.getError().equals(ERROR_SUBSCRIPTION_NOT_FOUND)){

                            showNoSubscriptionDialog();
                        }else {
                            Constant.getErrorString(ActivityCalendar.this, error.getError());
                        }
                    }
                }

                @Override
                public void onFailure(Call<DataUrl> call, Throwable t) {
                    finishProgress();
                    Constant.toastIt(ActivityCalendar.this, getString(R.string.user_info_fail_retry));
                    title = oldTitle;
                }
            });
        } else {
            //no internet connection
            Constant.toastIt(ActivityCalendar.this, getString(R.string.no_internet_connection));
        }
    }

    private void showNoSubscriptionDialog(){
        //create no subscription notification dialog
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder
                (this, R.style.AlertDialogCustom);
        builder.setMessage(getString(R.string.no_subscription_dialog_message));
        //.setTitle(getString(R.string.alert_dialog_server_down_title));

        builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                Intent intent = new Intent(ActivityCalendar.this, InfoSelectPriceActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(InfoMainActivity.RENEW, true);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        mDialog = builder.create();
        mDialog.show();
    }
}
