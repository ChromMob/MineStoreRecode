package me.chrommob.minestore.platforms.bukkit.logger;

import me.chrommob.minestore.api.interfaces.logger.LoggerCommon;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitLogger implements LoggerCommon {
    private final JavaPlugin plugin;

    public BukkitLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void log(String message) {
        plugin.getLogger().info(message);
    }
}
