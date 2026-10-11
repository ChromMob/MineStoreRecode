package me.chrommob.minestore.common.commandGetters;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.chrommob.minestore.api.event.MineStoreEvent;
import me.chrommob.minestore.api.event.types.MineStorePurchaseEvent;
import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.api.scheduler.SafeScheduledTask;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebContext;
import me.chrommob.minestore.api.web.WebRequest;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.GsonReponse;
import me.chrommob.minestore.common.commandGetters.dataTypes.PostResponse;
import me.chrommob.minestore.common.commandHolder.type.CheckResponse;
import me.chrommob.minestore.common.commandHolder.type.PendingCommand;
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.common.gui.payment.PaymentCreationResponse;
import me.chrommob.minestore.common.verification.VerificationResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WebListener {
    private final MineStoreVersion arraySupportedSince = new MineStoreVersion(3, 2, 5);
    private final MineStoreCommon plugin;
    public final MineStoreScheduledTask mineStoreScheduledTask;
    // Commands saved before a crash or restart are run on the first poll.
    private boolean ranWaiting = false;

    private List<ParsedResponse> fetchData() {
        List<ParsedResponse> parsedResponses = new ArrayList<>();
        try {
            if (MineStoreCommon.version().requires(arraySupportedSince)) {
                TypeToken<List<GsonReponse>> listType = new TypeToken<List<GsonReponse>>() {
                };
                WebRequest<List<GsonReponse>> request = new WebRequest.Builder<>(listType).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/queue").requiresApiKey(false).type(WebRequest.Type.GET).build();
                Result<List<GsonReponse>, WebContext> res = plugin.apiHandler().request(request);
                if (!res.isError()) {
                    for (GsonReponse response : res.value()) {
                        parsedResponses.add(parseGson(response));
                    }
                } else {
                    if (res.context().getCause() == null) {
                        throw res.context();
                    }
                    if (!(res.context().getCause() instanceof JsonSyntaxException)) {
                        throw res.context();
                    }
                }
            } else {
                WebRequest<GsonReponse> request = new WebRequest.Builder<>(GsonReponse.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/queue").requiresApiKey(false).type(WebRequest.Type.GET).build();
                Result<GsonReponse, WebContext> res = plugin.apiHandler().request(request);
                if (res.isError()) {
                    throw res.context();
                }
                GsonReponse response = res.value();
                if (response != null && response.username() != null) {
                    ParsedResponse parsedResponse = parseGson(response);
                    parsedResponses.add(parsedResponse);
                }
            }
        } catch (WebContext e) {
            plugin.debug(this.getClass(), e);
            plugin.handleError(e);
        }
        return parsedResponses;
    }

    public WebListener(MineStoreCommon plugin) {
        this.plugin = plugin;
        mineStoreScheduledTask = SafeScheduledTask.wrap("weblistener", () -> {
            plugin.debug(this.getClass(), "Running...");
            if (!MineStoreCommon.version().requires("3.0.0")) {
                return legacyPoll();
            }
            return poll();
        });
    }

    private long poll() {
        if (!ranWaiting) {
            ranWaiting = true;
            plugin.commandStorage().runWaiting();
        }
        List<ParsedResponse> parsedResponses = fetchData();
        List<ParsedResponse> commands = new ArrayList<>();
        Set<Integer> authIds = new HashSet<>();
        for (ParsedResponse parsedResponse : parsedResponses) {
            if (parsedResponse.type() == ParsedResponse.TYPE.AUTH) {
                plugin.debug(this.getClass(), "Got auth for player: " + parsedResponse.username() + " with id: "
                        + parsedResponse.authId());
                plugin.authHolder().listener(parsedResponse);
                authIds.add(parsedResponse.commandId());
                continue;
            }
            commands.add(applyPurchaseEvent(parsedResponse));
        }
        // Saved before confirming the delivery, the store never hands out a delivered command again.
        List<PendingCommand> received = plugin.commandStorage().receive(commands);
        confirmDelivered(authIds);
        plugin.commandStorage().run(received);
        // Also confirms commands executed since the last poll, e.g. by a player joining.
        confirmExecuted();
        return parsedResponses.isEmpty() ? 9500 : 500;
    }

    // Store versions before 3.0.0 only know "executed", which is confirmed on receipt.
    private long legacyPoll() {
        List<ParsedResponse> parsedResponses = fetchData();
        if (parsedResponses.isEmpty()) {
            return 9500;
        }
        List<ParsedResponse> commands = new ArrayList<>();
        for (ParsedResponse parsedResponse : parsedResponses) {
            if (parsedResponse.type() == ParsedResponse.TYPE.AUTH) {
                plugin.authHolder().listener(parsedResponse);
            } else {
                commands.add(applyPurchaseEvent(parsedResponse));
            }
        }
        plugin.commandStorage().legacyListener(commands);
        for (ParsedResponse parsedResponse : parsedResponses) {
            postExecuted(parsedResponse.commandId());
        }
        return 500;
    }

    private ParsedResponse applyPurchaseEvent(ParsedResponse parsedResponse) {
        MineStorePurchaseEvent event = new MineStorePurchaseEvent(parsedResponse.username(), parsedResponse.command(), parsedResponse.commandId(), parsedResponse.commandType() == ParsedResponse.COMMAND_TYPE.ONLINE ? MineStoreEvent.COMMAND_TYPE.ONLINE : MineStoreEvent.COMMAND_TYPE.OFFLINE);
        event.call();
        ParsedResponse.COMMAND_TYPE commandType = event.commandType() == MineStoreEvent.COMMAND_TYPE.ONLINE ? ParsedResponse.COMMAND_TYPE.ONLINE : ParsedResponse.COMMAND_TYPE.OFFLINE;
        plugin.debug(this.getClass(), "Got command: " + "\"" + parsedResponse.command() + "\""
                + " with id: " + parsedResponse.commandId() + " for player: "
                + parsedResponse.username() + " requires online: "
                + (parsedResponse.commandType().equals(ParsedResponse.COMMAND_TYPE.ONLINE)
                ? "true"
                : "false"));
        return new ParsedResponse(ParsedResponse.TYPE.COMMAND, commandType, event.command(), event.username(), event.id());
    }

    /**
     * Confirms the delivery of auth requests and of every saved command whose delivery is not confirmed yet.
     * Commands stay unconfirmed on a temporary failure and are confirmed again on the next poll.
     */
    private void confirmDelivered(Set<Integer> authIds) {
        List<PendingCommand> undelivered = plugin.commandStorage().undelivered();
        if (MineStoreCommon.version().requires(arraySupportedSince)) {
            Set<Integer> ids = new HashSet<>(authIds);
            for (PendingCommand command : undelivered) {
                ids.add(command.id());
            }
            if (!ids.isEmpty() && postDelivered(ids)) {
                plugin.commandStorage().markDelivered(undelivered);
            }
            return;
        }
        for (int authId : authIds) {
            postDelivered(authId);
        }
        for (PendingCommand command : undelivered) {
            if (postDelivered(command.id())) {
                plugin.commandStorage().markDelivered(Collections.singletonList(command));
            }
        }
    }

    /**
     * Confirms executed commands; they are removed from the saved commands once the store accepted it.
     */
    private void confirmExecuted() {
        List<PendingCommand> executed = plugin.commandStorage().executedUnconfirmed();
        if (executed.isEmpty()) {
            return;
        }
        if (MineStoreCommon.version().requires(arraySupportedSince)) {
            Set<Integer> ids = new HashSet<>();
            for (PendingCommand command : executed) {
                ids.add(command.id());
            }
            if (postExecuted(ids)) {
                plugin.commandStorage().remove(executed);
            }
            return;
        }
        for (PendingCommand command : executed) {
            if (postExecuted(command.id())) {
                plugin.commandStorage().remove(Collections.singletonList(command));
            }
        }
    }

    public VerificationResult load() {
        WebRequest<String> request = new WebRequest.Builder<>(String.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/queue").requiresApiKey(false).type(WebRequest.Type.GET).build();
        Result<String, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.debug(this.getClass(), res.context());
            if (res.context().isCloudflare()) {
                return new VerificationResult(false, Collections.singletonList("Cloudflare blocked the request, add cloudflare rule."), VerificationResult.TYPE.WEBSTORE);
            }
            return new VerificationResult(false, Collections.singletonList("Failed to fetch queue."), VerificationResult.TYPE.SECRET_KEY);
        }
        return VerificationResult.valid();
    }

    /**
     * @return false when the request failed temporarily and should be sent again.
     */
    private boolean postDelivered(int id) {
        WebRequest<Void> request = new WebRequest.Builder<>(Void.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/delivered/" + id).type(WebRequest.Type.POST).build();
        Result<Void, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.debug(this.getClass(), res.context());
            if (isTransient(res.context())) {
                plugin.log("Failed to post delivered: " + id + ", retrying on the next poll");
                return false;
            }
            plugin.log("Failed to post delivered: " + id);
        }
        return true;
    }

    /**
     * @return false when the request failed temporarily and should be sent again.
     */
    private boolean postDelivered(Set<Integer> ids) {
        JsonObject jsonObject = new JsonObject();
        JsonArray idsArrayJson = new JsonArray();
        for (int id : ids) {
            idsArrayJson.add(id);
        }
        jsonObject.add("ids", idsArrayJson);
        String json = jsonObject.toString();
        WebRequest<PostResponse> request = new WebRequest.Builder<>(PostResponse.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/delivered").type(WebRequest.Type.POST).strBody(json).build();
        Result<PostResponse, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.debug(this.getClass(), res.context());
            if (isTransient(res.context())) {
                plugin.log("Failed to post delivered, retrying on the next poll");
                return false;
            }
            plugin.log("Failed to post delivered");
            return true;
        }
        logPostResponse("delivery", res.value(), json);
        return true;
    }

    /**
     * @return false when the request failed temporarily and should be sent again.
     */
    private boolean postExecuted(Set<Integer> ids) {
        JsonObject jsonObject = new JsonObject();
        JsonArray idsArrayJson = new JsonArray();
        for (int id : ids) {
            idsArrayJson.add(id);
        }
        jsonObject.add("ids", idsArrayJson);
        String json = jsonObject.toString();
        WebRequest<PostResponse> request = new WebRequest.Builder<>(PostResponse.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/executed").type(WebRequest.Type.POST).strBody(json).build();
        Result<PostResponse, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.debug(this.getClass(), res.context());
            if (isTransient(res.context())) {
                plugin.log("Failed to post executed, retrying on the next poll");
                return false;
            }
            plugin.log("Failed to post executed");
            return true;
        }
        logPostResponse("execution", res.value(), json);
        return true;
    }

    /**
     * @return false when the request failed temporarily and should be sent again.
     */
    private boolean postExecuted(int id) {
        WebRequest<Void> request = new WebRequest.Builder<>(Void.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/executed/" + id).type(WebRequest.Type.POST).strBody(String.valueOf(id)).build();
        Result<Void, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.debug(this.getClass(), res.context());
            if (isTransient(res.context())) {
                plugin.log("Failed to post executed: " + id + ", retrying on the next poll");
                return false;
            }
            plugin.log("Failed to post executed: " + id);
        }
        return true;
    }

    private void logPostResponse(String action, PostResponse postResponse, String json) {
        if (postResponse == null) {
            plugin.log("Failed to confirm the " + action + " of commands: empty response");
            plugin.log(json);
            return;
        }
        if (!postResponse.status) {
            plugin.log("Failed to confirm the " + action + " of commands!");
            plugin.log(postResponse.error);
            plugin.log(json);
            return;
        }
        if (postResponse.results == null) {
            return;
        }
        for (PostResponse.Result result : postResponse.results) {
            if (result.status) {
                continue;
            }
            plugin.log("Failed to confirm the " + action + " of command with id: " + result.id + " with error: " + result.error);
        }
    }

    /**
     * Network failures, rate limits and server errors may succeed on a later attempt;
     * any other failure (e.g. a rejected request) would fail the same way again.
     */
    private static boolean isTransient(WebContext context) {
        int responseCode = context.responseCode();
        return responseCode == -1 || responseCode == 429 || responseCode >= 500;
    }

    private ParsedResponse parseGson(GsonReponse response) {
        ParsedResponse.TYPE type;
        if (response.getType() != null) {
            type = ParsedResponse.TYPE.AUTH;
        } else {
            type = ParsedResponse.TYPE.COMMAND;
        }
        if (type == ParsedResponse.TYPE.COMMAND) {
            ParsedResponse.COMMAND_TYPE commandType;
            if (response.isPlayerOnlineNeeded()) {
                commandType = ParsedResponse.COMMAND_TYPE.ONLINE;
            } else {
                commandType = ParsedResponse.COMMAND_TYPE.OFFLINE;
            }
            return new ParsedResponse(type, commandType, response.command(), response.username(), response.requestId());
        } else {
            return new ParsedResponse(type, response.username(), response.authId(), response.requestId());
        }
    }

    public CompletableFuture<CheckResponse> checkCommands(Set<Integer> ids) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject jsonObject = new JsonObject();
            JsonArray idsArrayJson = new JsonArray();
            for (int id : ids) {
                idsArrayJson.add(id);
            }
            jsonObject.add("ids", idsArrayJson);
            String json = jsonObject.toString();
            WebRequest<CheckResponse> request = new WebRequest.Builder<>(CheckResponse.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/validated").type(WebRequest.Type.POST).strBody(json).build();
            Result<CheckResponse, WebContext> res = plugin.apiHandler().request(request);
            if (res.isError()) {
                plugin.debug(this.getClass(), res.context());
                return CheckResponse.failed();
            }
            return res.value();
        }).exceptionally(e -> {
            plugin.debug(getClass(), e);
            return CheckResponse.failed();
        });
    }

    public CompletableFuture<PaymentCreationResponse> createPayment(String username, int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("username", username);
            jsonObject.addProperty("item_id", itemId);
            jsonObject.addProperty("virtual_currency", true);
            String json = jsonObject.toString();
            String path = "rest/v2/" + ConfigKeys.API_KEYS.KEY.getValue() + "/payment/create";
            WebRequest<PaymentCreationResponse> request = new WebRequest.Builder<>(PaymentCreationResponse.class).path(path).type(WebRequest.Type.POST).strBody(json).requiresApiKey(false).build();
            Result<PaymentCreationResponse, WebContext> res = plugin.apiHandler().request(request);
            if (res.isError()) {
                plugin.debug(this.getClass(), res.context());
                return null;
            }
            return res.value();
        });
    }
}
