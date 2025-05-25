package me.chrommob.minestore.platforms.bungee.logger;

import me.chrommob.minestore.api.interfaces.logger.LoggerCommon;
import net.md_5.bungee.api.plugin.Plugin;

public class LoggerBungee implements LoggerCommon {
    private final Plugin mineStoreBungee;

    public LoggerBungee(Plugin mineStoreBungee) {
        this.mineStoreBungee = mineStoreBungee;
    }

    @Override
    public void log(String message) {
        mineStoreBungee.getLogger().info(message);
    }
}
