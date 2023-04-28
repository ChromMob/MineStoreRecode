package me.chrommob.minestore.platforms.bukkit.logger;

import me.chrommob.minestore.common.interfaces.logger.LoggerCommon;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;

public class BukkitLogger implements LoggerCommon {
    private final MineStoreBukkit plugin;

    public BukkitLogger(MineStoreBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void log(String message) {
        plugin.getLogger().info(message);
    }
}
