package me.chrommob.minestore.common.dumper;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.stats.BuildConstats;
import me.chrommob.minestore.common.MineStoreCommon;

import java.util.Map;

@SuppressWarnings("FieldCanBeLocal unused")
public class DumpData {
    private final String version;
    private final String platform;
    private final String plaformName;
    private final String platformVersion;
    private final Map<String, Object> config;
    private final String log;

    public DumpData(String log, MineStoreCommon plugin) {
        this.config = plugin.pluginConfig().getConfig();
        this.version = BuildConstats.VERSION;
        this.platform = Registries.PLATFORM.get();
        this.plaformName = Registries.PLATFORM_NAME.get();
        this.platformVersion = Registries.PLATFORM_VERSION.get();
        this.log = log;
    }
}