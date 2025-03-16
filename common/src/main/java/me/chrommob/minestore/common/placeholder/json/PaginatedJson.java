package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedJson<T> {
    @SerializedName("data")
    private List<T> getList;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("last_page")
    private int lastPage;

    public List<T> getList() {
        return getList;
    }

    public int getLastPage() {
        return lastPage;
    }

    public int getPerPage() {
        return perPage;
    }
}
