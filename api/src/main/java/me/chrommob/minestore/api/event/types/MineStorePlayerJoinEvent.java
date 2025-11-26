package me.chrommob.minestore.api.event.types;

import me.chrommob.minestore.api.event.MineStoreEvent;

public class MineStorePlayerJoinEvent extends MineStoreEvent {
    private final String username;
    public MineStorePlayerJoinEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
