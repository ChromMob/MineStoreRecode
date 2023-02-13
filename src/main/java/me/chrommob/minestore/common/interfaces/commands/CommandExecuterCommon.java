package me.chrommob.minestore.common.interfaces.commands;

public interface CommandExecuterCommon {
    public void execute(String command);

    public boolean isOnline(String username);
}
