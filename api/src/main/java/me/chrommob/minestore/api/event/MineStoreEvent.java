package me.chrommob.minestore.api.event;

public class MineStoreEvent {
    public void call() {
        MineStoreEventBus.fireEvent(this);
    }

    public enum COMMAND_TYPE {
        ONLINE,
        OFFLINE
    }
}
