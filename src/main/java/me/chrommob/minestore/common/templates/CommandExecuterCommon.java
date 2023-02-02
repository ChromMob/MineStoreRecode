package me.chrommob.minestore.common.templates;

public interface CommandExecuterCommon {
    public void execute(String command);

    public boolean isOnline(String username);
}
