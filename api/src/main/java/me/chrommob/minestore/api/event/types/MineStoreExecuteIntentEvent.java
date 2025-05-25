package me.chrommob.minestore.api.event.types;

import me.chrommob.minestore.api.event.MineStoreEvent;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;

public class MineStoreExecuteIntentEvent extends MineStoreEvent {
    private boolean isCancelled = false;
    private final String username;
    private final String command;
    private final int id;
    private final COMMAND_TYPE commandType;

    public MineStoreExecuteIntentEvent(ParsedResponse.COMMAND_TYPE commandType, String username, String command, int id) {
        this.username = username;
        this.command = command;
        this.id = id;
        this.commandType = commandType == ParsedResponse.COMMAND_TYPE.ONLINE ? COMMAND_TYPE.ONLINE : COMMAND_TYPE.OFFLINE;
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

    public COMMAND_TYPE commandType() {
        return commandType;
    }
}
