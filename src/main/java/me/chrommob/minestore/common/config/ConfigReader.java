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
//        Set<String> keys = new HashSet<>();
//        for (ConfigKey key : ConfigKey.values()) {
//            Configuration configuration = key.getConfiguration();
//            keys.add(configuration.getLocation());
//            if (!configYaml.containsKey(configuration.getLocation())) {
//                configYaml.put(configuration.getLocation(), configuration.getDefaultValue());
//            }
//        }
//        for (String key : new HashSet<>(configYaml.keySet())) {
//            if (!keys.contains(key)) {
//                configYaml.remove(key);
//            }
//        }
        saveDefaultConfig();
    }

    public Object get(ConfigKey key) {
        Configuration configuration = key.getConfiguration();
        String location = configuration.getLocation();
        if (location.split("\\.").length > 1) {
            String[] split = location.split("\\.");
            String parent = split[0];
            return ((Map<String, Object>) configYaml.get(parent)).get(split[1]);
        } else {
            return configYaml.get(location);
        }
    }
}