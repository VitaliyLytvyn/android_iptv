
package com.app.tv.mediacast.retrofit.data;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataProgrammsByChannelDay {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("channel_zone")
    @Expose
    private String channelZone;
    @SerializedName("programs")
    @Expose
    private List<Program> programs = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChannelZone() {
        return channelZone;
    }

    public void setChannelZone(String channelZone) {
        this.channelZone = channelZone;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }

}
