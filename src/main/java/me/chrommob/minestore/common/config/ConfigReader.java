package me.chrommob.minestore.common.config;

import me.chrommob.minestore.common.MineStoreCommon;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigReader {
    private final File configFile;
    private File languageFile;
    private Yaml yaml;
    private Map<String, Object> configYaml = new LinkedHashMap<>();
    private Map<String, Object> languageYaml = new LinkedHashMap<>();

    public ConfigReader(File configFile) {
        this.configFile = configFile;
        init();
    }

    public void init() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        yaml = new Yaml(options);
        if (!configFile.exists()) {
            populateDefaultConfig();
            configFile.getParentFile().mkdirs();
            saveDefaultConfig();
        }
        languageFile = new File(configFile.getParentFile() + File.separator + "lang", "en_US.lang");
        if (!languageFile.exists()) {
            populateDefaultLanguage();
            languageFile.getParentFile().mkdirs();
            saveDefaultLanguage();
        }
        if (!new File(languageFile.getParentFile(), "cs_CZ.lang").exists()) {
            InputStream cs_CZ = MineStoreCommon.getInstance().getClass().getClassLoader().getResourceAsStream("cs_CZ.lang");
            try {
                Files.copy(cs_CZ, new File(languageFile.getParentFile(), "cs_CZ.lang").toPath());
            } catch (IOException e) {
                MineStoreCommon.getInstance().debug(e);
            }
        }
        if (!new File(languageFile.getParentFile(), "ru_RU.lang").exists()) {
            InputStream ru_RU = MineStoreCommon.getInstance().getClass().getClassLoader().getResourceAsStream("ru_RU.lang");
            try {
                Files.copy(ru_RU, new File(languageFile.getParentFile(), "ru_RU.lang").toPath());
            } catch (IOException e) {
                MineStoreCommon.getInstance().debug(e);
            }
        }
        if (!new File(languageFile.getParentFile(), "ua_UA.lang").exists()) {
            InputStream ua_UA = MineStoreCommon.getInstance().getClass().getClassLoader().getResourceAsStream("ua_UA.lang");
            try {
                Files.copy(ua_UA, new File(languageFile.getParentFile(), "ua_UA.lang").toPath());
            } catch (IOException e) {
                MineStoreCommon.getInstance().debug(e);
            }
        }
        reload();
    }

    private void populateDefaultLanguage() {
        for (ConfigKey key : ConfigKey.values()) {
            if (key.name().toLowerCase().contains("message")) {
                Configuration configuration = key.getConfiguration();
                String location = configuration.getLocation();
                if (location.split("\\.").length > 1) {
                    String[] split = location.split("\\.");
                    Map<String, Object> currentLocation = null;
                    for (int i = 0; i < split.length; i++) {
                        boolean isLast = i >= split.length - 1;
                        if (i == 0) {
                            languageYaml.putIfAbsent(split[i], new LinkedHashMap());
                            currentLocation = (Map<String, Object>) languageYaml.get(split[i]);
                        } else if (!isLast) {
                            currentLocation.putIfAbsent(split[i], new LinkedHashMap());
                            currentLocation = (Map<String, Object>) currentLocation.get(split[i]);
                        } else {
                            currentLocation.putIfAbsent(split[i], configuration.getDefaultValue());
                        }
                    }
                } else {
                    languageYaml.put(location, configuration.getDefaultValue());
                }
            }
        }
    }

    private void populateDefaultConfig() {
        for (ConfigKey key : ConfigKey.values()) {
            if (key.name().toLowerCase().contains("message")) {
                continue;
            }
            Configuration configuration = key.getConfiguration();
            String location = configuration.getLocation();
            if (location.split("\\.").length > 1) {
                String[] split = location.split("\\.");
                Map<String, Object> currentLocation = null;
                for (int i = 0; i < split.length; i++) {
                    boolean isLast = i >= split.length - 1;
                    if (i == 0) {
                        configYaml.putIfAbsent(split[i], new LinkedHashMap());
                        currentLocation = (Map<String, Object>) configYaml.get(split[i]);
                    } else if (!isLast) {
                        currentLocation.putIfAbsent(split[i], new LinkedHashMap());
                        currentLocation = (Map<String, Object>) currentLocation.get(split[i]);
                    } else {
                        currentLocation.putIfAbsent(split[i], configuration.getDefaultValue());
                    }
                }
            } else {
                configYaml.put(location, configuration.getDefaultValue());
            }
        }
    }

    private void saveDefaultConfig() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(configFile);
            yaml.dump(configYaml, writer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveDefaultLanguage() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(languageFile);
            yaml.dump(languageYaml, writer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        FileReader reader = null;
        try {
            reader = new FileReader(configFile);
            configYaml = yaml.load(reader);
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
        }
        for (final ConfigKey key : ConfigKey.values()) {
            final Configuration configuration = key.getConfiguration();
            final String location = configuration.getLocation();
            final String[] split = location.split("\\.");
            if (split.length > 1) {
                Map<String, Object> currentLocation = null;
                for (int i = 0; i < split.length; i++) {
                    boolean isLast = i == split.length - 1;
                    if (i == 0) {
                        if (!configYaml.containsKey(split[i])) {
                            configYaml.put(split[i], new LinkedHashMap());
                        }
                        currentLocation = (Map<String, Object>) configYaml.get(split[i]);
                    } else if (!isLast) {
                        if (!currentLocation.containsKey(split[i])) {
                            currentLocation.put(split[i], new LinkedHashMap());
                        }
                        currentLocation = (Map<String, Object>) currentLocation.get(split[i]);
                    } else {
                        if (!currentLocation.containsKey(split[i])) {
                            currentLocation.put(split[i], configuration.getDefaultValue());
                        }
                    }
                }
            }
            else if (!configYaml.containsKey(location)) {
                configYaml.put(location, configuration.getDefaultValue());
            }
        }
        try {
            languageFile = new File(configFile.getParentFile() + File.separator + "lang", get(ConfigKey.LANGUAGE) + ".lang");
            if (!languageFile.exists()) {
                languageFile = new File(configFile.getParentFile() + File.separator + "lang", "en_US.lang");
            }
            reader = new FileReader(languageFile);
            languageYaml = yaml.load(reader);
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
        }
        for (final ConfigKey key : ConfigKey.values()) {
            if (!key.name().toLowerCase().contains("message")) {
                continue;
            }
            final Configuration configuration = key.getConfiguration();
            final String location = configuration.getLocation();
            final String[] split = location.split("\\.");
            if (split.length > 1) {
                Map<String, Object> currentLocation = null;
                for (int i = 0; i < split.length; i++) {
                    boolean isLast = i == split.length - 1;
                    if (i == 0) {
                        if (!languageYaml.containsKey(split[i])) {
                            languageYaml.put(split[i], new LinkedHashMap());
                        }
                        currentLocation = (Map<String, Object>) languageYaml.get(split[i]);
                    } else if (!isLast) {
                        if (!currentLocation.containsKey(split[i])) {
                            currentLocation.put(split[i], new LinkedHashMap());
                        }
                        currentLocation = (Map<String, Object>) currentLocation.get(split[i]);
                    } else {
                        if (!currentLocation.containsKey(split[i])) {
                            currentLocation.put(split[i], configuration.getDefaultValue());
                        }
                    }
                }
            }
            else if (!languageYaml.containsKey(location)) {
                languageYaml.put(location, configuration.getDefaultValue());
            }
        }
        saveDefaultLanguage();
        saveDefaultConfig();
    }

    public Object get(ConfigKey key) {
        if (key.name().toLowerCase().contains("message")) {
            Configuration configuration = key.getConfiguration();
            String location = configuration.getLocation();
            if (location.split("\\.").length > 1) {
                String[] split = location.split("\\.");
                Map<String, Object> currentLocation = null;
                for (int i = 0; i < split.length; i++) {
                    boolean isLast = i == split.length - 1;
                    if (i == 0) {
                        currentLocation = (Map<String, Object>) languageYaml.get(split[i]);
                    } else if (!isLast) {
                        currentLocation = (Map<String, Object>) currentLocation.get(split[i]);
                    } else {
                        return currentLocation.get(split[i]);
                    }
                }
            }
            return languageYaml.get(location);
        }
        Configuration configuration = key.getConfiguration();
        String location = configuration.getLocation();
        if (location.split("\\.").length > 1) {
            String[] split = location.split("\\.");
            Map<String, Object> currentLocation = null;
            for (int i = 0; i < split.length; i++) {
                boolean isLast = i == split.length - 1;
                if (i == 0) {
                    currentLocation = (Map<String, Object>) configYaml.get(split[i]);
                } else if (!isLast) {
                    currentLocation = (Map<String, Object>) currentLocation.get(split[i]);
                } else {
                    return currentLocation.get(split[i]);
                }
            }
        }
        return configYaml.get(location);
    }

    public void set(ConfigKey key, Object value) {
        Configuration configuration = key.getConfiguration();
        String location = configuration.getLocation();
        Class<?> type = configuration.getDefaultValue().getClass();
        if (value.getClass() != type) {
            MineStoreCommon.getInstance().debug("Invalid type for config value: " + location + " (expected " + type.getSimpleName() + ", got " + value.getClass().getSimpleName() + ")");
        }
        if (location.split("\\.").length > 1) {
            String[] split = location.split("\\.");
            Map<String, Object> currentLocation = null;
            for (int i = 0; i < split.length; i++) {
                boolean isLast = i >= split.length - 1;
                if (i == 0) {
                    configYaml.putIfAbsent(split[i], new LinkedHashMap());
                    currentLocation = (Map<String, Object>) configYaml.get(split[i]);
                } else if (!isLast) {
                    currentLocation.putIfAbsent(split[i], new LinkedHashMap());
                    currentLocation = (Map<String, Object>) currentLocation.get(split[i]);
                } else {
                    currentLocation.put(split[i], value);
                }
            }
        } else {
            configYaml.put(location, configuration.getDefaultValue());
        }
        saveDefaultConfig();
        MineStoreCommon.getInstance().debug("Set config value: " + location + " to " + value);
        MineStoreCommon.getInstance().reload();
    }
}