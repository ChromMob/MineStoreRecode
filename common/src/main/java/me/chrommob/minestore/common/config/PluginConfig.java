package me.chrommob.minestore.common.config;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigManager;
import me.chrommob.config.ConfigWrapper;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.common.config.lang.cs_CZ;
import me.chrommob.minestore.common.config.lang.en_US;

import java.io.File;
import java.util.*;

public class PluginConfig extends ConfigWrapper {
    private static final Map<String, ConfigWrapper> langMap = new HashMap<>();
    static {
        langMap.put("cs_CZ", new cs_CZ());
        langMap.put("en_US", new en_US());
    }
    private final ConfigManager configManager;
    private final ConfigManager langConfigManager;
    public PluginConfig(ConfigManager configManager, File configFile) {
        super("config", getKeys());
        this.configManager = configManager;
        this.langConfigManager = new ConfigManager(new File(configFile.getParentFile(), "lang"));
        for (ConfigWrapper configWrapper : langMap.values()) {
            this.langConfigManager.addConfig(configWrapper);
        }
    }

    public static List<ConfigKey> getKeys() {
        List<ConfigKey> keys = new ArrayList<>();

        keys.add(new ConfigKey("debug", false, Collections.singletonList("Only enable this if you are asked to by the MineStore developer.")));
        keys.add(new ConfigKey("command-execution-logging", true, Collections.singletonList("If this is enabled every command executed by minestore will be logged to the console.")));
        List<String> langComment = new ArrayList<>();
        langComment.add("Set the language that is used by the plugin.");
        langComment.add("Available languages: " + langMap.keySet() + " but you can create your own language file.");
        langComment.add("To create your own language file, just set language to whatever you want and the plugin will create template file for you in the lang folder.");
        keys.add(new ConfigKey("language", "en_US", langComment));

        List<String> storeUrlComment = new ArrayList<>();
        storeUrlComment.add("Set the store URL that is used by the plugin.");
        storeUrlComment.add("The URL must start with https:// and end with /");
        keys.add(new ConfigKey("store-url", "https://store.example.com", storeUrlComment));

        List<ConfigKey> apiKeys = new ArrayList<>();
        apiKeys.add(new ConfigKey("key-enabled", false, Collections.singletonList("API key is not required only on very old versions of MineStore, so enable this.")));
        apiKeys.add(new ConfigKey("key", "123456789", Collections.singletonList("The API key that is used by the plugin.")));
        keys.add(new ConfigKey("api", apiKeys));

        List<ConfigKey> weblistenerKeys = new ArrayList<>();
        weblistenerKeys.add(new ConfigKey("secret-enabled", false, Collections.singletonList("Secret key is not required only on very old versions of MineStore, so enable this.")));
        weblistenerKeys.add(new ConfigKey("secret-key", "extraSecretKey", Collections.singletonList("The secret key that is used to authenticate the getting of commands.")));
        keys.add(new ConfigKey("weblistener", weblistenerKeys));

        List<ConfigKey> authKeys = new ArrayList<>();
        List<String> authTimeoutComment = new ArrayList<>();
        authTimeoutComment.add("Set the timeout in seconds that the player has to authenticate their Minecraft account with MineStore website.");
        authTimeoutComment.add("This is the amount of time in seconds that the player has to authenticate their Minecraft account with MineStore website.");
        authKeys.add(new ConfigKey("timeout", 300, authTimeoutComment));
        keys.add(new ConfigKey("auth", authKeys));

        List<ConfigKey> storeCommandKeys = new ArrayList<>();
        storeCommandKeys.add(new ConfigKey("enabled", false, Collections.singletonList("If this is enabled the player will be sent a link to the MineStore website when they run the /store command.")));
        keys.add(new ConfigKey("store-command", storeCommandKeys));

        List<ConfigKey> buyGuiKeys = new ArrayList<>();
        buyGuiKeys.add(new ConfigKey("enabled", false, Collections.singletonList("If this is enabled the player will be able to see packages in /buy command.")));
        List<ConfigKey> buyGuiBackgroundKeys = new ArrayList<>();
        buyGuiBackgroundKeys.add(new ConfigKey("enabled", true, Collections.singletonList("If this is enabled the background of the GUI will be enabled.")));
        buyGuiBackgroundKeys.add(new ConfigKey("item", "GLASS_PANE", Collections.singletonList("The item that is used as the background of the GUI.")));
        buyGuiKeys.add(new ConfigKey("background", buyGuiBackgroundKeys));
        List<ConfigKey> buyGuiBackItemKeys = new ArrayList<>();
        buyGuiBackItemKeys.add(new ConfigKey("item", "BARRIER", Collections.singletonList("The item that is used as the back item of the GUI.")));
        buyGuiKeys.add(new ConfigKey("back", buyGuiBackItemKeys));
        keys.add(new ConfigKey("buy-gui", buyGuiKeys));

        List<ConfigKey> mysqlKeys = new ArrayList<>();
        mysqlKeys.add(new ConfigKey("enabled", false, Collections.singletonList("If this is enabled the plugin will use MySQL database to sync data with MineStore website.")));
        mysqlKeys.add(new ConfigKey("ip", "localhost", Collections.singletonList("The IP of the MySQL database.")));
        mysqlKeys.add(new ConfigKey("port", 3306, Collections.singletonList("The port of the MySQL database.")));
        mysqlKeys.add(new ConfigKey("database", "minestore", Collections.singletonList("The name of the MySQL database.")));
        mysqlKeys.add(new ConfigKey("username", "root", Collections.singletonList("The username of the MySQL database.")));
        mysqlKeys.add(new ConfigKey("password", "superSecretPassword", Collections.singletonList("The password of the MySQL database.")));
        keys.add(new ConfigKey("mysql", mysqlKeys));

        return keys;
    }

    public ConfigWrapper getLang() {
        String lang = getKey("language").getAsString();
        if (langConfigManager.getConfigWrapper(lang) != null) {
            return langConfigManager.getConfigWrapper(lang);
        }
        if (langMap.containsKey(lang)) {
            langConfigManager.addConfig(langMap.get(lang));
            return langMap.get(lang);
        }
        ConfigWrapper configWrapper = new en_US(lang);
        langConfigManager.addConfig(configWrapper);
        return configWrapper;
    }

    public void saveConfig() {
        configManager.saveConfig(getName());
        langConfigManager.saveConfig(getLang().getName());
    }

    public void reload() {
        configManager.reloadConfig(getName());
        langConfigManager.reloadConfig(getLang().getName());
    }
}
