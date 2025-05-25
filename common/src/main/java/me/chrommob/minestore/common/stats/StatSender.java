package me.chrommob.minestore.common.stats;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.api.stats.BuildConstats;
import me.chrommob.minestore.common.MineStoreCommon;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class StatSender {
    private final MineStoreCommon common;
    private final UUID SERVERUUID;
    private final UUID STORE_UUID;
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
        STORE_UUID = generateUUIDFromStrings(common, common.pluginConfig().getKey("store-url").getAsString(), common.pluginConfig().getKey("api").getKey("key").getAsString());
        JAVA_VERSION = System.getProperty("java.version");
        PLATFORM_TYPE = Registries.PLATFORM.get();
        PLATFORM_NAME = Registries.PLATFORM_NAME.get();
        PLATFORM_VERSION = Registries.PLATFORM_VERSION.get();
        PLUGIN_VERSION = BuildConstats.VERSION;
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
                StatJson statJson = new StatJson(SERVERUUID, JAVA_VERSION, PLATFORM_TYPE, PLATFORM_NAME, PLATFORM_VERSION, PLUGIN_VERSION, CORE_COUNT, SYSTEM_ARCHITECTURE, MineStoreCommon.version() == MineStoreVersion.dummy() ? "Pre 3.0.0" : MineStoreCommon.version().toString());
                statJson.setPlayerCount(playerCount);
                int storePlayerCount = getPlayerCount(common.pluginConfig().getKey("store-url").getAsString());
                String json;
                if (storePlayerCount != -1) {
                    WebStoreJson webStoreJson = new WebStoreJson(STORE_UUID, storePlayerCount);
                    json = gson.toJson(webStoreJson);
                    sendStoreData(json);
                }
                json = gson.toJson(statJson);
                sendData(json);
                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        thread.start();
    }

    private void sendData(String json) {
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
    }

    private void sendStoreData(String json) {
        common.debug(this.getClass(), "Sending store json: " + json);
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) new java.net.URL("https://api.chrommob.fun/minestore/playerCountData").openConnection();
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
    }

    private int getPlayerCount(String ip, int port) {
        String url = "https://yamcsrvstatus.chrommob.fun/api/" + ip + ":" + port;
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) new java.net.URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
            return jsonObject.getAsJsonObject("players").get("online").getAsInt();
        } catch (Exception e) {
            return 0;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private int getPlayerCount(String storeUrl) {
        storeUrl = storeUrl.endsWith("/") ? storeUrl : storeUrl + "/";
        String apiUrl = storeUrl + "api/settings/get";
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) new java.net.URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
            String ip = jsonObject.getAsJsonObject("server").get("ip").getAsString();
            int port = jsonObject.getAsJsonObject("server").get("port").getAsInt();
            return getPlayerCount(ip, port);
        } catch (Exception e) {
            return -1;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
