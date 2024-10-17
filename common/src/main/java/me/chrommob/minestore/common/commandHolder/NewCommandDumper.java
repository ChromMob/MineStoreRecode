package me.chrommob.minestore.common.commandHolder;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandHolder.type.StoredCommand;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewCommandDumper {
    private final MineStoreCommon plugin;
    private final File dumpedFile;
    public NewCommandDumper(MineStoreCommon plugin) {
        this.plugin = plugin;
        dumpedFile = new File(plugin.configFile().getParentFile(), "savedCommands.json");
    }
    
    private final Gson gson = new Gson();
    public Map<String, List<StoredCommand>> load() {
        if (!dumpedFile.exists()) {
            Map<String, List<String>> old = plugin.commandDumper().load();
            if (!old.isEmpty()) {
                Map<String, List<StoredCommand>> commands = new HashMap<>();
                old.forEach((key, value) -> {
                    List<StoredCommand> storedCommands = new ArrayList<>();
                    value.forEach(command -> storedCommands.add(new StoredCommand(command, -1)));
                    commands.put(key.toLowerCase(), storedCommands);
                });
                update(commands);
                plugin.commandDumper().delete();
                return commands;
            }
            return new HashMap<>();
        }
        Map<String, List<StoredCommand>> commands = new HashMap<>();
        try (FileReader reader = new FileReader(dumpedFile)) {
            Type listType = new TypeToken<Map<String, List<StoredCommand>>>() {
            }.getType();
            commands = gson.fromJson(reader, listType);
        } catch (Exception e) {
            plugin.debug(e);
        }
        return commands;
    }

    public void update(Map<String, List<StoredCommand>> commands) {
        String json = gson.toJson(commands);
        try (FileWriter writer = new FileWriter(dumpedFile, false)) {
            writer.write(json);
        } catch (Exception e) {
            plugin.debug(e);
        }
    }
}
