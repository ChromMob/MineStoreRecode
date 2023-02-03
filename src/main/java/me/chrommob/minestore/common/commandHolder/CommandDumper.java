package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandDumper {
    private File dumpedFile = new File(MineStoreCommon.getInstance().configReader().dataFolder(), "temp.yaml");
    private Yaml yaml = new Yaml();

    public CommandDumper() {
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
            MineStoreCommon.getInstance().debug(e);
        }
        return new ConcurrentHashMap<>(yaml.load(inputStream));
    }

    public void update(Map<String, List<String>> commands) {
        FileWriter fileOutputStream = null;
        try {
            fileOutputStream = new FileWriter(dumpedFile);
        } catch (IOException e) {
            MineStoreCommon.getInstance().debug(e);
        }
        yaml.dump(commands, fileOutputStream);
    }
}
