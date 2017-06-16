
package com.app.tv.mediacast.retrofit.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataTranslateLanguages {

    @SerializedName("ua")
    @Expose
    private String ua;
    @SerializedName("ru")
    @Expose
    private String ru;

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


    //TODO TEST ADDED THIS FILD - CHECK
    @SerializedName("pl")
    @Expose
    private String pl;

    public String getPl() {
        return pl;
    }
    public void setPl(String pl) {
        this.pl = pl;
    }

}
