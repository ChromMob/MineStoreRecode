package me.chrommob.minestore.common.placeholder;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.api.placeholder.PlaceHolderManager;
import me.chrommob.minestore.api.web.WebApiAccessor;
import me.chrommob.minestore.api.web.profile.ProfileManager;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.placeholder.json.*;
import me.chrommob.minestore.common.verification.VerificationResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

public class PlaceHolderData {
    private final MineStoreVersion pagesSupportedSince = new MineStoreVersion(3, 3, 5);
    private DonationGoal donationGoal = new DonationGoal(0, 0);
    private MineStoreCommon plugin;
    private List<TopDonator> topDonators = new ArrayList<>();
    private List<LastDonator> lastDonators = new ArrayList<>();
    private PaginatedJson<LastDonator> lastDonatorJson;
    private PaginatedJson<TopDonator> topDonatorJson;
    private final Map<Integer, TopDonator> topDonatorsMap =  new HashMap<>();
    private final SparseIndexedBlockDeque<LastDonator> lastDonatorsDeque = new SparseIndexedBlockDeque<>();
    private final TreeMap<Long, Integer> indexesFetchedLately = new TreeMap<>();

    private final URI[] apiUrls = new URI[3];

    private final Gson gson = new Gson();
    private Thread thread = null;
    
    public PlaceHolderData(MineStoreCommon plugin) {
        this.plugin = plugin;
        registerNativePlaceholders();
    }

