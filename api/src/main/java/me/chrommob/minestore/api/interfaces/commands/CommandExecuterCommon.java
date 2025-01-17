package me.chrommob.minestore.api.interfaces.commands;

public interface CommandExecuterCommon {
    public void execute(String command);

    public boolean isOnline(String username);
}
