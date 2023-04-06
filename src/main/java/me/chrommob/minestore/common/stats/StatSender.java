package me.chrommob.minestore.common.stats;


import com.google.gson.Gson;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;

import javax.net.ssl.HttpsURLConnection;
import java.util.UUID;

public class StatSender {
    private final MineStoreCommon common;
    private final UUID SERVERUUID;
    private final String JAVA_VERSION;
    private final String PLATFORM_TYPE;
    private final String PLATFORM_NAME;
    private final String PLATFORM_VERSION;
    private final int CORE_COUNT;
    private final String SYSTEM_ARCHITECTURE;
    private final Gson gson = new Gson();
    private Thread thread;

    public StatSender(MineStoreCommon common) {
        this.common = common;
        SERVERUUID = UUID.fromString((String) common.configReader().get(ConfigKey.SERVER_UUID));
        JAVA_VERSION = System.getProperty("java.version");
        PLATFORM_TYPE = common.getPlatform();
        PLATFORM_NAME = common.getPlatformName();
        PLATFORM_VERSION = common.getPlatformVersion();
        CORE_COUNT = Runtime.getRuntime().availableProcessors();
        SYSTEM_ARCHITECTURE = System.getProperty("os.arch");
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }


    public void start() {
        thread = new Thread(() -> {
            while (true) {
                int playerCount = common.userGetter().getAllPlayers().size();
                StatJson statJson = new StatJson(SERVERUUID, JAVA_VERSION, PLATFORM_TYPE, PLATFORM_NAME, PLATFORM_VERSION, CORE_COUNT, SYSTEM_ARCHITECTURE);
                statJson.send(playerCount);
                HttpsURLConnection connection = null;
                try {
                    connection = (HttpsURLConnection) new java.net.URL("https://api.chrommob.fun/minestore/data").openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.getOutputStream().write(gson.toJson(statJson).getBytes());
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
                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        thread.start();
    }
}
