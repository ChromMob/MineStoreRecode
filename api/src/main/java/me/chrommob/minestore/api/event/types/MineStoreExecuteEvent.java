package me.chrommob.minestore.api.event.types;

import me.chrommob.minestore.api.event.MineStoreEvent;

public class MineStoreExecuteEvent extends MineStoreEvent {
    private boolean isCancelled = false;
    private final String username;
    private final String command;
    private final int id;

    public MineStoreExecuteEvent(String username, String command, int id) {
        this.username = username;
        this.command = command;
        this.id = id;
    }

    public String username() {
        return username;
    }

    public String command() {
        return command;
    }

    public int id() {
        return id;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}
