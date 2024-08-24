package me.chrommob.minestore.common.subsription.json;

import com.google.gson.annotations.SerializedName;

public class ReturnSubscriptionObject {
    @SerializedName("success")
    private boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("urls")
    private String[] urls;

    public boolean isSuccess() {
        return success;
    }

    public String message() {
        return message;
    }

    public String[] urls() {
        return urls;
    }
}
