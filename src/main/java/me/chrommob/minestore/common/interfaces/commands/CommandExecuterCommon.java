package me.chrommob.minestore.common.interfaces.commands;

import me.chrommob.minestore.common.interfaces.user.CommonUser;

public interface CommandExecuterCommon {
    public void execute(String command);

    public boolean isOnline(String username);
}
