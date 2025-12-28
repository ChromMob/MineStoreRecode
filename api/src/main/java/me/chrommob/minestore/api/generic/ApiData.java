package me.chrommob.minestore.api.generic;

import java.io.File;

public class ApiData {
    private final File dataFolder;
    private final MineStoreVersion pluginVersion;
    private MineStoreVersion mineStoreVersion;

    public ApiData(File dataFolder, MineStoreVersion pluginVersion, MineStoreVersion mineStoreVersion) {
        this.dataFolder = dataFolder;
        this.pluginVersion = pluginVersion;
        this.mineStoreVersion = mineStoreVersion;
    }

    public void setMineStoreVersion(MineStoreVersion mineStoreVersion) {
        this.mineStoreVersion = mineStoreVersion;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public MineStoreVersion getPluginVersion() {
        return pluginVersion;
    }

    public MineStoreVersion getMineStoreVersion() {
        return mineStoreVersion;
    }
}
