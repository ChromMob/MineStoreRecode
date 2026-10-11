package me.chrommob.minestore.common.commandHolder.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandHolder.type.PendingCommand;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Saves pending commands to pendingCommands.json. Purchases that are not saved here are lost on a crash,
 * because the store never hands out a command again once its delivery was confirmed.
 */
public class PendingCommandStore {
    private final MineStoreCommon plugin;
    private final File file;
    private final Gson gson = new Gson();

    public PendingCommandStore(MineStoreCommon plugin) {
        this.plugin = plugin;
        this.file = new File(Registries.CONFIG_FILE.get().getParentFile(), "pendingCommands.json");
    }

    public boolean exists() {
        return file.exists();
    }

    public List<PendingCommand> load() {
        List<PendingCommand> commands = null;
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<PendingCommand>>() {
            }.getType();
            commands = gson.fromJson(reader, listType);
        } catch (Exception e) {
            // Move the unreadable file aside, otherwise the next save overwrites the pending purchases for good.
            File backup = new File(file.getParentFile(), file.getName() + ".corrupt-" + System.currentTimeMillis());
            if (file.renameTo(backup)) {
                plugin.log("Failed to read pending commands from " + file.getName() + "! The file was moved to " + backup.getName() + " so they can be recovered.");
            } else {
                plugin.log("Failed to read pending commands from " + file.getName() + "! Back the file up before it is overwritten.");
            }
            plugin.debug(this.getClass(), e);
        }
        // An empty file deserializes to null.
        return commands == null ? new ArrayList<>() : commands;
    }

    public void save(Collection<PendingCommand> commands) {
        String json = gson.toJson(commands);
        // Write to a temporary file and swap it in, so a crash mid-write cannot corrupt the pending commands.
        File tempFile = new File(file.getParentFile(), file.getName() + ".tmp");
        try {
            try (FileWriter writer = new FileWriter(tempFile, false)) {
                writer.write(json);
            }
            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            plugin.log("Failed to save pending commands to " + file.getName() + ", they are only kept in memory until the next restart!");
            plugin.debug(this.getClass(), e);
        }
    }
}
