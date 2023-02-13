package me.chrommob.minestore.common.gui;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.JsonRoot;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;

public class GuiData {
    private JsonRoot jsonRoot;
    private URL packageURL;
    private Gson gson = new Gson();

    private GuiInfo guiInfo = new GuiInfo();

    public boolean load() {
        boolean error = false;
        ConfigReader configReader = MineStoreCommon.getInstance().configReader();
        String finalUrl;
        String storeUrl = (String) configReader.get(ConfigKey.STORE_URL);
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        if ((boolean) configReader.get(ConfigKey.SECRET_ENABLED)) {
            finalUrl = storeUrl + "/api/servers/" + configReader.get(ConfigKey.SECRET_KEY) + "/gui/packages_new";
        } else {
            finalUrl = storeUrl + "/api/servers/gui/packages_new";
        }
        try {
            packageURL = new URL(finalUrl);
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
            return false;
        }
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) packageURL.openConnection();
            InputStream in = urlConnection.getInputStream();

            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    jsonRoot = gson.fromJson(line, JsonRoot.class);
                } catch (JsonSyntaxException e) {
                    MineStoreCommon.getInstance().debug(e);
                    error = true;
                }
            }
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
            return false;
        }
        if (error) {
            jsonRoot = null;
            return false;
        }
        return true;
    }

    public void start() {
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (!load()) {
                    MineStoreCommon.getInstance().debug("[GuiData] Error loading data!");
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    MineStoreCommon.getInstance().debug(e);
                }
            }
        }
    };

    public JsonRoot jsonRoot() {
        return jsonRoot;
    }
}
