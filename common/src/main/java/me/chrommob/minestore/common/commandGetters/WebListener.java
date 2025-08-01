package me.chrommob.minestore.common.commandGetters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.leangen.geantyref.TypeToken;
import me.chrommob.minestore.api.event.MineStoreEvent;
import me.chrommob.minestore.api.event.types.MineStorePurchaseEvent;
import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.GsonReponse;
import me.chrommob.minestore.common.commandGetters.dataTypes.PostResponse;
import me.chrommob.minestore.common.commandHolder.type.CheckResponse;
import me.chrommob.minestore.common.gui.payment.PaymentCreationResponse;
import me.chrommob.minestore.common.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.common.scheduler.SafeScheduledTask;
import me.chrommob.minestore.common.verification.VerificationResult;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebListener {
    private final MineStoreVersion arraySupportedSince = new MineStoreVersion(3, 2, 5);
    private final MineStoreCommon plugin;
    private final Gson gson = new Gson();
    private boolean wasEmpty = false;
    private URL queueUrl;
    private URL executedUrl;
    private URL deliveredUrl;
    private URL checkUrl;
    private URL paymentUrl;
    private final Set<String> toPostExecuted = new HashSet<>();
    public final MineStoreScheduledTask mineStoreScheduledTask;

    private List<ParsedResponse> fetchData() {
        List<ParsedResponse> parsedResponses = new ArrayList<>();
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) queueUrl.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }
            plugin.debug(this.getClass(), "Received: " + responseString);
            if (responseString.toString().equals("{}")) {
                return new ArrayList<>();
            }
            try {
                if (MineStoreCommon.version().requires(arraySupportedSince)) {
                    Type listType = new TypeToken<List<GsonReponse>>(){}.getType();
                    List<GsonReponse> list = gson.fromJson(responseString.toString(), listType);
                    for (GsonReponse response : list) {
                        parsedResponses.add(parseGson(response));
                    }
                } else {
                    GsonReponse response = gson.fromJson(responseString.toString(), GsonReponse.class);
                    if (response != null && response.username() != null) {
                        ParsedResponse parsedResponse = parseGson(response);
                        parsedResponses.add(parsedResponse);
                    }
                }
            } catch (JsonSyntaxException e) {
                plugin.debug(this.getClass(), e);
                wasEmpty = true;
            }
            plugin.notError();
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
            plugin.handleError();
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
        mineStoreScheduledTask = SafeScheduledTask.wrap(this.getClass(), "weblistener", plugin, () -> {
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
        String finalQueueUrl;
        String finalExecutedUrl;
        String finalDeliveredUrl;
        String finalCheckUrl;
        String finalPaymentUrl;
        String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        if (plugin.pluginConfig().getKey("weblistener").getKey("secret-enabled").getAsBoolean()) {
            finalQueueUrl = storeUrl + "/api/servers/" + plugin.pluginConfig().getKey("weblistener").getKey("secret-key").getAsString() + "/commands/queue";
            finalExecutedUrl = storeUrl + "/api/servers/" + plugin.pluginConfig().getKey("weblistener").getKey("secret-key").getAsString()
                    + "/commands/executed/";
            finalDeliveredUrl = storeUrl + "/api/servers/" + plugin.pluginConfig().getKey("weblistener").getKey("secret-key").getAsString()
                    + "/commands/delivered/";
            finalCheckUrl = storeUrl + "/api/servers/" + plugin.pluginConfig().getKey("weblistener").getKey("secret-key").getAsString() + "/commands/validated/";
            finalPaymentUrl = storeUrl + "/api/rest/v2/" + plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/payment/create";
        } else {
            finalQueueUrl = storeUrl + "/api/servers/commands/queue";
            finalExecutedUrl = storeUrl + "/api/servers/commands/executed/";
            finalDeliveredUrl = storeUrl + "/api/servers/commands/delivered/";
            finalCheckUrl = storeUrl + "/api/servers/commands/validated/";
            finalPaymentUrl = storeUrl + "/api/rest/v2/payment/create";
        }
        try {
            queueUrl = new URL(finalQueueUrl);
            executedUrl = new URL(finalExecutedUrl);
            deliveredUrl = new URL(finalDeliveredUrl);
            checkUrl = new URL(finalCheckUrl);
            paymentUrl = new URL(finalPaymentUrl);
        } catch (Exception e) {
            plugin.debug(this.getClass(), e);
            return new VerificationResult(false, Collections.singletonList("Store URL is not a valid URL!"), VerificationResult.TYPE.STORE_URL);
        }
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) queueUrl.openConnection();
            InputStream in = urlConnection.getInputStream();

            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }
            try {
                if (MineStoreCommon.version().requires(arraySupportedSince)) {
                    Type listType = new TypeToken<List<GsonReponse>>(){}.getType();
                    List<GsonReponse> list = gson.fromJson(responseString.toString(), listType);
                    if (list.isEmpty()) {
                        wasEmpty = true;
                    }
                    return VerificationResult.valid();
                }
                gson.fromJson(responseString.toString(), GsonReponse.class);
            } catch (JsonSyntaxException e) {
                if (responseString.toString().equals("{}")) {
                    wasEmpty = true;
                } else {
                    plugin.debug(this.getClass(), e);
                    List<String> messages = new ArrayList<>();
                    messages.add(e.getMessage());
                    messages.add("SECRET KEY: " + plugin.pluginConfig().getKey("weblistener").getKey("secret-key").getAsString());
                    return new VerificationResult(false, messages, VerificationResult.TYPE.SECRET_KEY);
                }
            }
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
            List<String> messages = new ArrayList<>();
            messages.add(e.getMessage());
            messages.add("SECRET KEY: " + plugin.pluginConfig().getKey("weblistener").getKey("secret-key").getAsString());
            return new VerificationResult(false, messages, VerificationResult.TYPE.SECRET_KEY);
        } catch (ClassCastException e) {
            plugin.log("STORE URL has to start with https://");
            plugin.debug(this.getClass(), e);
            return new VerificationResult(false, Collections.singletonList("STORE URL has to start with https://"), VerificationResult.TYPE.STORE_URL);
        }
        return VerificationResult.valid();
    }

    private void postDelivered(String id) {
        try {
            URL url = new URL(deliveredUrl + id);
            plugin.debug(this.getClass(), "Posting to: " + url);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            try (final OutputStream os = urlConnection.getOutputStream()) {
                os.write(id.getBytes());
            }
            urlConnection.getInputStream();
            urlConnection.disconnect();
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
        }
    }

    private void postDelivered(int[] ids) {
        if (ids.length == 0) {
            return;
        }
        try {
            plugin.debug(this.getClass(), "Posting to: " + deliveredUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection) deliveredUrl.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            JsonObject jsonObject = new JsonObject();
            JsonArray idsArrayJson = new JsonArray();
            for (int id : ids) {
                idsArrayJson.add(id);
            }
            jsonObject.add("ids", idsArrayJson);
            String json = jsonObject.toString();
            try (final OutputStream os = urlConnection.getOutputStream()) {
                os.write(json.getBytes());
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }
            urlConnection.disconnect();
            PostResponse postResponse = gson.fromJson(responseString.toString(), PostResponse.class);
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
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
        }
    }

    public void postExecuted(String id) {
        toPostExecuted.add(id);
    }

    private void postExecutedAsync(Set<String> ids) {
        try {
            plugin.debug(this.getClass(), "Posting to: " + executedUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection) executedUrl.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
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
            try (final OutputStream os = urlConnection.getOutputStream()) {
                os.write(json.getBytes());
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }
            urlConnection.disconnect();
            PostResponse postResponse = gson.fromJson(responseString.toString(), PostResponse.class);
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
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
        }
    }

    private void postExecutedAsync(String id) {
        try {
            URL url = new URL(executedUrl + id);
            plugin.debug(this.getClass(), "Posting to: " + url);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            try (final OutputStream os = urlConnection.getOutputStream()) {
                os.write(id.getBytes());
            }
            urlConnection.getInputStream();
            urlConnection.disconnect();
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
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
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) checkUrl.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.connect();
                OutputStream os = urlConnection.getOutputStream();
                os.write(json.getBytes());
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder responseString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseString.append(line);
                }
                urlConnection.disconnect();
                plugin.debug(this.getClass(), "Received: " + responseString);
                return gson.fromJson(responseString.toString(), CheckResponse.class);
            } catch (IOException e) {
                plugin.debug(this.getClass(), e);
                return CheckResponse.empty();
            }
        }).exceptionally(e -> {
            plugin.debug(getClass(), e);
            return CheckResponse.empty();
        });
    }

    //https://v3.minestorecms.com/api/rest/v2/payment/create?username=superuser123456&item_id=1&virtual_currency=true
    public CompletableFuture<PaymentCreationResponse> createPayment(String username, int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("username", username);
            jsonObject.addProperty("item_id", itemId);
            jsonObject.addProperty("virtual_currency", true);
            String json = jsonObject.toString();
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) paymentUrl.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.connect();
                OutputStream os = urlConnection.getOutputStream();
                os.write(json.getBytes());
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder responseString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseString.append(line);
                }
                urlConnection.disconnect();
                plugin.debug(this.getClass(), "Received: " + responseString);
                return gson.fromJson(responseString.toString(), PaymentCreationResponse.class);
            } catch (IOException e) {
                plugin.debug(this.getClass(), e);
                return null;
            }
        });
    }
}
