package me.chrommob.minestore.common.stats;

import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.util.UUID;

public class StatJson {
    private final UUID uuid;
    private final String javaVersion;
    private final String platformType;
    private final String platformName;
    private final String platformVersion;
    private final int coreCount;
    private String systemArchitecture;
    private int playerCount;
    public StatJson(UUID uuid, String javaVersion, String platformType, String platformName, String platformVersion, int coreCount, String systemArchitecture) {
        this.uuid = uuid;
        this.javaVersion = javaVersion;
        this.platformType = platformType;
        this.platformName = platformName;
        this.platformVersion = platformVersion;
        this.coreCount = coreCount;
        this.systemArchitecture = systemArchitecture;
    }

    public void send(int playerCount) {
        this.playerCount = playerCount;
    }
}
