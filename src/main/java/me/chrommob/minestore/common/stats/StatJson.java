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
    private final String systemArchitecture;
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
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) new java.net.URL("https://api.chrommob.fun/minestore/data").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.getOutputStream().write(("{\"uuid\":\"" + uuid.toString() + "\",\"javaVersion\":\"" + javaVersion + "\",\"platformType\":\"" + platformType + "\",\"platformName\":\"" + platformName + "\",\"platformVersion\":\"" + platformVersion + "\",\"coreCount\":" + coreCount + ",\"systemArchitecture\":\"" + systemArchitecture + "\",\"playerCount\":" + playerCount + "}").getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            connection.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
//        System.out.println("Sending stat update: " + "{\"uuid\":\"" + uuid.toString() + "\",\"javaVersion\":\"" + javaVersion + "\",\"platformType\":\"" + platformType + "\",\"platformName\":\"" + platformName + "\",\"platformVersion\":\"" + platformVersion + "\",\"coreCount\":" + coreCount + ",\"systemArchitecture\":\"" + systemArchitecture + "\",\"playerCount\":" + playerCount + "}");
    }
}
