
package com.app.tv.mediacast.retrofit.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataAccess {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("left")
    @Expose
    private Integer left;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

}
