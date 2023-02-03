package me.chrommob.minestore.platforms.bukkit.logger;

import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import me.chrommob.minestore.common.interfaces.LoggerCommon;

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
