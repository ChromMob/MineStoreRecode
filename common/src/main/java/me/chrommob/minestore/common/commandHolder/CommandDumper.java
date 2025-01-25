package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.common.MineStoreCommon;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandDumper {
    private final MineStoreCommon plugin;
    private final File dumpedFile;
    private final Yaml yaml = new Yaml();

    public CommandDumper(MineStoreCommon plugin) {
        this.plugin = plugin;
        dumpedFile = new File(Registries.CONFIG_FILE.get().getParentFile(), "dumpedCommands.yml");
        if (!dumpedFile.getParentFile().exists()) {
            dumpedFile.getParentFile().mkdirs();
        }
    }

    public Map<String, List<String>> load() {
        if (!dumpedFile.exists()) {
            return new ConcurrentHashMap<>();
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(dumpedFile);
        } catch (FileNotFoundException e) {
            plugin.debug(this.getClass(), e);
            return new ConcurrentHashMap<>();
        }
        return new ConcurrentHashMap<>(yaml.load(inputStream));
    }

    public void update(Map<String, List<String>> commands) {
        FileWriter fileOutputStream = null;
        try {
            fileOutputStream = new FileWriter(dumpedFile);
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
        }
        yaml.dump(commands, fileOutputStream);
    }

    public void delete() {
        dumpedFile.delete();
    }
}
