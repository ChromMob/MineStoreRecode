package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class TopDonator {
    public TopDonator() {
        this.userName = "";
        this.price = 0;
    }

    private static final TopDonator DEFAULT = new TopDonator();

    public static TopDonator getDefault() {
        return DEFAULT;
    }

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
