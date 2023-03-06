package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class TopDonator {
    @SerializedName("username")
    private String userName;

    @SerializedName("amount")
    private double price;

    public String getUserName() {
        return userName;
    }

    public double getPrice() {
        return price;
    }
}
