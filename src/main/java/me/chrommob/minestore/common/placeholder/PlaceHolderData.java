package me.chrommob.minestore.common.placeholder;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.placeholder.json.DonationGoal;
import me.chrommob.minestore.common.placeholder.json.LastDonator;
import me.chrommob.minestore.common.placeholder.json.TopDonator;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaceHolderData {
    private DonationGoal donationGoal;
    private List<LastDonator> lastDonators;
    private List<TopDonator> topDonators;
    private Set<URL> apiUrls = new HashSet<>();

    private Gson gson = new Gson();
    private Thread thread = null;

    public boolean load() {
        ConfigReader configReader = MineStoreCommon.getInstance().configReader();
        String finalDonationGoalUrl;
        String finalLastDonatorsUrl;
        String finalTopDonatorsUrl;
        String storeUrl = (String) configReader.get(ConfigKey.STORE_URL);
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        if ((boolean) configReader.get(ConfigKey.API_ENABLED)) {
            finalDonationGoalUrl = storeUrl + "/api/" + configReader.get(ConfigKey.API_KEY) + "/donation_goal";
            finalLastDonatorsUrl = storeUrl + "/api/" + configReader.get(ConfigKey.API_KEY) + "/getTotalPayments";
            finalTopDonatorsUrl = storeUrl + "/api/" + configReader.get(ConfigKey.API_KEY) + "/top_donators";
        } else {
            finalDonationGoalUrl = storeUrl + "/api/donation_goal";
            finalLastDonatorsUrl = storeUrl + "/api/getTotalPayments";
            finalTopDonatorsUrl = storeUrl + "/api/top_donators";
        }
        try {
            URL donationGoalUrl = new URL(finalDonationGoalUrl);
            URL lastDonatorsUrl = new URL(finalLastDonatorsUrl);
            URL topDonatorsUrl = new URL(finalTopDonatorsUrl);
            apiUrls.add(donationGoalUrl);
            apiUrls.add(lastDonatorsUrl);
            apiUrls.add(topDonatorsUrl);
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
            return false;
        }
        try {
            MineStoreCommon.getInstance().debug("Loading placeholder data...");
            for (URL apiUrl : apiUrls) {
                HttpsURLConnection urlConnection = (HttpsURLConnection) apiUrl.openConnection();
                InputStream in = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

                String line;

                while ((line = reader.readLine()) != null) {
                    try {
                        if (apiUrl.equals(apiUrls.toArray()[0])) {
                            donationGoal = gson.fromJson(line, DonationGoal.class);
                        } else if (apiUrl.equals(apiUrls.toArray()[1])) {
                            Type listType = new TypeToken<List<LastDonator>>() {}.getType();
                            lastDonators = gson.fromJson(line, listType);
                        } else if (apiUrl.equals(apiUrls.toArray()[2])) {
                            Type listType = new TypeToken<List<TopDonator>>() {}.getType();
                            topDonators = gson.fromJson(line, listType);
                        }
                    } catch (JsonSyntaxException e) {
                        MineStoreCommon.getInstance().debug(e);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            MineStoreCommon.getInstance().debug(e);
            return false;
        }
        return true;
    }

    public void start() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        thread = new Thread(runnable);
        thread.start();
    }

    private Runnable runnable = () -> {
        while (true) {
            if (!load()) {
                MineStoreCommon.getInstance().debug("Failed to load placeholder data!");
            }
            try {
                Thread.sleep(1000 * 60 * 5);
            } catch (InterruptedException e) {
                break;
            }
        }
    };

    public void stop() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public DonationGoal getDonationGoal() {
        return donationGoal;
    }

    public List<LastDonator> getLastDonators() {
        return lastDonators;
    }

    public List<TopDonator> getTopDonators() {
        return topDonators;
    }
}
