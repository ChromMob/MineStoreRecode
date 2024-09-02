package me.chrommob.minestore.addons.api.event.types;

import me.chrommob.minestore.addons.api.event.MineStoreCancellableEvent;

public class MineStorePurchaseEvent extends MineStoreCancellableEvent {
    public enum COMMAND_TYPE {
        ONLINE,
        OFFLINE
    }

    private final String username;
    private String command;
    private final int id;
    private COMMAND_TYPE commandType;
    public MineStorePurchaseEvent(String username, String command, int id, COMMAND_TYPE commandType) {
        this.username = username;
        this.command = command;
        this.id = id;
        this.commandType = commandType;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setCommandType(COMMAND_TYPE commandType) {
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
