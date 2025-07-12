package me.chrommob.minestore.common.authHolder;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.scheduler.MineStoreScheduledTask;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthHolder {
    private MineStoreCommon plugin;
    private final int authTimeout;
    private final Map<String, AuthUser> authUsers = new ConcurrentHashMap<>();
    private final Map<String, ParsedResponse> toPost = new ConcurrentHashMap<>();
    private final String url;

    public AuthHolder(MineStoreCommon plugin) {
        this.plugin = plugin;
        authTimeout = plugin.pluginConfig().getKey("auth").getKey("timeout").getAsInt() * 1000;
        String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        url = storeUrl + "/api/game_auth/confirm/";
    }
    
    public final MineStoreScheduledTask removeAndPost = new MineStoreScheduledTask("removeAndPost", task -> {
        if (authUsers.isEmpty() && toPost.isEmpty()) {
            task.delay(1000);
            return;
        }

        toPost.forEach((s, parsedResponse) -> {
            postAuthCompleted(parsedResponse);
            toPost.remove(s);
        });

        authUsers.forEach((s, authUser) -> {
        /*
        Remove the user from the authUsers map if the user is offline or the authTimeout has been reached.
        This is to prevent memory leaks.
         */
            if (isExpired(authUser) || !authUser.user().isOnline()) {
                plugin.debug(this.getClass(), "Removing " + authUser.user().getName() + " from authUsers map because the authTimeout has been reached (" + this.isExpired(authUser) + ") or the user is offline (" + !authUser.user().isOnline() + ")");
                if (authUser.user().isOnline()) {
                    authUser.user().sendMessage(plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("auth").getKey("timeout-message").getAsString()));
                }
                authUsers.remove(s);
            }
        });
        task.delay(1000);
    });

    private void postAuthCompleted(ParsedResponse parsedResponse) {
        plugin.debug(this.getClass(), "Posting auth completed for " + parsedResponse.username() + " with id " + parsedResponse.authId());
        try {
            HttpsURLConnection urlConnection;
            String link = url + parsedResponse.authId();
            URL url = new URL(link);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            urlConnection.setDoOutput(true);
            try (final OutputStream os = urlConnection.getOutputStream()) {
                // get current time in milliseconds
                os.write(5);
            }
            urlConnection.getInputStream();
        } catch (Exception e) {
            this.plugin.debug(this.getClass(), "Error while posting auth completed for " + parsedResponse.username() + " with id " + parsedResponse.authId());
        }
    }

    private boolean isExpired(AuthUser authUser) {
        return System.currentTimeMillis() - authUser.time() > authTimeout;
    }

    public void confirmAuth(AuthUser authUser) {
        toPost.put(authUser.user().getName(), authUser.parsedResponse());
        authUsers.remove(authUser.user().getName());
    }

    public AuthUser getAuthUser(String username) {
        return authUsers.getOrDefault(username, null);
    }

    /*
    This method is called when a user is authenticated.
    If the user is already in the authUsers map, the time is updated else the user is added to the map.
     */
    public void listener(ParsedResponse parsedResponse) {
        AbstractUser abstractUser = Registries.USER_GETTER.get().get(parsedResponse.username());
        if (!abstractUser.commonUser().isOnline() || abstractUser.commonUser() instanceof CommonConsoleUser) {
            return;
        }
        AuthUser authUser = authUsers.getOrDefault(parsedResponse.username(), null);
        if (authUser == null) {
            authUsers.put(parsedResponse.username().toLowerCase(), new AuthUser(plugin, abstractUser.commonUser(), parsedResponse, System.currentTimeMillis()));
        } else {
            authUser.setTime(System.currentTimeMillis());
        }
    }
}
