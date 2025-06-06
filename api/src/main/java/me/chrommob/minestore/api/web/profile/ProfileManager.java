package me.chrommob.minestore.api.web.profile;

import com.google.gson.annotations.SerializedName;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebApiRequest;
import me.chrommob.minestore.api.web.Wrapper;
import me.chrommob.minestore.api.web.FeatureManager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ProfileManager extends FeatureManager {
    public ProfileManager(Wrapper<Function<WebApiRequest<?>, Result<?, ? extends Exception>>> requestHandler) {
        super(requestHandler);
    }

    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    private final Set<String> fetching = ConcurrentHashMap.newKeySet();

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
        if (fetching.contains(username)) {
            return;
        }
        fetching.add(username);
        CompletableFuture.runAsync(() -> {
            Result<Profile, Exception> result = request(new WebApiRequest<>("profile/" + username, WebApiRequest.Type.GET, Profile.class));
            if (result.value() == null) {
                return;
            }
            Profile profile = result.value();
            profile.fetched();
            profiles.put(username, profile);
            fetching.remove(username);
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
