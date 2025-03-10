package me.chrommob.minestore.common.placeholder;

import me.chrommob.minestore.api.WebApiAccessor;
import me.chrommob.minestore.api.profile.ProfileManager;
import net.kyori.adventure.text.Component;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.api.placeholder.PlaceHolderManager;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.placeholder.json.*;
import me.chrommob.minestore.common.verification.VerificationResult;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class PlaceHolderData {
    private DonationGoal donationGoal = new DonationGoal(0, 0);
    private MineStoreCommon plugin;
    private List<LastDonator> lastDonators = new ArrayList<>();
    private List<TopDonator> topDonators = new ArrayList<>();

    private final URI[] apiUrls = new URI[3];

    private final Gson gson = new Gson();
    private Thread thread = null;
    
    public PlaceHolderData(MineStoreCommon plugin) {
        this.plugin = plugin;
        registerNativePlaceholders();
    }

    private TopDonator getTopDonator(int index) {
        if (topDonators.size() > index) {
            return topDonators.get(index);
        }
        return TopDonator.getDefault();
    }

    private LastDonator getLastDonator(int index) {
        if (lastDonators.size() > index) {
            return lastDonators.get(index);
        }
        return LastDonator.getDefault();
    }

    private void registerNativePlaceholders() {
        //Regex that matches top_donator_username_1, top_donator_username_2, etc.
        PlaceHolderManager.getInstance().registerPlaceHolder("top_donator_username_(\\d+)", (name, param) -> {
            param = param.replaceFirst("top_donator_username_", "");
            int arg = Integer.parseInt(param);
            return getTopDonator(arg - 1).getUserName();
        });
        //Regex that matches top_donator_price_1, top_donator_price_2, etc.
        PlaceHolderManager.getInstance().registerPlaceHolder("top_donator_price_(\\d+)", (name, param) -> {
            param = param.replaceFirst("top_donator_price_", "");
            int arg = Integer.parseInt(param);
            return String.valueOf(getTopDonator(arg - 1).getPrice());
        });
        //Regex that matches last_donator_username_1, last_donator_username_2, etc.
        PlaceHolderManager.getInstance().registerPlaceHolder("last_donator_username_(\\d+)", (name, param) -> {
            param = param.replaceFirst("last_donator_username_", "");
            int arg = Integer.parseInt(param);
            return getLastDonator(arg - 1).getUserName();
        });
        //Regex that matches last_donator_price_1, last_donator_price_2, etc.
        PlaceHolderManager.getInstance().registerPlaceHolder("last_donator_price_(\\d+)", (name, param) -> {
            param = param.replaceFirst("last_donator_price_", "");
            int arg = Integer.parseInt(param);
            return String.valueOf(getLastDonator(arg - 1).getPrice());
        });
        //Regex that matches last_donator_package_1, last_donator_package_2, etc.
        PlaceHolderManager.getInstance().registerPlaceHolder("last_donator_package_(\\d+)", (name, param) -> {
            param = param.replaceFirst("last_donator_package_", "");
            int arg = Integer.parseInt(param);
            return getLastDonator(arg - 1).getPackageName();
        });
        PlaceHolderManager.getInstance().registerPlaceHolder("donation_goal_current", (name, param) -> String.valueOf(donationGoal.getDonationGoalCurrentAmount()));
        PlaceHolderManager.getInstance().registerPlaceHolder("donation_goal_target", (name, param) -> String.valueOf(donationGoal.getDonationGoalAmount()));
        PlaceHolderManager.getInstance().registerPlaceHolder("donation_goal_percentage", (name, param) -> String.valueOf(donationGoal.getDonationGoalPercentage()));
        PlaceHolderManager.getInstance().registerPlaceHolder("donation_goal_bar_(\\d+)", (name, param) -> {
            param = param.replaceFirst("donation_goal_bar_", "");
            int amount = Integer.parseInt(param);
            Component component = Component.text("");
            if (donationGoal.getDonationGoalPercentage() > 100) {
                for (int i = 0; i < amount; i++) {
                    component = component.append(Component.text("█").color(NamedTextColor.GREEN));
                }
                LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
                return serializer.serialize(component);
            }
            if (donationGoal.getDonationGoalPercentage() < 0) {
                for (int i = 0; i < amount; i++) {
                    component = component.append(Component.text("█").color(NamedTextColor.RED));
                }
                LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
                return serializer.serialize(component);
            }
            int step = 100 / amount;
            for (int i = 0; i < amount; i++) {
                if ((i + 1) * step <= donationGoal.getDonationGoalPercentage()) {
                    component = component.append(Component.text("█").color(NamedTextColor.GREEN));
                    continue;
                }
                component = component.append(Component.text("█").color(NamedTextColor.RED));
            }
            LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
            return serializer.serialize(component);
        });
        //Regex that matches player_spent
        PlaceHolderManager.getInstance().registerPlaceHolder("player_spent", (name, param) -> {
            ProfileManager.Profile profile = WebApiAccessor.profileManager().getCachedProfile(name);
            if (profile == null) {
                return "";
            }
            return String.valueOf(profile.moneySpent());
        });
    }

    public VerificationResult load() {
        List<String> errors = new ArrayList<>();
        String finalDonationGoalUrl;
        String finalLastDonatorsUrl;
        String finalTopDonatorsUrl;
        String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        finalDonationGoalUrl = storeUrl + "/api/"
                + (plugin.pluginConfig().getKey("api").getKey("key-enabled").getAsBoolean()
                        ? plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/donation_goal"
                        : "donation_goal");
        finalLastDonatorsUrl = storeUrl + "/api/"
                + (plugin.pluginConfig().getKey("api").getKey("key-enabled").getAsBoolean()
                        ? plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/getTotalPayments"
                        : "getTotalPayments");
        finalTopDonatorsUrl = storeUrl + "/api/"
                + (plugin.pluginConfig().getKey("api").getKey("key-enabled").getAsBoolean()
                        ? plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/top_donators"
                        : "top_donators");
        try {
            URI donationGoalUrl = new URI(finalDonationGoalUrl);
            URI lastDonatorsUrl = new URI(finalLastDonatorsUrl);
            URI topDonatorsUrl = new URI(finalTopDonatorsUrl);
            apiUrls[0] = donationGoalUrl;
            apiUrls[1] = lastDonatorsUrl;
            apiUrls[2] = topDonatorsUrl;
        } catch (Exception e) {
            plugin.debug(this.getClass(), e);
            errors.add("STORE URL has invalid format!");
            return new VerificationResult(false, errors, VerificationResult.TYPE.STORE_URL);
        }
        try {
            plugin.debug(this.getClass(), "Loading placeholder data...");
            for (URI apiUrl : apiUrls) {
                URL url = apiUrl.toURL();
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                if ("gzip".equals(urlConnection.getContentEncoding())) {
                    in = new GZIPInputStream(in);
                }

                BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                plugin.debug(this.getClass(), "Received: " + response);
                if (apiUrl.equals(apiUrls[0])) {
                    try {
                        if (!MineStoreCommon.version().requires("3.0.0")) {
                            DonationGoalJsonOld donationGoalJsonOld = gson.fromJson(response.toString(), DonationGoalJsonOld.class);
                            donationGoal = donationGoalJsonOld.getDonationGoal();
                        } else {
                            Type listType = new TypeToken<List<DonationGoalJson>>() {
                            }.getType();
                            List<DonationGoalJson> donationGoals = gson.fromJson(response.toString(), listType);
                            if (!donationGoals.isEmpty()) {
                                donationGoal = donationGoals.get(0).getDonationGoal();
                            } else {
                                donationGoal = new DonationGoal(0, 0);
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        plugin.debug(this.getClass(), e);
                        donationGoal = new DonationGoal(0, 0);
                    }
                } else if (apiUrl.equals(apiUrls[1])) {
                    Type listType = new TypeToken<List<LastDonator>>() {
                    }.getType();
                    try {
                        lastDonators = gson.fromJson(response.toString(), listType);
                    } catch (JsonSyntaxException e) {
                        plugin.debug(this.getClass(), e);
                        lastDonators = new ArrayList<>();
                    }
                } else if (apiUrl.equals(apiUrls[2])) {
                    Type listType = new TypeToken<List<TopDonator>>() {
                    }.getType();
                    try {
                        topDonators = gson.fromJson(response.toString(), listType);
                    } catch (JsonSyntaxException e) {
                        plugin.debug(this.getClass(), e);
                        topDonators = new ArrayList<>();
                    }
                }
            }
        } catch (IOException e) {
            plugin.debug(this.getClass(), e);
            errors.add("API KEY is invalid!");
            return new VerificationResult(false, errors, VerificationResult.TYPE.API_KEY);
        } catch (ClassCastException e) {
            plugin.debug(this.getClass(), e);
            errors.add("STORE URL has to start with https://");
            return new VerificationResult(false, errors, VerificationResult.TYPE.STORE_URL);
        }
        return VerificationResult.valid();
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
            if (!load().isValid()) {
                plugin.debug(this.getClass(), "Failed to load placeholder data!");
                plugin.handleError();
            } else {
                plugin.notError();
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
}
