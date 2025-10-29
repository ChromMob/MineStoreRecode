package me.chrommob.minestore.common.paynow.json;

import me.chrommob.minestore.libs.com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

public class PlayerList {

    @SerializedName("customer_names")
    List<String> customerNames;
    @SerializedName("minecraft_uuids")
    List<UUID> minecraftUuids;

    public PlayerList(List<String> names, List<UUID> uuids) {
        this.customerNames = names;
        this.minecraftUuids = uuids;
    }

}