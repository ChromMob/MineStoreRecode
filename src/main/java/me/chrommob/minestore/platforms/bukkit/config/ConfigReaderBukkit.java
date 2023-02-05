package me.chrommob.minestore.platforms.bukkit.config;

import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import me.chrommob.minestore.common.interfaces.ConfigReaderCommon;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigReaderBukkit implements ConfigReaderCommon {
    private final MineStoreBukkit plugin;
    private FileConfiguration config;
    public ConfigReaderBukkit(MineStoreBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public File dataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public void init() {
        config = plugin.getConfig();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    @Override
    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    @Override
    public COMMAND_MODE commandMode() {
        return COMMAND_MODE.valueOf(config.getString("command-mode").toUpperCase());
    }

    @Override
    public String storeUrl() {
        return config.getString("store-url");
    }

    @Override
    public boolean secretEnabled() {
        return config.getBoolean("secret-enabled");
    }

    @Override
    public String secretKey() {
        return config.getString("secret-key");
    }

    @Override
    public boolean debug() {
        return config.getBoolean("debug");
    }

    @Override
    public int authTimeout() {
        return config.getInt("auth-timeout");
    }
}
