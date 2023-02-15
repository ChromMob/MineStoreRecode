package me.chrommob.minestore.common.config;

import me.chrommob.minestore.common.MineStoreCommon;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigReader {
    private final File configFile;
    private Yaml yaml;
    private Map<String, Object> configYaml = new LinkedHashMap<>();

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
        populateDefaultConfig();
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveDefaultConfig();
        }
        reload();
    }

    private void populateDefaultConfig() {
        for (ConfigKey key : ConfigKey.values()) {
            Configuration configuration = key.getConfiguration();
            String location = configuration.getLocation();
            if (location.split("\\.").length > 1) {
                String[] split = location.split("\\.");
                String parent = split[0];
                configYaml.putIfAbsent(parent, new LinkedHashMap<String, Object>());
                ((Map<String, Object>) configYaml.get(parent)).put(split[1], configuration.getDefaultValue());
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

    public void reload() {
        FileReader reader = null;
        try {
            reader = new FileReader(configFile);
            configYaml = yaml.load(reader);
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
        }
        for (final ConfigKey key : ConfigKey.values()) {
            final Configuration configuration = key.getConfiguration();
            final String location = configuration.getLocation();
            final String[] split = location.split("\\.");
            if (split.length > 1) {
                final String parent = split[0];
                configYaml.putIfAbsent(parent, new LinkedHashMap());
                final Map<String, Object> map = (Map<String, Object>) this.configYaml.get(parent);
                map.putIfAbsent(split[1], configuration.getDefaultValue());
            }
            else if (!configYaml.containsKey(location)) {
                configYaml.put(location, configuration.getDefaultValue());
            }
        }
        saveDefaultConfig();
    }

    public Object get(ConfigKey key) {
        Configuration configuration = key.getConfiguration();
        String location = configuration.getLocation();
        if (location.split("\\.").length > 1) {
            String[] split = location.split("\\.");
            String parent = split[0];
            return ((Map<String, Object>) configYaml.get(parent)).get(split[1]);
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
            String parent = split[0];
            ((Map<String, Object>) configYaml.get(parent)).put(split[1], value);
        } else {
            configYaml.put(location, value);
        }
        saveDefaultConfig();
    }
}