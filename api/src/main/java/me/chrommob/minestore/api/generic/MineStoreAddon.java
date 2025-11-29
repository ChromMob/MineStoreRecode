package me.chrommob.minestore.api.generic;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigWrapper;

import java.io.File;
import java.util.Collections;
import java.util.List;

public abstract class MineStoreAddon {
    private File directory;

    public void  setDirectory(File directory) {
        this.directory = directory;
    }

    abstract public void onEnable();
    abstract public String getName();

    public List<ConfigKey<?>> getConfigKeys() {
        return Collections.emptyList();
    }

    public List<Object> getCommands() {
        return Collections.emptyList();
    }

    public File getDataFolder() {
        return directory;
    }
}
