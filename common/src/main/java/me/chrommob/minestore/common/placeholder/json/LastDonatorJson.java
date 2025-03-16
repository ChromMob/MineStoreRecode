package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LastDonatorJson {
    @SerializedName("data")
    private List<LastDonator> lastDonators;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("last_page")
    private int lastPage;

    public List<LastDonator> getLastDonators() {
        return lastDonators;
    }

    public int getLastPage() {
        return lastPage;
    }

    public int getPerPage() {
        return perPage;
    }
}
