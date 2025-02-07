package me.chrommob.minestore.common.subsription;

import com.google.gson.Gson;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.subsription.json.ReturnSubscriptionObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class SubscriptionUtil {
    private static URL url;
    public static ReturnSubscriptionObject getSubscription(String username) {
        if (url == null) {
            return null;
        }
        String body = "username=" + username;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(body.length()));
            connection.setDoOutput(true);
            connection.getOutputStream().write(body.getBytes());
            connection.getOutputStream().close();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return new Gson().fromJson(response.toString(), ReturnSubscriptionObject.class);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void init(MineStoreCommon plugin) {
        String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        String url = storeUrl + "/api/"
                + (plugin.pluginConfig().getKey("api").getKey("key-enabled").getAsBoolean()
                ? plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/in-game/manageSubscriptions/"
                : "/in-game/manageSubscriptions/");
        try {
            SubscriptionUtil.url = new URL(url);
        } catch (MalformedURLException ignored) {
        }
    }
}
