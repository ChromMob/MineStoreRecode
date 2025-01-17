package me.chrommob.minestore.api.profile;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import me.chrommob.minestore.api.generic.AuthData;
import me.chrommob.minestore.api.generic.FeatureManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileManager extends FeatureManager {
    private final Gson gson = new Gson();
    public ProfileManager(AuthData authData) {
        super(authData);
    }

    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    public Profile getProfile(String username) {
        URL url = authData().createNonKeyUrl("profile/" + username, "");
        try {
            return gson.fromJson(new BufferedReader(new InputStreamReader(url.openStream())), Profile.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Profile getCachedProfile(String username) {
        if (profiles.containsKey(username)) {
            if (profiles.get(username).needsUpdate()) {
                updateProfile(username);
            }
            return profiles.get(username);
        }
        updateProfile(username);
        return null;
    }

    private void updateProfile(String username) {
        CompletableFuture.runAsync(() -> {
            Profile profile = getProfile(username);
            if (profile == null) {
                return;
            }
            profile.fetched();
            profiles.put(username, profile);
        });
    }

    public static class Profile {
        private long fetched;

        @SerializedName("display_group")
        private String displayGroup;
        @SerializedName("uuid")
        private UUID uuid;
        @SerializedName("username")
        private String username;
        @SerializedName("displayname")
        private String displayName;
        @SerializedName("group")
        private String group;
        @SerializedName("created")
        private String created;
        @SerializedName("top_item_name")
        private String topItemName;
        @SerializedName("top_item_id")
        private int topItemId;
        @SerializedName("items")
        private Item[] items;
        @SerializedName("money_spent")
        private double moneySpent;

        public String displayGroup() {
            return displayGroup;
        }

        public UUID uuid() {
            return uuid;
        }

        public String username() {
            return username;
        }

        public String displayName() {
            return displayName;
        }

        public String group() {
            return group;
        }

        public String created() {
            return created;
        }

        public String topItemName() {
            return topItemName;
        }

        public int topItemId() {
            return topItemId;
        }

        public Item[] items() {
            return items;
        }

        public double moneySpent() {
            return moneySpent;
        }

        public void fetched() {
            fetched = System.currentTimeMillis();
        }

        public boolean needsUpdate() {
            return System.currentTimeMillis() - fetched > 1000 * 60 * 5;
        }
    }

    public static class Item {
        private int id;
        private String name;
        private double price;

        public int id() {
            return id;
        }

        public String name() {
            return name;
        }

        public double price() {
            return price;
        }
    }
}
