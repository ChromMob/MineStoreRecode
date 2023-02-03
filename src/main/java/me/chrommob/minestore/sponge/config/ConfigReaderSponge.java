package me.chrommob.minestore.sponge.config;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.templates.ConfigReaderCommon;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigReaderSponge implements ConfigReaderCommon {
    private final File configFile;
    private Map<String, Object> defaultConfig = new LinkedHashMap<>();
    private Yaml yaml;

    private void populateDefault() {
        defaultConfig.put("command-mode", "WEBLISTENER");
        defaultConfig.put("store-url", "https://store.chrommob.me");
        defaultConfig.put("secret-enabled", true);
        defaultConfig.put("secret-key", 123456789);
        defaultConfig.put("debug", false);
    }
    private Map<String, Object> config = new LinkedHashMap<>();

    public ConfigReaderSponge(Path defaultConfig) {
        this.configFile = new File(defaultConfig.toFile(), "config.yml");
    }

    @Override
    public File dataFolder() {
        return configFile.getParentFile();
    }

    @Override
    public void init() {
        yaml = new Yaml();
        if (!configFile.exists()) {
            createConfig();
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        config = yaml.load(inputStream);
    }

    private void createConfig() {
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        populateDefault();
        FileWriter writer = null;
        try {
            writer = new FileWriter(configFile);
        } catch (IOException e) {
            MineStoreCommon.getInstance().debug(e);
        }
        yaml.dump(defaultConfig, writer);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public COMMAND_MODE commandMode() {
        return COMMAND_MODE.valueOf(((String) config.get("command-mode")).toUpperCase());
    }

    @Override
    public String storeUrl() {
        return (String) config.get("store-url");
    }

    @Override
    public boolean secretEnabled() {
        return (boolean) config.get("secret-enabled");
    }

    @Override
    public String secretKey() {
        try {
            return (String) config.get("secret-key");
        } catch (ClassCastException e) {
            return String.valueOf(config.get("secret-key"));
        }
    }

    @Override
    public boolean debug() {
        return (boolean) config.get("debug");
    }
}
