
package com.app.tv.mediacast.retrofit.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DataAppTimeZones {

    @SerializedName("-05")
    @Expose
    private String _05;
    @SerializedName("-06")
    @Expose
    private String _06;
    @SerializedName("-07")
    @Expose
    private String _07;
    @SerializedName("-08")
    @Expose
    private String _08;
    @SerializedName("-09")
    @Expose
    private String _09;
    @SerializedName("Live")
    @Expose
    private String live;

    private ArrayList<String> arrayList;

    public ArrayList<String> getZonesList() {
        arrayList = new ArrayList<>();
        arrayList.add(live);
        arrayList.add(_05);
        arrayList.add(_06);
        arrayList.add(_07);
        arrayList.add(_08);
        arrayList.add(_09);

        return arrayList;
    }

    public String getTimeZone(String in) {
        switch (in) {
            case "Europe/Kiev":
                return "Live";
            case "America/Toronto":
                return "-05";
            case "America/Chicago":
                return "-06";
            case "America/Denver":
                return "-07";
            case "America/Vancouver":
                return "-08";
            case "US/Alaska":
                return "-09";
            default:
                return "Live";
        }
    }


    public String get05() {
        return _05;
    }

    public void set05(String _05) {
        this._05 = _05;
    }

    public String get06() {
        return _06;
    }

    public void set06(String _06) {
        this._06 = _06;
    }

    public String get07() {
        return _07;
    }

    public void set07(String _07) {
        this._07 = _07;
    }

    public String get08() {
        return _08;
    }

    public void set08(String _08) {
        this._08 = _08;
    }

    public String get09() {
        return _09;
    }

    public void set09(String _09) {
        this._09 = _09;
    }

    public String getLive() {
        return live;
    }

    public void setLive(String live) {
        this.live = live;
    }

}
