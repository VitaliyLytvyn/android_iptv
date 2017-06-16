
package com.app.tv.mediacast.retrofit.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Package {

    @SerializedName("package_id")
    @Expose
    public String packageId;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("isDefault")
    @Expose
    public Boolean isDefault;
    @SerializedName("price")
    @Expose
    public Double price;
    @SerializedName("ios_id")
    @Expose
    public String iosId;
    @SerializedName("savePrice")
    @Expose
    public Integer savePrice;
    @SerializedName("channels")
    @Expose
    public Integer channels;
    @SerializedName("data")
    @Expose
    public Data data;

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getIosId() {
        return iosId;
    }

    public void setIosId(String iosId) {
        this.iosId = iosId;
    }

    public Integer getSavePrice() {
        return savePrice;
    }

    public void setSavePrice(Integer savePrice) {
        this.savePrice = savePrice;
    }

    public Integer getChannels() {
        return channels;
    }

    public void setChannels(Integer channels) {
        this.channels = channels;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}
