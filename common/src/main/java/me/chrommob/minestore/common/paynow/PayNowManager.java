package me.chrommob.minestore.common.paynow;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebContext;
import me.chrommob.minestore.api.web.WebRequest;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.common.paynow.json.*;
import me.chrommob.minestore.common.verification.VerificationResult;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PayNowManager {
    private MineStoreCommon mineStoreCommon;
    private final static Gson gson = new Gson();
    private TokenReadResponse tokenReadResponse;
    private static final String CUSTOM_URL = "https://api.paynow.gg/v1/delivery/";
    private final List<PayNowEvent> events = new ArrayList<>();

    public final MineStoreScheduledTask mineStoreScheduledTask = new MineStoreScheduledTask("paynow", () -> {
        if (!isEnabled()) return;

        {
            List<PayNowEvent> localEvents = new ArrayList<>(events);
            events.removeAll(localEvents);
            String data = gson.toJson(localEvents.toArray(new PayNowEvent[0]));
            WebRequest<Void> request = new WebRequest.Builder<>(Void.class).customUrl(CUSTOM_URL).path("events").type(WebRequest.Type.POST).strBody(data).header("Authorization", "Gameserver " + tokenReadResponse.token).build();
            Result<Void, WebContext> res = mineStoreCommon.apiHandler().request(request);
            if (res.isError()) {
                mineStoreCommon.log("Failed to send events to paynow");
                mineStoreCommon.debug(this.getClass(), res.context());
            }
            mineStoreCommon.debug(this.getClass(), "Sent events to paynow");
        }
        List<CommandAttempt> commandAttempts = new ArrayList<>();
        {
            List<String> names = new ArrayList<>();
            List<UUID> uuids = new ArrayList<>();
            Registries.USER_GETTER.get().getAllPlayers().forEach(player -> {
                names.add(player.commonUser().getName());
                uuids.add(player.commonUser().getUUID());
            });
            PlayerList playerList = new PlayerList(names, uuids);
            String json = gson.toJson(playerList);
            mineStoreCommon.debug(this.getClass(), "Sending server data to paynow: " + json);
            TypeToken<List<QueuedCommand>> queuedCommandType = new TypeToken<List<QueuedCommand>>() {
            };
            WebRequest<List<QueuedCommand>> request = new WebRequest.Builder<>(queuedCommandType).customUrl(CUSTOM_URL).path("command-queue").type(WebRequest.Type.POST).strBody(json).header("Authorization", "Gameserver " + tokenReadResponse.token).build();
            Result<List<QueuedCommand>, WebContext> res = mineStoreCommon.apiHandler().request(request);

            if (res.isError()) {
                mineStoreCommon.log("Failed to send server data to paynow");
                mineStoreCommon.debug(this.getClass(), res.context());
                return;
            }
            List<QueuedCommand> queuedCommands = res.value();
            mineStoreCommon.debug(this.getClass(), "Received paynow commands (ignoring and confirming): " + queuedCommands);
            if (queuedCommands == null || queuedCommands.isEmpty()) {
                return;
            }
            for (QueuedCommand queuedCommand : queuedCommands) {
                commandAttempts.add(new CommandAttempt(queuedCommand.getAttemptId()));
            }
        }
        String json = gson.toJson(commandAttempts);
        WebRequest<Void> request = new WebRequest.Builder<>(Void.class).customUrl(CUSTOM_URL).path("command-queue").type(WebRequest.Type.DELETE).strBody(json).header("Authorization", "Gameserver " + tokenReadResponse.token).build();
        Result<Void, WebContext> res = mineStoreCommon.apiHandler().request(request);
        if (res.isError()) {
            mineStoreCommon.log("Failed to delete commands from paynow");
            mineStoreCommon.debug(this.getClass(), res.context());
        }
        mineStoreCommon.debug(this.getClass(), "Deleted commands");
    }, 1000 * 60);

    public final MineStoreScheduledTask initTask = new MineStoreScheduledTask("paynow", () -> {
        if (!isEnabled()) return;
        LinkRequest linkRequest = new LinkRequest(Registries.IP.get().getAddress().getHostAddress() + ":" + Registries.IP.get().getPort(), Registries.HOSTNAME.get(), Registries.PLATFORM_NAME.get(), Registries.PLATFORM_VERSION.get());
        WebRequest<JsonObject> request = new WebRequest.Builder<>(JsonObject.class).customUrl(CUSTOM_URL).path("gameserver/link").type(WebRequest.Type.POST).strBody(gson.toJson(linkRequest)).header("Authorization", "Gameserver " + tokenReadResponse.token).build();
        Result<JsonObject, WebContext> res = mineStoreCommon.apiHandler().request(request);
        if (res.isError()) {
            mineStoreCommon.log("Failed to send link request to paynow");
            mineStoreCommon.debug(this.getClass(), res.context());
            return;
        }
        JsonObject responseJson = res.value();

        if (!responseJson.has("gameserver")) {
            mineStoreCommon.log("PayNow API did not return a GameServer object, this may be a transient issue, please try again or contact support.");
            return;
        }

        JsonObject gameServer = responseJson.get("gameserver").getAsJsonObject();
        String gsName = gameServer.get("name").getAsString();
        String gsId = gameServer.get("id").getAsString();

        mineStoreCommon.log("Successfully connected to PayNow using the token for \"" + gsName + "\" (" + gsId + ")");
    }, 0);


    public PayNowManager(MineStoreCommon mineStoreCommon) {
        this.mineStoreCommon = mineStoreCommon;
        MineStoreEventBus.registerListener(mineStoreCommon.getInternalAddon(), MineStorePlayerJoinEvent.class, event -> onJoin(Registries.USER_GETTER.get().get(event.getUsername())));
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
        String apiKey = ConfigKeys.API_KEYS.KEY.getValue();
        String secretKey = ConfigKeys.WEBLISTENER_KEYS.KEY.getValue();
        TokenReadResponse response = new TokenReadResponse();
        WebRequest<JsonObject> request = new WebRequest.Builder<>(JsonObject.class).path("servers/" + secretKey + "/commands/paynow").requiresApiKey(false).type(WebRequest.Type.GET).build();
        Result<JsonObject, WebContext> res = mineStoreCommon.apiHandler().request(request);
        if (res.isError()) {
            mineStoreCommon.debug(this.getClass(), res.context());
            return response;
        }
        JsonObject json = res.value();
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
        try {
            response.token = decryptToken(json.get("api_token").getAsString(), apiKey);
        } catch (Exception e) {
            response.message = e.getMessage();
            mineStoreCommon.debug(this.getClass(), e);
            return response;
        }
        return response;
    }
}
