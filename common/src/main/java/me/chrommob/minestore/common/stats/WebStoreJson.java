package me.chrommob.minestore.common.stats;

import java.util.UUID;

public class WebStoreJson {
    private final UUID uuid;
    private final int playerCount;
    public WebStoreJson(UUID uuid, int playerCount) {
        this.uuid = uuid;
        this.playerCount = playerCount;
    }
}
