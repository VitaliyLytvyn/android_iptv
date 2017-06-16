
package com.app.tv.mediacast.retrofit.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataAppLanguages {

    @SerializedName("ua")
    @Expose
    private String ua;
    @SerializedName("ru")
    @Expose
    private String ru;
    @SerializedName("en")
    @Expose
    private String en;

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public String getRu() {
        return ru;
    }

    public void setRu(String ru) {
        this.ru = ru;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

}
