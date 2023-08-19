package me.chrommob.minestore.common.gui.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.gui.GuiOpenener;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.parsed.ParsedGui;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

public class GuiData {
    private List<Category> parsedResponse;
    private URL packageURL;
    private Gson gson = new Gson();

    private GuiOpenener guiOpenener = new GuiOpenener(this);
    private ParsedGui parsedGui;
    private Thread thread = null;

    public boolean load() {
        ConfigReader configReader = MineStoreCommon.getInstance().configReader();
        String finalUrl;
        String storeUrl = (String) configReader.get(ConfigKey.STORE_URL);
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        finalUrl = storeUrl + "/api/"
                + ((boolean) configReader.get(ConfigKey.API_ENABLED)
                        ? configReader.get(ConfigKey.API_KEY) + "/gui/packages_new"
                        : "gui/packages_new");
        try {
            packageURL = new URL(finalUrl);
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
            MineStoreCommon.getInstance().log("STORE URL format is invalid!");
            return false;
        }
        try {
            MineStoreCommon.getInstance().debug("[GuiData] Loading data from " + finalUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection) packageURL.openConnection();
            InputStream in = urlConnection.getInputStream();

            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Type listType = new TypeToken<List<Category>>() {
                    }.getType();
                    parsedResponse = gson.fromJson(line, listType);
                } catch (JsonSyntaxException e) {
                    MineStoreCommon.getInstance().debug(e);
                    MineStoreCommon.getInstance().log("API key is invalid!");
                    parsedResponse = null;
                    return false;
                }
            }
        } catch (ClassCastException e) {
            MineStoreCommon.getInstance().log("STORE URL has to start with https://");
            MineStoreCommon.getInstance().debug(e);
            return false;
        } catch (IOException e) {
            MineStoreCommon.getInstance().debug(e);
            MineStoreCommon.getInstance().log("API key is invalid!");
            return false;
        }
        if (parsedResponse == null) {
            MineStoreCommon.getInstance().log("API key is invalid!");
            return false;
        }
        parsedGui = new ParsedGui(parsedResponse);
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
                MineStoreCommon.getInstance().debug("[GuiData] Error loading data!");
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
}
