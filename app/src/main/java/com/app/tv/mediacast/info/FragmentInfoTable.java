package com.app.tv.mediacast.info;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.R;
import com.app.tv.mediacast.databinding.InfoTableLayoutBinding;
import com.app.tv.mediacast.retrofit.data.Package;

import java.util.Arrays;


/**
 * Created by skyver on 12/17/16.
 */

public class FragmentInfoTable  extends Fragment implements View.OnClickListener {

    private int mCurrentColumn = -1;
    private View mView;

    private int position = -1;
    private boolean isSelectable;
    private static final String POSITION = "position";
    private static final String SELECT = "select";
    private static final String SAVE_COLUMN = "column";

    private static final Integer[] aSelectColumn = {
            R.id.table2_view0,
            R.id.table2_view1,
            R.id.table2_view2
    };

    private static final int[] aSelectTxtView = {
            R.id.text_view_select_0,
            R.id.text_view_select_1,
            R.id.text_view_select_2
    };

    OnJoinButtonResultListener onJoinButtonResultListener;

    public interface OnJoinButtonResultListener{
        void onJoinButtonResult(int position, int selected);
    }

    public static FragmentInfoTable newInstance(boolean isSelectable, int position) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SELECT, isSelectable);
        bundle.putInt(POSITION, position);

        FragmentInfoTable fragment = new FragmentInfoTable();
        fragment.setArguments(bundle);

        return fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            this.isSelectable = bundle.getBoolean(SELECT);
            position = bundle.getInt(POSITION);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, position);
        outState.putBoolean(SELECT, isSelectable);
        outState.putInt(SAVE_COLUMN, mCurrentColumn);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onJoinButtonResultListener = (OnJoinButtonResultListener)activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement OnJoinButtonResultListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(FragmentInfoTwo.dataPlanList == null){
            Intent intent = new Intent(getActivity(), getActivity().getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getActivity().startActivity(intent);
            getActivity().finish();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        InfoTableLayoutBinding tableLayoutBinding = DataBindingUtil.inflate(inflater,
                R.layout.info_table_layout, container, false);

        if(savedInstanceState == null){
            readBundle(getArguments());
        } else {
            isSelectable = savedInstanceState.getBoolean(SELECT);
            position = savedInstanceState.getInt(POSITION, 0);

            mCurrentColumn = savedInstanceState.getInt(SAVE_COLUMN, -1);
        }

        tableLayoutBinding.setModel(FragmentInfoTwo.dataPlanList.get(position));
        tableLayoutBinding.setPackage0(FragmentInfoTwo.dataPlanList.get(position).getPackages().get(0));
        tableLayoutBinding.setPackage1(FragmentInfoTwo.dataPlanList.get(position).getPackages().get(1));
        tableLayoutBinding.setPackage2(FragmentInfoTwo.dataPlanList.get(position).getPackages().get(2));

        View v = tableLayoutBinding.getRoot();
        mView = v;

        if(position != -1){

            View btn = v.findViewById(R.id.button_join_bottom);
            btn.setOnClickListener(this);
            View btn2 = v.findViewById(R.id.button_join_bottom_2);
            btn2.setOnClickListener(this);

            if(isSelectable) {
                initTableClick();

            } else {
                View viewToDelete = v.findViewById(R.id.faq_LinearLayout);
                if(viewToDelete != null){
                    ((ViewManager)viewToDelete.getParent()).removeView(viewToDelete);
                }

                viewToDelete = v.findViewById(R.id.tableRow_select);
                if(viewToDelete != null){
                    ((ViewManager)viewToDelete.getParent()).removeView(viewToDelete);
                }
            }
        }

        return v;
    }

    private void processClick(){
        onJoinButtonResultListener.onJoinButtonResult(position, mCurrentColumn);
    }

    private  void initTableClick(){

        //these all are to resize and adjust columns

        TableLayout tbL1 = (TableLayout)mView.findViewById(R.id.table_layout1);
        LinearLayout tbL2 = (LinearLayout)mView.findViewById(R.id.table_layout2);

        TextView vv = (TextView) mView.findViewById(aSelectColumn[0]);
        TextView vv2 = (TextView) mView.findViewById(aSelectColumn[1]);
        TextView vv3 = (TextView) mView.findViewById(aSelectColumn[2]);

        ViewGroup.LayoutParams params1 = tbL1.getLayoutParams();
        ViewGroup.LayoutParams params2 = tbL2.getLayoutParams();

        params2.height = params1.height;
        tbL2.requestLayout();
        params2.width = params1.width;
        tbL2.requestLayout();

        tbL2.setLayoutParams(params2);
        tbL2.requestLayout();

        View v1_1 = mView.findViewById(R.id.row1_element1);
        View v1_2 = mView.findViewById(R.id.row1_element2);
        View v1_3 = mView.findViewById(R.id.row1_element3);

        vv.getLayoutParams().height = tbL2.getLayoutParams().height;
        vv.requestLayout();
        vv.getLayoutParams().width = (v1_1.getLayoutParams().width ) ;
        vv.requestLayout();

        vv2.getLayoutParams().height = tbL2.getLayoutParams().height;
        vv2.requestLayout();
        vv2.getLayoutParams().width = v1_2.getLayoutParams().width ;
        vv2.requestLayout();

        vv3.getLayoutParams().height = tbL2.getLayoutParams().height;
        vv3.requestLayout();
        vv3.getLayoutParams().width = v1_3.getLayoutParams().width  ;
        vv3.requestLayout();

        //set listeners to set chosen column
        vv2.setOnClickListener(this);
        vv.setOnClickListener(this);
        vv3.setOnClickListener(this);

        //find and set Default column as selected
        if(mCurrentColumn == -1){
            for(Package pkg : FragmentInfoTwo.dataPlanList.get(position).getPackages()){
                if(pkg.getIsDefault()){
                    int ind = FragmentInfoTwo.dataPlanList.get(position).getPackages().indexOf(pkg);

                    onClick(mView.findViewById(aSelectColumn[ind]));
                    return;

                }
            }
        } else {
            onClick(mView.findViewById(aSelectColumn[mCurrentColumn]));
            return;
        }
        ///this should not happen - if there is no default property then select 0 column
        onClick(mView.findViewById(aSelectColumn[0]));

    }

    @Override
    public void onClick(final View view) {
        int id = view.getId();
        if(id == R.id.button_join_bottom || id == R.id.button_join_bottom_2){
            processClick();
            return;
        }

        int index = Arrays.asList(aSelectColumn).indexOf(id);

            View tmp;
            if(mCurrentColumn == -1){
                view.setVisibility(View.VISIBLE);//check if needed

                view.setBackgroundColor(getResources().getColor(R.color.semi_transparent));
                mCurrentColumn = index;

                tmp =  mView.findViewById(aSelectTxtView[index]);
                //set text bold
                ((TextView)tmp).setText(Global.bold(getResources().getString(R.string.selected)));

            } else {
                mView.findViewById(aSelectColumn[mCurrentColumn]).setBackgroundColor(Color.TRANSPARENT);
                tmp =  mView.findViewById(aSelectTxtView[mCurrentColumn]);
                ((TextView)tmp).setText(getResources().getString(R.string.select));

                view.setVisibility(View.VISIBLE);

                view.setBackgroundColor(getResources().getColor(R.color.semi_transparent));
                //set text bold
                tmp =  mView.findViewById(aSelectTxtView[index]);
                ((TextView)tmp).setText(Global.bold(getResources().getString(R.string.selected)));

                mCurrentColumn = index;
            }

    }
}

