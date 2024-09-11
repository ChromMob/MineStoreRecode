package me.chrommob.minestore.common.placeholder;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.placeholder.json.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PlaceHolderData {
    private DonationGoal donationGoal;
    private MineStoreCommon plugin;
    private List<LastDonator> lastDonators;
    private List<TopDonator> topDonators;
    private final URI[] apiUrls = new URI[3];

    private final Gson gson = new Gson();
    private Thread thread = null;
    
    public PlaceHolderData(MineStoreCommon plugin) {
        this.plugin = plugin;
    }

    public boolean load() {
        ConfigReader configReader = plugin.configReader();
        String finalDonationGoalUrl;
        String finalLastDonatorsUrl;
        String finalTopDonatorsUrl;
        String storeUrl = (String) configReader.get(ConfigKey.STORE_URL);
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        finalDonationGoalUrl = storeUrl + "/api/"
                + ((boolean) configReader.get(ConfigKey.API_ENABLED)
                        ? configReader.getEncodedApiKey() + "/donation_goal"
                        : "donation_goal");
        finalLastDonatorsUrl = storeUrl + "/api/"
                + ((boolean) configReader.get(ConfigKey.API_ENABLED)
                        ? configReader.getEncodedApiKey() + "/getTotalPayments"
                        : "getTotalPayments");
        finalTopDonatorsUrl = storeUrl + "/api/"
                + ((boolean) configReader.get(ConfigKey.API_ENABLED)
                        ? configReader.getEncodedApiKey() + "/top_donators"
                        : "top_donators");
        try {
            URI donationGoalUrl = new URI(finalDonationGoalUrl);
            URI lastDonatorsUrl = new URI(finalLastDonatorsUrl);
            URI topDonatorsUrl = new URI(finalTopDonatorsUrl);
            apiUrls[0] = donationGoalUrl;
            apiUrls[1] = lastDonatorsUrl;
            apiUrls[2] = topDonatorsUrl;
        } catch (Exception e) {
            plugin.debug(e);
            plugin.log("STORE URL has invalid format!");
            return false;
        }
        try {
            plugin.debug("Loading placeholder data...");
            for (URI apiUrl : apiUrls) {
                URL url = apiUrl.toURL();
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

                String line;

                while ((line = reader.readLine()) != null) {
                    plugin.debug("Received: " + line);
                    if (apiUrl.equals(apiUrls[0])) {
                        try {
                            if (!MineStoreCommon.version().requires("3.0.0")) {
                                DonationGoalJsonOld donationGoalJsonOld = gson.fromJson(line, DonationGoalJsonOld.class);
                                donationGoal = donationGoalJsonOld.getDonationGoal();
                            } else {
                                Type listType = new TypeToken<List<DonationGoalJson>>() {
                                }.getType();
                                List<DonationGoalJson> donationGoals = gson.fromJson(line, listType);
                                if (!donationGoals.isEmpty()) {
                                    donationGoal = donationGoals.get(0).getDonationGoal();
                                }
                            }
                        } catch (JsonSyntaxException e) {
                            plugin.debug(e);
                            donationGoal = new DonationGoal(0, 0);
                        }
                    } else if (apiUrl.equals(apiUrls[1])) {
                        Type listType = new TypeToken<List<LastDonator>>() {
                        }.getType();
                        try {
                            lastDonators = gson.fromJson(line, listType);
                        } catch (JsonSyntaxException e) {
                            plugin.debug(e);
                            lastDonators = new ArrayList<>();
                        }
                    } else if (apiUrl.equals(apiUrls[2])) {
                        Type listType = new TypeToken<List<TopDonator>>() {
                        }.getType();
                        try {
                            topDonators = gson.fromJson(line, listType);
                        } catch (JsonSyntaxException e) {
                            plugin.debug(e);
                            topDonators = new ArrayList<>();
                        }
                    }
                }
            }
        } catch (IOException e) {
            plugin.debug(e);
            plugin.log("API KEY is invalid!");
            return false;
        } catch (ClassCastException e) {
            plugin.debug(e);
            plugin.log("STORE URL has to start with https://");
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

    private final Runnable runnable = () -> {
        while (true) {
            if (!load()) {
                plugin.debug("Failed to load placeholder data!");
            }
            try {
                Thread.sleep(1000 * 60);
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
