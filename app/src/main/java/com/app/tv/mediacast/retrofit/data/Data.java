
package com.app.tv.mediacast.retrofit.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("advertasing_free")
    @Expose
    public Boolean advertasingFree;
    @SerializedName("screens")
    @Expose
    public Integer screens;
    @SerializedName("onLaptop")
    @Expose
    public Boolean onLaptop;
    @SerializedName("archive")
    @Expose
    public Boolean archive;
    @SerializedName("cancelAnyTime")
    @Expose
    public Boolean cancelAnyTime;
    @SerializedName("haveTrial")
    @Expose
    public Boolean haveTrial;

    public Boolean getAdvertasingFree() {
        return advertasingFree;
    }

    public void setAdvertasingFree(Boolean advertasingFree) {
        this.advertasingFree = advertasingFree;
    }

    public Integer getScreens() {
        return screens;
    }

    public void setScreens(Integer screens) {
        this.screens = screens;
    }

    public Boolean getOnLaptop() {
        return onLaptop;
    }

    public void setOnLaptop(Boolean onLaptop) {
        this.onLaptop = onLaptop;
    }

    public Boolean getArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    public Boolean getCancelAnyTime() {
        return cancelAnyTime;
    }

    public void setCancelAnyTime(Boolean cancelAnyTime) {
        this.cancelAnyTime = cancelAnyTime;
    }

    public Boolean getHaveTrial() {
        return haveTrial;
    }

    public void setHaveTrial(Boolean haveTrial) {
        this.haveTrial = haveTrial;
    }

}
