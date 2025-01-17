package me.chrommob.minestore.common.commandGetters.dataTypes;

import com.google.gson.annotations.SerializedName;

public class PostResponse {
    public boolean status;

    @SerializedName("processed")
    public int[] processedIds;

    @SerializedName("results")
    public Result[] results;

    public String error;

    public static class Result {
        public int id;
        public boolean status;
        public String error;
    }
}
