package me.chrommob.minestore.common.commandHolder.storage;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandHolder.type.StoredCommand;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewCommandDumper {
    private final MineStoreCommon plugin;
    private final File dumpedFile;
    public NewCommandDumper(MineStoreCommon plugin) {
        this.plugin = plugin;
        dumpedFile = new File(Registries.CONFIG_FILE.get().getParentFile(), "savedCommands.json");
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
        Map<String, List<StoredCommand>> commands = null;
        try (FileReader reader = new FileReader(dumpedFile)) {
            Type listType = new TypeToken<Map<String, List<StoredCommand>>>() {
            }.getType();
            commands = gson.fromJson(reader, listType);
        } catch (Exception e) {
            // Move the unreadable file aside, otherwise the next update overwrites the pending purchases for good.
            File backup = new File(dumpedFile.getParentFile(), dumpedFile.getName() + ".corrupt-" + System.currentTimeMillis());
            if (dumpedFile.renameTo(backup)) {
                plugin.log("Failed to read pending commands from " + dumpedFile.getName() + "! The file was moved to " + backup.getName() + " so they can be recovered.");
            } else {
                plugin.log("Failed to read pending commands from " + dumpedFile.getName() + "! Back the file up before it is overwritten.");
            }
            plugin.debug(this.getClass(), e);
        }
        // An empty file deserializes to null.
        return commands == null ? new HashMap<>() : commands;
    }

    // Keeps the old file as a backup after its commands moved to pendingCommands.json, so they are not imported twice.
    public void markMigrated() {
        if (!dumpedFile.exists()) {
            return;
        }
        File migrated = new File(dumpedFile.getParentFile(), dumpedFile.getName() + ".migrated");
        if (!dumpedFile.renameTo(migrated)) {
            plugin.log("Could not rename " + dumpedFile.getName() + " after importing its commands.");
        }
    }

    public void update(Map<String, List<StoredCommand>> commands) {
        String json = gson.toJson(commands);
        // Write to a temporary file and swap it in, so a crash mid-write cannot corrupt the stored commands.
        File tempFile = new File(dumpedFile.getParentFile(), dumpedFile.getName() + ".tmp");
        try {
            try (FileWriter writer = new FileWriter(tempFile, false)) {
                writer.write(json);
            }
            try {
                Files.move(tempFile.toPath(), dumpedFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), dumpedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            plugin.log("Failed to save pending commands to " + dumpedFile.getName() + ", they are only kept in memory until the next restart!");
            plugin.debug(this.getClass(), e);
        }
    }
}
