package me.chrommob.minestore.common.commandGetters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.GsonReponse;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.templates.CommandGetter;
import me.chrommob.minestore.common.templates.ConfigReaderCommon;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WebListener implements CommandGetter {
    private boolean running;
    private final MineStoreCommon mineStoreCommon;
    private final ConfigReaderCommon configReader;
    private boolean wasEmpty = false;
    private final Gson gson = new Gson();
    public WebListener(MineStoreCommon mineStoreCommon) {
        running = false;
        this.mineStoreCommon = mineStoreCommon;
        configReader = mineStoreCommon.configReader();
    }

    private URL url;
    @Override
    public boolean load() {
        running = false;
        String finalUrl;
        String storeUrl = configReader.storeUrl();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        if (configReader.secretEnabled()) {
            finalUrl = storeUrl + "/servers/" + configReader.secretKey() + "/commands/queue";
        } else {
            finalUrl = storeUrl + "/servers/commands/queue";
        }
        try {
            url = new URL(finalUrl);
        } catch (Exception e) {
            mineStoreCommon.log("Store URL is not a URL!");
            e.printStackTrace();
            return false;
        }
        running = true;
        return true;
    }

    @Override
    public void start() {
        new Thread(runnable).start();
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (!running) {
                    return;
                }
                try {
                    GsonReponse response;
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();

                    BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            response = gson.fromJson(line, GsonReponse.class);
                            ParsedResponse parsedResponse = parseGson(response);
                            mineStoreCommon.commandStorage().listener(parsedResponse);
                        } catch (JsonSyntaxException e) {
                            wasEmpty = true;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

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
            return new ParsedResponse(type, commandType, response.command(), response.username(), response.commandId());
        } else {
            return new ParsedResponse(type, response.username(), response.authId());
        }
    }
}
