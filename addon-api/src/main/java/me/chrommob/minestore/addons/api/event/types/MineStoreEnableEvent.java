package me.chrommob.minestore.addons.api.event.types;

import me.chrommob.minestore.addons.api.event.MineStoreEvent;

public class MineStoreEnableEvent extends MineStoreEvent {
    private final String storeUrl;
    private final String apiKey;
    public MineStoreEnableEvent(String storeUrl, String apiKey) {
        if (!storeUrl.endsWith("/")) {
            storeUrl += "/";
        }
        if (!storeUrl.startsWith("https://")) {
            storeUrl = "https://" + storeUrl;
        }
        this.storeUrl = storeUrl;
        this.apiKey = apiKey;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}
