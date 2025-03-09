package me.chrommob.minestore.common.stats;


import com.google.gson.Gson;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.common.MineStoreCommon;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class StatSender {
    private final MineStoreCommon common;
    private final UUID SERVERUUID;
    private final String JAVA_VERSION;
    private final String PLATFORM_TYPE;
    private final String PLATFORM_NAME;
    private final String PLATFORM_VERSION;
    private final String PLUGIN_VERSION;
    private final int CORE_COUNT;
    private final String SYSTEM_ARCHITECTURE;
    private final Gson gson = new Gson();
    private Thread thread;

    public StatSender(MineStoreCommon common) {
        this.common = common;
        SERVERUUID = generateUUIDFromStrings(common, common.pluginConfig().getKey("store-url").getAsString(), common.pluginConfig().getKey("api").getKey("key").getAsString(), common.pluginConfig().getKey("weblistener").getKey("secret-key").getAsString());
        JAVA_VERSION = System.getProperty("java.version");
        PLATFORM_TYPE = Registries.PLATFORM.get();
        PLATFORM_NAME = Registries.PLATFORM_NAME.get();
        PLATFORM_VERSION = Registries.PLATFORM_VERSION.get();
        PLUGIN_VERSION = "3.3.2";
        CORE_COUNT = Runtime.getRuntime().availableProcessors();
        SYSTEM_ARCHITECTURE = System.getProperty("os.arch");
    }

    public static UUID generateUUIDFromStrings(MineStoreCommon common, String... strings) {
        try {
            // Combine the strings
            StringBuilder combined = new StringBuilder();
            for (String s : strings) {
                combined.append(s);
            }

            // Create a hash of the combined string
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(combined.toString().getBytes(StandardCharsets.UTF_8));

            return UUID.nameUUIDFromBytes(hash);
        } catch (NoSuchAlgorithmException e) {
            common.log("SHA-256 algorithm not found, generating random UUID");
            return UUID.randomUUID();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }


    public void start() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread(() -> {
            while (true) {
                int playerCount = Registries.USER_GETTER.get().getAllPlayers().size();
                StatJson statJson = new StatJson(SERVERUUID, JAVA_VERSION, PLATFORM_TYPE, PLATFORM_NAME, PLATFORM_VERSION, PLUGIN_VERSION, CORE_COUNT, SYSTEM_ARCHITECTURE);
                statJson.setPlayerCount(playerCount);
                String json = gson.toJson(statJson);
                common.debug(this.getClass(), "Sending stat json: " + json);
                HttpsURLConnection connection = null;
                try {
                    connection = (HttpsURLConnection) new java.net.URL("https://api.chrommob.fun/minestore/data").openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.getOutputStream().write(json.getBytes());
                    connection.getOutputStream().flush();
                    connection.getOutputStream().close();
                    connection.getInputStream().close();
                } catch (IOException e) {
                    common.log(e.getMessage());
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
