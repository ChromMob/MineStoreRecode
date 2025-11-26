package me.chrommob.minestore.api.event.types;

public class MineStorePlayerQuitEvent extends MineStorePlayerJoinEvent {
    public MineStorePlayerQuitEvent(String username) {
        super(username);
    }
}
