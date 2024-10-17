package me.chrommob.minestore.common.commandGetters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.addons.api.event.types.MineStorePurchaseEvent;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.GsonReponse;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.interfaces.commands.CommandGetter;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class WebListener implements CommandGetter {
    private final MineStoreCommon plugin;
    private final ConfigReader configReader;
    private final Gson gson = new Gson();
    private boolean wasEmpty = false;
    private URL queueUrl;
    private URL executedUrl;
    private URL deliveredUrl;
    private Thread thread = null;
    private final Set<String> toPostExecuted = new HashSet<>();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (wasEmpty) {
                    try {
                        Thread.sleep(9500);
                        wasEmpty = false;
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
                if (!toPostExecuted.isEmpty()) {
                    Set<String> toPostExecutedCopy = new HashSet<>(toPostExecuted);
                    toPostExecuted.clear();
                    for (String id : toPostExecutedCopy) {
                        postExecutedAsync(id);
                    }
                }
                plugin.debug("[WebListener] Running...");
                try {
                    ParsedResponse parsedResponse = null;
                    GsonReponse response;
                    HttpsURLConnection urlConnection = (HttpsURLConnection) queueUrl.openConnection();
                    InputStream in = urlConnection.getInputStream();

                    BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            response = gson.fromJson(line, GsonReponse.class);
                            parsedResponse = parseGson(response);
                        } catch (JsonSyntaxException e) {
                            wasEmpty = true;
                        }
                    }
                    if (wasEmpty || parsedResponse == null || parsedResponse.username() == null) {
                        plugin.debug("Got empty response from server");
                        wasEmpty = true;
                    }
                    reader.close();
                    in.close();
                    urlConnection.disconnect();
                    if (!wasEmpty) {
                        switch (parsedResponse.type()) {
                            case COMMAND:
                                MineStorePurchaseEvent event = new MineStorePurchaseEvent(parsedResponse.username(), parsedResponse.command(), parsedResponse.commandId(), parsedResponse.commandType() == ParsedResponse.COMMAND_TYPE.ONLINE ? MineStorePurchaseEvent.COMMAND_TYPE.ONLINE : MineStorePurchaseEvent.COMMAND_TYPE.OFFLINE);
                                event.call();
                                if (event.isCancelled()) {
                                    return;
                                }
                                ParsedResponse.COMMAND_TYPE commandType = event.commandType() == MineStorePurchaseEvent.COMMAND_TYPE.ONLINE ? ParsedResponse.COMMAND_TYPE.ONLINE : ParsedResponse.COMMAND_TYPE.OFFLINE;
                                plugin.commandStorage().listener(new ParsedResponse(ParsedResponse.TYPE.COMMAND, commandType, event.command(), event.username(), event.id()));
                                plugin.debug("Got command: " + "\"" + parsedResponse.command() + "\""
                                        + " with id: " + parsedResponse.commandId() + " for player: "
                                        + parsedResponse.username() + " requires online: "
                                        + (parsedResponse.commandType().equals(ParsedResponse.COMMAND_TYPE.ONLINE)
                                                ? "true"
                                                : "false"));
                                break;
                            case AUTH:
                                plugin.debug("Got auth for player: " + parsedResponse.username() + " with id: "
                                        + parsedResponse.authId());
                                plugin.authHolder().listener(parsedResponse);
                                break;
                        }
                        if (MineStoreCommon.version().requires("3.0.0")) {
                            postDelivered(String.valueOf(parsedResponse.commandId()));
                            if (parsedResponse.commandType() == ParsedResponse.COMMAND_TYPE.OFFLINE) {
                                postExecuted(String.valueOf(parsedResponse.commandId()));
                            }
                        } else {
                            postExecuted(String.valueOf(parsedResponse.commandId()));
                        }
                    }
                } catch (IOException e) {
                    plugin.debug(e);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };

    public WebListener(MineStoreCommon plugin) {
        this.plugin = plugin;
        configReader = plugin.configReader();
    }

    @Override
    public boolean load() {
        String finalQueueUrl;
        String finalExecutedUrl;
        String finalDeliveredUrl;
        String storeUrl = (String) configReader.get(ConfigKey.STORE_URL);
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        if ((boolean) configReader.get(ConfigKey.SECRET_ENABLED)) {
            finalQueueUrl = storeUrl + "/api/servers/" + configReader.get(ConfigKey.SECRET_KEY) + "/commands/queue";
            finalExecutedUrl = storeUrl + "/api/servers/" + configReader.get(ConfigKey.SECRET_KEY)
                    + "/commands/executed/";
            finalDeliveredUrl = storeUrl + "/api/servers/" + configReader.get(ConfigKey.SECRET_KEY)
                    + "/commands/delivered/";
        } else {
            finalQueueUrl = storeUrl + "/api/servers/commands/queue";
            finalExecutedUrl = storeUrl + "/api/servers/commands/executed/";
            finalDeliveredUrl = storeUrl + "/api/servers/commands/delivered/";
        }
        try {
            queueUrl = new URL(finalQueueUrl);
            executedUrl = new URL(finalExecutedUrl);
            deliveredUrl = new URL(finalDeliveredUrl);
        } catch (Exception e) {
            plugin.log("Store URL is not a valid URL!");
            plugin.debug(e);
            return false;
        }
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) queueUrl.openConnection();
            InputStream in = urlConnection.getInputStream();

            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    gson.fromJson(line, GsonReponse.class);
                } catch (JsonSyntaxException e) {
                    if (line.equals("{}")) {
                        wasEmpty = true;
                    } else {
                        plugin.debug(e);
                        plugin.debug(e);
                        plugin.log("SECRET KEY is invalid!");
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            plugin.log("SECRET KEY is invalid!");
            plugin.debug(e);
            return false;
        } catch (ClassCastException e) {
            plugin.log("STORE URL has to start with https://");
            plugin.debug(e);
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void postDelivered(String id) {
        try {
            URL url = new URL(deliveredUrl + id);
            plugin.debug("Posting to: " + url);
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
            plugin.debug(e);
        }
    }

    public void postExecuted(String id) {
        toPostExecuted.add(id);
    }

    public void postExecutedAsync(String id) {
        try {
            URL url = new URL(executedUrl + id);
            plugin.debug("Posting to: " + url);
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
            plugin.debug(e);
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
}
