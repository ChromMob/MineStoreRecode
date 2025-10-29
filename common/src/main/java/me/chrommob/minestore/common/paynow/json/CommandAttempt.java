package me.chrommob.minestore.common.paynow.json;

import me.chrommob.minestore.libs.com.google.gson.annotations.SerializedName;

public class CommandAttempt {

    @SerializedName("attempt_id")
    String attemptId;

    public CommandAttempt(String attemptId) {
        this.attemptId = attemptId;
    }

}