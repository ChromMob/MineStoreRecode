package me.chrommob.minestore.addons.events.types;

import me.chrommob.minestore.addons.events.MineStoreEvent;

public class MineStorePurchaseEvent extends MineStoreEvent {
    public enum COMMAND_TYPE {
        ONLINE,
        OFFLINE
    }

    private final String username;
    private final String command;
    private final int id;
    private final COMMAND_TYPE commandType;
    public MineStorePurchaseEvent(String username, String command, int id, COMMAND_TYPE commandType) {
        this.username = username;
        this.command = command;
        this.id = id;
        this.commandType = commandType;
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

    public COMMAND_TYPE commandType() {
        return commandType;
    }
}
