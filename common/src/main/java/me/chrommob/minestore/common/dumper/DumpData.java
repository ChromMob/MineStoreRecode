package me.chrommob.minestore.common.dumper;

import com.sun.jna.platform.FileUtils;
import me.chrommob.minestore.common.MineStoreCommon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("FieldCanBeLocal unused")
public class DumpData {
    private final String version = MineStoreCommon.getInstance().jarFile().getAbsolutePath();
    private final String platform = MineStoreCommon.getInstance().getPlatform();
    private final String plaformName = MineStoreCommon.getInstance().getPlatformName();
    private final String platformVersion = MineStoreCommon.getInstance().getPlatformVersion();
    private final Map<String, Object> config;
    private String log = "Log file not found";
    public DumpData() {
        this.config = MineStoreCommon.getInstance().configReader().getLoadedConfig();
        Path logPath = Paths.get(MineStoreCommon.getInstance().jarFile().getParentFile().getParentFile().getAbsolutePath(), "logs", "latest.log");
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