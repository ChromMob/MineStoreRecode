package me.chrommob.minestore.api.generic;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigWrapper;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class MineStoreAddon {
    private ApiData apiData;

    public void setApiData(ApiData apiData) {
        this.apiData = apiData;
    }
    abstract public void onEnable();
    abstract public String getName();

    public List<ConfigKey<?>> getConfigKeys() {
        return Collections.emptyList();
    }

    public List<Object> getCommands() {
        return Collections.emptyList();
    }

    public ApiData getApiData() {
        return apiData;
    }
}
