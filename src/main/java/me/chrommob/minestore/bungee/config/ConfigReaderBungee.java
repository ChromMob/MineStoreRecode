package me.chrommob.minestore.bungee.config;

import me.chrommob.minestore.bungee.MineStoreBungee;
import me.chrommob.minestore.common.templates.ConfigReaderCommon;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static jdk.jfr.internal.SecuritySupport.getResourceAsStream;

public class ConfigReaderBungee implements ConfigReaderCommon {
    private final MineStoreBungee mineStoreBungee;
    public ConfigReaderBungee(MineStoreBungee mineStoreBungee) {
        this.mineStoreBungee = mineStoreBungee;
    }

    private File configFile;
    private Configuration config;

    @Override
    public File dataFolder() {
        return mineStoreBungee.getDataFolder();
    }


    @Override
    public void init() {
        if (!dataFolder().exists()) {
            dataFolder().mkdirs();
        }
        configFile = new File(dataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ignored) {}
    }

    @Override
    public void reload() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ignored) {}
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
}
