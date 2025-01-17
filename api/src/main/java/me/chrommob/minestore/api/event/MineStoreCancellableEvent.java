package me.chrommob.minestore.api.event;

public class MineStoreCancellableEvent extends MineStoreEvent {
    private boolean isCancelled = false;

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
