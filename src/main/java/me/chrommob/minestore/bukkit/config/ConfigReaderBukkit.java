package me.chrommob.minestore.bukkit.config;

import me.chrommob.minestore.bukkit.MineStoreBukkit;
import me.chrommob.minestore.common.templates.ConfigReaderCommon;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigReaderBukkit implements ConfigReaderCommon {
    private final MineStoreBukkit plugin;
    private FileConfiguration config;
    public ConfigReaderBukkit(MineStoreBukkit plugin) {
        this.plugin = plugin;
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
}
