package me.chrommob.minestore.common.command.types;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.CommonUser;

public class CommonConsoleUser implements CommonUser {
    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public void sendMessage(String message) {
        MineStoreCommon.getInstance().log(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean isOnline() {
        return true;
    }
}
