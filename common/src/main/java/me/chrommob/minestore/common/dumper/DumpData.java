package me.chrommob.minestore.common.dumper;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.common.MineStoreCommon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal unused")
public class DumpData {
    private final String version;
    private final String platform;
    private final String plaformName;
    private final String platformVersion;
    private final Map<String, Object> config;
    private String log = "Log file not found or too large to dump.";

    public DumpData(boolean includeLog, MineStoreCommon plugin) {
        this.config = plugin.configReader().getLoadedConfig();
        this.version = plugin.jarFile().getAbsolutePath();
        this.platform = Registries.PLATFORM.get();
        this.plaformName = Registries.PLATFORM_NAME.get();
        this.platformVersion = Registries.PLATFORM_VERSION.get();
        Path logPath = Paths.get(
                plugin.jarFile().getParentFile().getParentFile().getAbsolutePath(), "logs",
                "latest.log");
        if (!includeLog) {
            return;
        }
        File logFile = logPath.toFile();
        if (logFile.exists()) {
            try {
                StringBuilder fileData = new StringBuilder();
                BufferedReader reader = new BufferedReader(
                        new FileReader(logFile));
                char[] buf = new char[1024];
                int numRead = 0;
                while ((numRead = reader.read(buf)) != -1) {
                    String readData = String.valueOf(buf, 0, numRead);
                    fileData.append(readData);
                }
                reader.close();
                log = fileData.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}