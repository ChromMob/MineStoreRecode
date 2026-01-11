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
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.common.gui.payment.PaymentCreationResponse;
import me.chrommob.minestore.common.verification.VerificationResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WebListener {
    private final MineStoreVersion arraySupportedSince = new MineStoreVersion(3, 2, 5);
    private final MineStoreCommon plugin;
    private boolean wasEmpty = false;
    private final Set<String> toPostExecuted = new HashSet<>();
    public final MineStoreScheduledTask mineStoreScheduledTask;

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

    private void handleExecuted() {
        if (toPostExecuted.isEmpty()) {
            return;
        }
        Set<String> toPostExecutedCopy = new HashSet<>(toPostExecuted);
        toPostExecuted.clear();
        if (MineStoreCommon.version().requires(arraySupportedSince)) {
            postExecutedAsync(toPostExecutedCopy);
            return;
        }
        for (String id : toPostExecutedCopy) {
            postExecutedAsync(id);
        }
    }

    public WebListener(MineStoreCommon plugin) {
        this.plugin = plugin;
        mineStoreScheduledTask = SafeScheduledTask.wrap("weblistener", () -> {
            handleExecuted();
            plugin.debug(this.getClass(), "Running...");
            List<ParsedResponse> parsedResponses = fetchData();
            if (wasEmpty || parsedResponses.isEmpty()) {
                plugin.debug(this.getClass(), wasEmpty ? "Issue while parsing json" : "Parsed responses is empty");
                wasEmpty = false;
                return 9500;
            }
            Set<Integer> toPostDelivered = new HashSet<>();
            List<ParsedResponse> commands = new ArrayList<>();
            for (ParsedResponse parsedResponse : parsedResponses) {
                if (parsedResponse.type() != ParsedResponse.TYPE.COMMAND) {
                    continue;
                }
                MineStorePurchaseEvent event = new MineStorePurchaseEvent(parsedResponse.username(), parsedResponse.command(), parsedResponse.commandId(), parsedResponse.commandType() == ParsedResponse.COMMAND_TYPE.ONLINE ? MineStoreEvent.COMMAND_TYPE.ONLINE : MineStoreEvent.COMMAND_TYPE.OFFLINE);
                event.call();
                ParsedResponse.COMMAND_TYPE commandType = event.commandType() == MineStoreEvent.COMMAND_TYPE.ONLINE ? ParsedResponse.COMMAND_TYPE.ONLINE : ParsedResponse.COMMAND_TYPE.OFFLINE;
                commands.add(new ParsedResponse(ParsedResponse.TYPE.COMMAND, commandType, event.command(), event.username(), event.id()));
                plugin.debug(this.getClass(), "Got command: " + "\"" + parsedResponse.command() + "\""
                        + " with id: " + parsedResponse.commandId() + " for player: "
                        + parsedResponse.username() + " requires online: "
                        + (parsedResponse.commandType().equals(ParsedResponse.COMMAND_TYPE.ONLINE)
                        ? "true"
                        : "false"));
            }
            plugin.commandStorage().listener(commands);
            for (ParsedResponse parsedResponse : parsedResponses) {
                toPostDelivered.add(parsedResponse.commandId());
                if (parsedResponse.type() == ParsedResponse.TYPE.AUTH) {
                    plugin.debug(this.getClass(), "Got auth for player: " + parsedResponse.username() + " with id: "
                            + parsedResponse.authId());
                    plugin.authHolder().listener(parsedResponse);
                }
                if (MineStoreCommon.version().requires("3.0.0")) {
                    if (!MineStoreCommon.version().requires(arraySupportedSince)) {
                        postDelivered(String.valueOf(parsedResponse.commandId()));
                    }
                    if (parsedResponse.commandType() == ParsedResponse.COMMAND_TYPE.OFFLINE) {
                        postExecuted(String.valueOf(parsedResponse.commandId()));
                    }
                } else {
                    postExecuted(String.valueOf(parsedResponse.commandId()));
                }
            }
            if (MineStoreCommon.version().requires(arraySupportedSince)) {
                int[] toPostDeliveredArray = new int[toPostDelivered.size()];
                int i = 0;
                for (Integer id : toPostDelivered) {
                    toPostDeliveredArray[i++] = id;
                }
                postDelivered(toPostDeliveredArray);
            }
            return 500;
        });
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

    private void postDelivered(String id) {
        WebRequest<Void> request = new WebRequest.Builder<>(Void.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/delivered/" + id).type(WebRequest.Type.POST).build();
        Result<Void, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.log("Failed to post delivered: " + id);
            plugin.debug(this.getClass(), res.context());
        }
    }

    private void postDelivered(int[] ids) {
        if (ids.length == 0) {
            return;
        }
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
            plugin.log("Failed to post delivered");
            plugin.debug(this.getClass(), res.context());
        }
        PostResponse postResponse = res.value();
        if (postResponse.status) {
            for (PostResponse.Result result : postResponse.results) {
                if (result.status) {
                    continue;
                }
                plugin.log("Failed to confirm the delivery of command with id: " + result.id + " with error: " + result.error);
            }
        } else {
            plugin.log("Failed to confirm the delivery of commands!");
            plugin.log(postResponse.error);
            plugin.log(json);
        }
    }

    public void postExecuted(String id) {
        toPostExecuted.add(id);
    }

    private void postExecutedAsync(Set<String> ids) {
        int[] idsArray = new int[ids.size()];
        int i = 0;
        for (String id : ids) {
            idsArray[i++] = Integer.parseInt(id);
        }
        JsonObject jsonObject = new JsonObject();
        JsonArray idsArrayJson = new JsonArray();
        for (int id : idsArray) {
            idsArrayJson.add(id);
        }
        jsonObject.add("ids", idsArrayJson);
        String json = jsonObject.toString();
        WebRequest<PostResponse> request = new WebRequest.Builder<>(PostResponse.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/executed").type(WebRequest.Type.POST).strBody(json).build();
        Result<PostResponse, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.log("Failed to post executed");
            plugin.debug(this.getClass(), res.context());
        }
        PostResponse postResponse = res.value();
        if (postResponse.status) {
            for (PostResponse.Result result : postResponse.results) {
                if (result.status) {
                    continue;
                }
                plugin.log("Failed to confirm the execution of command with id: " + result.id + " with error: " + result.error);
            }
        } else {
            plugin.log("Failed to confirm the execution of commands!");
            plugin.log(postResponse.error);
            plugin.log(json);
        }
    }

    private void postExecutedAsync(String id) {
        WebRequest<Void> request = new WebRequest.Builder<>(Void.class).path("servers/" + (ConfigKeys.WEBLISTENER_KEYS.ENABLED.getValue() ? ConfigKeys.WEBLISTENER_KEYS.KEY.getValue() + "/" : "") + "commands/executed/" + id).type(WebRequest.Type.POST).strBody(id).build();
        Result<Void, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.log("Failed to post executed: " + id);
            plugin.debug(this.getClass(), res.context());
        }
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
                return CheckResponse.empty();
            }
            return res.value();
        }).exceptionally(e -> {
            plugin.debug(getClass(), e);
            return CheckResponse.empty();
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
