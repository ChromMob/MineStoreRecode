package me.chrommob.minestore.platforms.bungee.logger;

import me.chrommob.minestore.api.interfaces.logger.LoggerCommon;
import me.chrommob.minestore.platforms.bungee.MineStoreBungee;

public class LoggerBungee implements LoggerCommon {
    private final MineStoreBungee mineStoreBungee;

    public LoggerBungee(MineStoreBungee mineStoreBungee) {
        this.mineStoreBungee = mineStoreBungee;
    }

    @Override
    public void log(String message) {
        mineStoreBungee.getLogger().info(message);
    }
}
