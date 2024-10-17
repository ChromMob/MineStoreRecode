package me.chrommob.minestore.common.gui.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.gui.GuiOpenener;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedGui;

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
    private List parsedResponse;
    private URL packageURL;
    private final Gson gson = new Gson();

    private final GuiOpenener guiOpenener;
    private ParsedGui parsedGui;
    private Thread thread = null;

    public boolean load() {
        ConfigReader configReader = plugin.configReader();
        String finalUrl;
        String storeUrl = (String) configReader.get(ConfigKey.STORE_URL);
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        finalUrl = storeUrl + "/api/"
                + ((boolean) configReader.get(ConfigKey.API_ENABLED)
                        ? configReader.getEncodedApiKey() + "/gui/packages_new"
                        : "gui/packages_new");
        try {
            packageURL = new URL(finalUrl);
        } catch (Exception e) {
            plugin.debug(e);
            plugin.log("STORE URL format is invalid!");
            return false;
        }
        try {
            plugin.debug("[GuiData] Loading data from " + finalUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection) packageURL.openConnection();
            InputStream in = urlConnection.getInputStream();

            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

            String line;

            if (urlConnection.getResponseCode() == 403) {
                plugin.log("The request was denied by the server! Probably Cloudflare protection.");
                return false;
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
                        return true;
                    }
                    parsedResponse = gson.fromJson(line, listType);
                } catch (JsonSyntaxException e) {
                    plugin.debug(e);
                    plugin.log("API key is invalid!");
                    parsedResponse = null;
                    return false;
                }
            }
        } catch (ClassCastException e) {
            plugin.log("STORE URL has to start with https://");
            plugin.debug(e);
            return false;
        } catch (IOException e) {
            plugin.debug(e);
            plugin.log("API key is invalid!");
            return false;
        }
        if (parsedResponse == null) {
            plugin.log("API key is invalid!");
            return false;
        }
        parsedGui = new ParsedGui(parsedResponse, plugin);
        return true;
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
            if (!load()) {
                plugin.debug("[GuiData] Error loading data!");
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