    private final Map<Integer, Long> fetchingLast = new ConcurrentHashMap<>();
    private void fetchLastDonator(int index) {
        CompletableFuture.runAsync(() -> {
            if (!MineStoreCommon.version().requires(pagesSupportedSince)) {
                return;
            }
            if (lastDonatorJson == null) {
                return;
            }
            int page = (index + 1) / lastDonatorJson.getPerPage();
            page++;
            if (page > lastDonatorJson.getLastPage()) {
                return;
            }
            if (fetchingLast.containsKey(page) && System.currentTimeMillis() - fetchingLast.get(page) < 1000 * 60) {
                return;
            }
            fetchingLast.put(page, System.currentTimeMillis());
            String finalLastDonatorsUrl;
            String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
            if (storeUrl.endsWith("/")) {
                storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
            }
            finalLastDonatorsUrl = storeUrl + "/api/"
                    + (plugin.pluginConfig().getKey("api").getKey("key-enabled").getAsBoolean()
                    ? plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/getTotalPayments/"
                    : "getTotalPayments/");
            finalLastDonatorsUrl = finalLastDonatorsUrl + "?page=" + page;
            plugin.debug(this.getClass(), "Fetching last donors page " + page);
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(finalLastDonatorsUrl).openConnection();
                StringBuilder response = getResponse(urlConnection);

                plugin.debug(this.getClass(), "Received: " + response);
                if (urlConnection.getResponseCode() != 200) {
                    plugin.debug(this.getClass(), "Failed to fetch last donors page " + page);
                    return;
                }

                Type jsonType = new TypeToken<PaginatedJson<LastDonator>>(){}.getType();
                PaginatedJson<LastDonator> lastDonatorJson = gson.fromJson(response.toString(), jsonType);
                int pageIndex = --page * lastDonatorJson.getPerPage();
                for (LastDonator lastDonator : lastDonatorJson.getList()) {
                    if (lastDonatorsDeque.has(pageIndex)) {
                        pageIndex++;
                        continue;
                    }
                    lastDonatorsDeque.set(pageIndex, lastDonator, false);
                    pageIndex++;
                }
            } catch (Exception e) {
                plugin.debug(this.getClass(), e);
            }
        });
    }

    private final Map<Integer, Long> fetchingTop = new HashMap<>();
    private void fetchTopDonator(int index, boolean force) {
        CompletableFuture.runAsync(() -> {
            if (!MineStoreCommon.version().requires(pagesSupportedSince)) {
                return;
            }
            if (topDonatorJson == null) {
                return;
            }
            int page = (index + 1) / topDonatorJson.getPerPage();
            page++;
            if (page > topDonatorJson.getLastPage()) {
                return;
            }
            if (!force && fetchingTop.containsKey(page) && System.currentTimeMillis() - fetchingTop.get(page) < 1000 * 60 * 2) {
                return;
            }
            fetchingTop.put(page, System.currentTimeMillis());
            indexesFetchedLately.put(System.currentTimeMillis(), page);
            String finalTopDonatorsUrl;
            String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
            if (storeUrl.endsWith("/")) {
                storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
            }
            finalTopDonatorsUrl = storeUrl + "/api/"
                    + (plugin.pluginConfig().getKey("api").getKey("key-enabled").getAsBoolean()
                    ? plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/top_donators/"
                    : "top_donators/");
            finalTopDonatorsUrl = finalTopDonatorsUrl + "?page=" + page;
            plugin.debug(this.getClass(), "Fetching top donors page " + page);
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(finalTopDonatorsUrl).openConnection();
                StringBuilder response = getResponse(urlConnection);

                plugin.debug(this.getClass(), "Received: " + response);
                if (urlConnection.getResponseCode() != 200) {
                    plugin.debug(this.getClass(), "Failed to fetch top donors page " + page);
                    return;
                }

                Type jsonType = new TypeToken<PaginatedJson<TopDonator>>(){}.getType();
                PaginatedJson<TopDonator> topDonatorJson = gson.fromJson(response.toString(), jsonType);
                int pageIndex = --page * topDonatorJson.getPerPage();
                for (TopDonator topDonator : topDonatorJson.getList()) {
                    topDonatorsMap.put(pageIndex, topDonator);
                    pageIndex++;
                }
            } catch (Exception e) {
                plugin.debug(this.getClass(), e);
            }
        });
    }

    private void refetchAllTopDonators() {
        long tenMinutesAgo = System.currentTimeMillis() - 600_000;
        indexesFetchedLately.headMap(tenMinutesAgo, true).clear();
        Set<Integer> toFetch = new HashSet<>(indexesFetchedLately.values());
        for (int indexesFetchedLately : toFetch) {
            fetchTopDonator(indexesFetchedLately, true);
        }
    }

    private static @NotNull StringBuilder getResponse(HttpsURLConnection urlConnection) throws IOException {
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
        return response;
    }

    private LastDonator getLastDonator(int index) {
        if (MineStoreCommon.version().requires(pagesSupportedSince)) {
            LastDonator lastDono = lastDonatorsDeque.get(index);
            if (lastDono != null) {
                return lastDono;
            }
            fetchLastDonator(index);
            return LastDonator.getDefault();
        }
        return getOrDefault(lastDonators, index, LastDonator.getDefault());
    }

    private TopDonator getTopDonator(int index) {
        if (MineStoreCommon.version().requires(pagesSupportedSince)) {
            TopDonator topDono = topDonatorsMap.get(index);
            if (topDono != null) {
                return topDono;
            }
            fetchTopDonator(index, false);
            return TopDonator.getDefault();
        }
        return getOrDefault(topDonators, index, TopDonator.getDefault());
    }

    private <T> T getOrDefault(List<T> list, int index, T defaultValue) {
        if (index < 0) {
            return defaultValue;
        }
        if (list.size() > index) {
            return list.get(index);
        }
        return defaultValue;
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
            return toFixed(getTopDonator(arg - 1).getPrice(), 2);
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
            return toFixed(getLastDonator(arg - 1).getPrice(), 2);
        });
        //Regex that matches last_donator_package_1, last_donator_package_2, etc.
        PlaceHolderManager.getInstance().registerPlaceHolder("last_donator_package_(\\d+)", (name, param) -> {
            param = param.replaceFirst("last_donator_package_", "");
            int arg = Integer.parseInt(param);
            return getLastDonator(arg - 1).getPackageName();
        });
        PlaceHolderManager.getInstance().registerPlaceHolder("donation_goal_current", (name, param) -> toFixed(donationGoal.getDonationGoalCurrentAmount(), 2));
        PlaceHolderManager.getInstance().registerPlaceHolder("donation_goal_target", (name, param) -> toFixed(donationGoal.getDonationGoalAmount(), 2));
        PlaceHolderManager.getInstance().registerPlaceHolder("donation_goal_percentage", (name, param) -> toFixed(donationGoal.getDonationGoalPercentage(), 2));
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
            return toFixed(profile.moneySpent(), 2);
        });
    }

    String toFixed(double value, int precision) {
        if (precision < 0) {
            precision = 0;
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return bd.toPlainString();
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
                plugin.debug(this.getClass(), "Loading placeholder data from " + apiUrl);
                URL url = apiUrl.toURL();
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                StringBuilder response = getResponse(urlConnection);

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
                    try {
                        if (MineStoreCommon.version().requires(pagesSupportedSince)) {
                            Type jsonType = new TypeToken<PaginatedJson<LastDonator>>(){}.getType();
                            lastDonatorJson = gson.fromJson(response.toString(), jsonType);
                            List<LastDonator> newLastDonators = new ArrayList<>();
                            LastDonator fromQueue = lastDonatorsDeque.getFirst();
                            for (LastDonator lastDonator : lastDonatorJson.getList()) {
                                int comparisonResult = fromQueue == null ? 1 : fromQueue.compareTo(lastDonator);
                                if (comparisonResult == -1) {
                                    continue;
                                }
                                if (comparisonResult == 0 && fromQueue.getUserName().equals(lastDonator.getUserName()) && fromQueue.getPackageName().equals(lastDonator.getPackageName())) {
                                    continue;
                                }
                                newLastDonators.add(lastDonator);
                            }
                            if (!newLastDonators.isEmpty()) {
                                for (int i = newLastDonators.size() - 1; i >= 0; i--) {
                                    lastDonatorsDeque.pushFirst(newLastDonators.get(i));
                                }
                                refetchAllTopDonators();
                            }
                        } else {
                            Type listType = new TypeToken<List<LastDonator>>() {
                            }.getType();
                            lastDonators = gson.fromJson(response.toString(), listType);
                        }
                    } catch (JsonSyntaxException e) {
                        plugin.debug(this.getClass(), e);
                    }
                } else if (apiUrl.equals(apiUrls[2])) {
                    if (MineStoreCommon.version().requires(pagesSupportedSince)) {
                        Type jsonType = new TypeToken<PaginatedJson<TopDonator>>(){}.getType();
                        topDonatorJson = gson.fromJson(response.toString(), jsonType);
                        int index = 0;
                        for (TopDonator topDonator : topDonatorJson.getList()) {
                            topDonatorsMap.put(index++, topDonator);
                        }
                        indexesFetchedLately.put(System.currentTimeMillis(), 1);
                    } else {
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
        lastDonatorsDeque.clear();
        topDonatorsMap.clear();
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
