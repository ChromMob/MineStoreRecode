package me.chrommob.minestore.addons.api.event;

public class MineStoreEvent {
    public void call() {
        MineStoreEventBus.fireEvent(this);
    }
}
