package me.chrommob.minestore.common.commandGetters.dataTypes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GsonReponse {
    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("auth_id")
    @Expose
    private String auth_id;

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("command")
    @Expose
    private String command;

    @SerializedName("is_online_required")
    @Expose
    private boolean playerOnlineNeeded;

    public String getType() {
        return type;
    }

    public String authId() {
        return auth_id;
    }

    public boolean isPlayerOnlineNeeded() {
        return playerOnlineNeeded;
    }

    public int requestId() {
        return id;
    }

    public String username() {
        return username;
    }

    public String command() {
        return command;
    }
}
