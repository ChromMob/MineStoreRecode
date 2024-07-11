package me.chrommob.minestore.addons.events;

public class MineStoreEvent {
    public void call() {
        MineStoreEventBus.fireEvent(this);
    }
}
