package me.chrommob.minestore.api.interfaces.commands;

import me.chrommob.minestore.api.event.types.MineStoreExecuteEvent;

public abstract class CommandExecuterCommon {
    public void execute(MineStoreExecuteEvent event) {
        if (event.isCancelled()) {
            return;
        }
        execute(event.command());
    }

    public abstract void execute(String command);

    public abstract boolean isOnline(String username);
}
