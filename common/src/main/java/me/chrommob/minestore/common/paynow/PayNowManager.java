package me.chrommob.minestore.common.paynow;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.leangen.geantyref.TypeToken;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.common.paynow.json.*;
import me.chrommob.minestore.common.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.common.verification.VerificationResult;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PayNowManager {
    private MineStoreCommon mineStoreCommon;
    private final static Gson gson = new Gson();
    private TokenReadResponse tokenReadResponse;
    private static final URI API_LINK_URL = URI.create("https://api.paynow.gg/v1/delivery/gameserver/link");
    private static final URI API_QUEUE_URL = URI.create("https://api.paynow.gg/v1/delivery/command-queue/");
    private static final URI API_EVENT_URL = URI.create("https://api.paynow.gg/v1/delivery/events/");

    private final List<PayNowEvent> events = new ArrayList<>();

    public final MineStoreScheduledTask mineStoreScheduledTask = new MineStoreScheduledTask("paynow", () -> {
        if (!isEnabled()) return;


        List<PayNowEvent> localEvents = new ArrayList<>(events);
        events.removeAll(localEvents);
        try {
            HttpsURLConnection connection = (HttpsURLConnection) API_EVENT_URL.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Gameserver " + tokenReadResponse.token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();
            OutputStream os = connection.getOutputStream();
            String data = gson.toJson(localEvents.toArray(new PayNowEvent[0]));
            mineStoreCommon.debug(this.getClass(), "Sending events to paynow:");
            mineStoreCommon.debug(this.getClass(), data);
            os.write(data.getBytes());
            os.flush();
            os.close();
            connection.getInputStream();
            connection.disconnect();
        } catch (IOException e) {
            mineStoreCommon.debug(this.getClass(), e);
        }

        try {
            List<String> names = new ArrayList<>();
            List<UUID> uuids = new ArrayList<>();
            Registries.USER_GETTER.get().getAllPlayers().forEach(player -> {
                names.add(player.commonUser().getName());
                uuids.add(player.commonUser().getUUID());
            });
            PlayerList playerList = new PlayerList(names, uuids);
            String json = gson.toJson(playerList);
            mineStoreCommon.debug(this.getClass(), "Sending server data to paynow: " + json);

            HttpsURLConnection connection = (HttpsURLConnection) API_QUEUE_URL.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Gameserver " + tokenReadResponse.token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();
            connection.getOutputStream().write(json.getBytes());
            if (connection.getResponseCode() != 200) {
                return;
            }
            String response = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining("\n"));
            mineStoreCommon.debug(this.getClass(), "Received paynow commands (ignoring and confirming): " + response);
            TypeToken<List<QueuedCommand>> queuedCommandType = new TypeToken<List<QueuedCommand>>() {
            };
            List<QueuedCommand> queuedCommands = gson.fromJson(response, queuedCommandType.getType());
            if (queuedCommands == null || queuedCommands.isEmpty()) {
                return;
            }
            List<CommandAttempt> commandAttempts = new ArrayList<>();
            for (QueuedCommand queuedCommand : queuedCommands) {
                commandAttempts.add(new CommandAttempt(queuedCommand.getAttemptId()));
            }
            try {
                HttpsURLConnection delete = (HttpsURLConnection) API_QUEUE_URL.toURL().openConnection();
                delete.setRequestMethod("DELETE");
                delete.setRequestProperty("Content-Type", "application/json");
                delete.setRequestProperty("Authorization", "Gameserver " + tokenReadResponse.token);
                delete.setRequestProperty("Accept", "application/json");
                delete.setDoOutput(true);
                delete.setDoInput(true);
                delete.connect();
                OutputStream os = delete.getOutputStream();
                os.write(gson.toJson(commandAttempts).getBytes());
                os.flush();
                os.close();
                delete.getInputStream();
                delete.disconnect();
                mineStoreCommon.debug(this.getClass(), "Deleted commands");
            } catch (IOException e) {
                mineStoreCommon.debug(this.getClass(), e);
            }
        } catch (Exception e) {
            mineStoreCommon.debug(this.getClass(), e);
        }
    }, 1000 * 60);

    public final MineStoreScheduledTask initTask = new MineStoreScheduledTask("paynow", () -> {
        if (!isEnabled()) return;
        LinkRequest linkRequest = new LinkRequest(Registries.IP.get().getAddress().getHostAddress() + ":" + Registries.IP.get().getPort(), Registries.HOSTNAME.get(), Registries.PLATFORM_NAME.get(), Registries.PLATFORM_VERSION.get());
        try {
            HttpsURLConnection connection = (HttpsURLConnection) API_LINK_URL.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Gameserver " + tokenReadResponse.token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            mineStoreCommon.debug(this.getClass(), "Sending link request to paynow:");
            String linkRequestString = gson.toJson(linkRequest);
            mineStoreCommon.debug(this.getClass(), linkRequestString);
            connection.getOutputStream().write(linkRequestString.getBytes(StandardCharsets.UTF_8));
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                return;
            }
            String response = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining("\n"));
            JsonObject responseJson = gson.fromJson(response, JsonObject.class);

            if (!responseJson.has("gameserver")) {
                mineStoreCommon.log("PayNow API did not return a GameServer object, this may be a transient issue, please try again or contact support.");
                return;
            }

            JsonObject gameServer = responseJson.get("gameserver").getAsJsonObject();
            String gsName = gameServer.get("name").getAsString();
            String gsId = gameServer.get("id").getAsString();

            mineStoreCommon.log("Successfully connected to PayNow using the token for \"" + gsName + "\" (" + gsId + ")");
        } catch (Exception e) {
            mineStoreCommon.debug(this.getClass(), e);
        }
    }, 1000 * 60 * 5);


    public PayNowManager(MineStoreCommon mineStoreCommon) {
        this.mineStoreCommon = mineStoreCommon;
    }

    public VerificationResult load() {
        tokenReadResponse = readToken();
        mineStoreCommon.debug(this.getClass(), "Token read response: " + gson.toJson(tokenReadResponse));
        if (tokenReadResponse.success) {
            return VerificationResult.valid();
        }
        return new VerificationResult(false, tokenReadResponse.message == null ? Collections.singletonList("Failed to read token") : Collections.singletonList(tokenReadResponse.message), VerificationResult.TYPE.WEBSTORE);
    }

    public boolean isEnabled() {
        return tokenReadResponse != null && tokenReadResponse.success && tokenReadResponse.token != null;
    }

    public void onJoin(AbstractUser user) {
        if (!isEnabled()) {
            return;
        }
        PayNowEvent event = new PayNowEvent("player_join", new PayNowEvent.PlayerJoin(user.commonUser().getAddress().getHostString(), user.commonUser().getUUID().toString(), user.commonUser().getName()));
        events.add(event);
    }

    public static String decryptToken(String base64Payload, String apiKey) throws Exception {
        byte[] outerBytes = Base64.getDecoder().decode(base64Payload);
        byte[] iv = Arrays.copyOfRange(outerBytes, 0, 16);
        byte[] innerBase64 = Arrays.copyOfRange(outerBytes, 16, outerBytes.length);
        byte[] encryptedToken = Base64.getDecoder().decode(innerBase64);
        byte[] keyBytes = Arrays.copyOf(apiKey.getBytes(StandardCharsets.UTF_8), 32);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedToken);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static class TokenReadResponse {
        TokenReadResponse() {
            success = false;
            message = "Initial error, no error message";
            token = null;
        }

        boolean success;
        String message;
        String token;
    }

    public TokenReadResponse readToken() {
        String storeUrl = ConfigKeys.STORE_URL.getValue();
        String apiKey = ConfigKeys.API_KEYS.KEY.getValue();
        String secretKey = ConfigKeys.WEBLISTENER_KEYS.KEY.getValue();
        TokenReadResponse response = new TokenReadResponse();
        try {
            if (storeUrl.endsWith("/")) {
                storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
            }
            HttpsURLConnection connection = getHttpsURLConnection(storeUrl, secretKey);
            if (connection.getResponseCode() == 200) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
                if (!json.has("status")) {
                    response.message = "No status field";
                    return response;
                }
                response.success = json.get("status").getAsBoolean();
                if (!response.success && json.has("message")) {
                    response.message = json.get("message").getAsString();
                    return response;
                } else if (!response.success) {
                    response.message = "No error message";
                    return response;
                }
                if (!json.has("api_token")) {
                    if (json.has("message")) {
                        response.message = json.get("message").getAsString();
                    } else {
                        response.message = "No api_token field error.";
                    }
                    return response;
                }
                response.token = decryptToken(json.get("api_token").getAsString(), apiKey);
                return response;
            } else {
                response.message = "Server returned " + connection.getResponseCode();
                return response;
            }
        } catch (Exception e) {
            response.message = e.getMessage();
            mineStoreCommon.debug(this.getClass(), e);
            return response;
        }
    }

    private static HttpsURLConnection getHttpsURLConnection(String storeUrl, String secretKey) throws URISyntaxException, IOException {
        String path = "/api/servers/" + secretKey + "/commands/paynow";
        URI uri = new URI(storeUrl + path);
        HttpsURLConnection connection = (HttpsURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();
        return connection;
    }
}
