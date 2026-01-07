package me.chrommob.minestore.common.stats;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.api.stats.BuildConstats;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebContext;
import me.chrommob.minestore.api.web.WebRequest;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKeys;

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

    public StatSender(MineStoreCommon common) {
        this.common = common;
        SERVERUUID = generateUUIDFromStrings(common, ConfigKeys.STORE_URL.getValue(), ConfigKeys.API_KEYS.KEY.getValue(), ConfigKeys.WEBLISTENER_KEYS.KEY.getValue());
        STORE_UUID = generateUUIDFromStrings(common, ConfigKeys.STORE_URL.getValue(), ConfigKeys.API_KEYS.KEY.getValue());
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

    public final MineStoreScheduledTask mineStoreScheduledTask = new MineStoreScheduledTask("statSender", new Runnable() {
        @Override
        public void run() {
            int playerCount = Registries.USER_GETTER.get().getAllPlayers().size();
            StatJson statJson = new StatJson(SERVERUUID, JAVA_VERSION, PLATFORM_TYPE, PLATFORM_NAME, PLATFORM_VERSION, PLUGIN_VERSION, CORE_COUNT, SYSTEM_ARCHITECTURE, MineStoreCommon.version() == MineStoreVersion.dummy() ? "Pre 3.0.0" : MineStoreCommon.version().toString());
            statJson.setPlayerCount(playerCount);
            int storePlayerCount = getPlayerCount(ConfigKeys.STORE_URL.getValue());
            String json;
            if (storePlayerCount != -1) {
                WebStoreJson webStoreJson = new WebStoreJson(STORE_UUID, storePlayerCount);
                json = gson.toJson(webStoreJson);
                sendStoreData(json);
            }
            json = gson.toJson(statJson);
            sendData(json);
        }
    }, 1000 * 60);

    private void sendData(String json) {
        common.debug(this.getClass(), "Sending stat json: " + json);
        WebRequest<Void> request = new WebRequest.Builder<>(Void.class).customUrl("https://api.chrommob.fun/minestore/data").type(WebRequest.Type.POST).strBody(json).header("Content-Type", "application/json").build();
        Result<Void, WebContext> res = common.apiHandler().request(request);
        if (res.isError()) {
            common.log("Failed to send stat data");
            common.debug(this.getClass(), res.context());
        }
    }

    private void sendStoreData(String json) {
        common.debug(this.getClass(), "Sending store json: " + json);
        WebRequest<Void> request = new WebRequest.Builder<>(Void.class).customUrl("https://api.chrommob.fun/minestore/playerCountData").type(WebRequest.Type.POST).strBody(json).header("Content-Type", "application/json").build();
        Result<Void, WebContext> res = common.apiHandler().request(request);
        if (res.isError()) {
            common.log("Failed to send store data");
            common.debug(this.getClass(), res.context());
        }
    }

    private int getPlayerCount(String ip, int port) {
        String url = "https://yamcsrvstatus.chrommob.fun/api/" + ip + ":" + port;
        WebRequest<JsonObject> request = new WebRequest.Builder<>(JsonObject.class).customUrl(url).type(WebRequest.Type.GET).build();
        Result<JsonObject, WebContext> res = common.apiHandler().request(request);
        if (res.isError()) {
            common.log("Failed to get player count");
            common.debug(this.getClass(), res.context());
            return 0;
        }
        JsonObject json = res.value();
        if (!json.has("players") || !json.get("players").isJsonObject() || !json.get("players").getAsJsonObject().has("online") || !json.get("players").getAsJsonObject().get("online").isJsonPrimitive()) {
            return 0;
        }
        return json.get("players").getAsJsonObject().get("online").getAsInt();
    }

    private int getPlayerCount(String storeUrl) {
        WebRequest<JsonObject> request = new WebRequest.Builder<>(JsonObject.class).path("settings/get").type(WebRequest.Type.GET).build();
        Result<JsonObject, WebContext> res = common.apiHandler().request(request);
        if (res.isError()) {
            common.log("Failed to get player count");
            common.debug(this.getClass(), res.context());
            return 0;
        }
        JsonObject jsonObject = res.value();
        if (!jsonObject.has("server") || !jsonObject.get("server").isJsonObject() || !jsonObject.get("server").getAsJsonObject().has("ip") || !jsonObject.get("server").getAsJsonObject().has("port")) {
            return 0;
        }
        String ip = jsonObject.getAsJsonObject("server").get("ip").getAsString();
        int port = jsonObject.getAsJsonObject("server").get("port").getAsInt();
        return getPlayerCount(ip, port);
    }
}
