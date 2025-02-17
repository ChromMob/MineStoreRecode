package me.chrommob.minestore.common.gui.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.GuiOpenener;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedGui;
import me.chrommob.minestore.common.verification.VerificationResult;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GuiData {
    private MineStoreCommon plugin;
    public GuiData(MineStoreCommon plugin) {
        this.plugin = plugin;
        guiOpenener = new GuiOpenener(this);
    }
    private List<?> parsedResponse;
    private final Gson gson = new Gson();

    private final GuiOpenener guiOpenener;
    private ParsedGui parsedGui;
    private Thread thread = null;

    public VerificationResult load() {
        List<String> messages = new ArrayList<>();
        String finalUrl;
        String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        finalUrl = storeUrl + "/api/"
                + (plugin.pluginConfig().getKey("api").getKey("key-enabled").getAsBoolean()
                        ? plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/gui/packages_new"
                        : "gui/packages_new");
        URL packageURL;
        try {
            packageURL = new URL(finalUrl);
        } catch (Exception e) {
            plugin.debug(this.getClass(), e);
            messages.add("Store URL: " + finalUrl);
            messages.add("STORE URL format is invalid!");
            return new VerificationResult(false, messages, VerificationResult.TYPE.STORE_URL);
        }
        try {
            plugin.debug(this.getClass(), "[GuiData] Loading data from " + finalUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection) packageURL.openConnection();
            InputStream in = urlConnection.getInputStream();

            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

            String line;

            if (urlConnection.getResponseCode() != 200) {
                messages.add("The request was denied by the server with code: " + urlConnection.getResponseCode() + "!");
                switch (urlConnection.getResponseCode()) {
                    case 403:
                        messages.add("Probably Cloudflare protection.");
                        break;
                    case 404:
                        messages.add("The server returned a 404 error.");
                        break;
                    default:
                        messages.add("The server returned an error with code: " + urlConnection.getResponseCode() + "!");
                        break;
                }
                return new VerificationResult(false, messages, VerificationResult.TYPE.WEBSTORE);
            }

            while ((line = reader.readLine()) != null) {
                try {
                    Type listType;
                    if (MineStoreCommon.version().requires(3,0,0)) {
                        listType = new TypeToken<List<NewCategory>>() {
                        }.getType();
                    } else {
                        listType = new TypeToken<List<Category>>() {
                        }.getType();
                    }
                    if (line.equals("[]")) {
                        parsedResponse = new ArrayList<>();
                        return VerificationResult.valid();
                    }
                    parsedResponse = gson.fromJson(line, listType);
                } catch (JsonSyntaxException e) {
                    plugin.debug(this.getClass(), e);
                    messages.add("API key is invalid!");
                    parsedResponse = null;
                    return new VerificationResult(false, messages, VerificationResult.TYPE.API_KEY);
                }
            }
        } catch (ClassCastException e) {
            messages.add("STORE URL has to start with https://");
            plugin.debug(this.getClass(), e);
            return new VerificationResult(false, messages, VerificationResult.TYPE.STORE_URL);
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
            messages.add("API key is invalid!");
            return new VerificationResult(false, messages, VerificationResult.TYPE.API_KEY);
        }
        if (parsedResponse == null) {
            messages.add("Parsed response is null!");
            messages.add("API key is invalid!");
            return new VerificationResult(false, messages, VerificationResult.TYPE.API_KEY);
        }
        parsedGui = new ParsedGui(parsedResponse, plugin);
        return VerificationResult.valid();
    }

    public void start() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        thread = new Thread(runnable);
        thread.start();
    }

    public void stop() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    private Runnable runnable = () -> {
        while (true) {
            if (!load().isValid()) {
                plugin.debug(this.getClass(), "[GuiData] Error loading data!");
            }
            try {
                Thread.sleep(1000 * 60 * 5);
            } catch (InterruptedException e) {
                break;
            }
        }
    };

    public ParsedGui getParsedGui() {
        return parsedGui;
    }

    public GuiOpenener getGuiInfo() {
        return guiOpenener;
    }

    public MineStoreCommon getPlugin() {
        return plugin;
    }
}
