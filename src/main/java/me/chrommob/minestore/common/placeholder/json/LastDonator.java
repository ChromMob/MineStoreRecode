package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class LastDonator {
    @SerializedName("user")
    private String userName;

    @SerializedName("amount")
    private double price;

    @SerializedName("package")
    private String packageName;

    public String getUserName() {
        return userName;
    }

    public double getPrice() {
        return price;
    }

    public String getPackageName() {
        return packageName;
    }
}
