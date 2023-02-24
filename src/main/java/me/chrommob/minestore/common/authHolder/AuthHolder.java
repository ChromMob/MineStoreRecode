package me.chrommob.minestore.common.authHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthHolder {
    private int authTimeout;
    private Map<String, AuthUser> authUsers = new ConcurrentHashMap<>();
    private Map<String, ParsedResponse> toPost = new ConcurrentHashMap<>();
    private String url;
    private Thread thread = null;
    private Runnable removeAndPost = () -> {
        while (true) {
            if (authUsers.isEmpty() && toPost.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
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
                    MineStoreCommon.getInstance().debug("Removing " + authUser.user().getName() + " from authUsers map because the authTimeout has been reached (" + this.isExpired(authUser) + ") or the user is offline (" + !authUser.user().isOnline() + ")");
                    if (authUser.user().isOnline()) {
                        authUser.user().sendMessage((MineStoreCommon.getInstance().miniMessage()).deserialize((String)MineStoreCommon.getInstance().configReader().get(ConfigKey.AUTH_TIMEOUT_MESSAGE)));
                    }
                    authUsers.remove(s);
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    };

    public AuthHolder(MineStoreCommon plugin) {
        authTimeout = (int) plugin.configReader().get(ConfigKey.AUTH_TIMEOUT) * 1000;
        thread = new Thread(removeAndPost);
        thread.start();
        String storeUrl = (String) MineStoreCommon.getInstance().configReader().get(ConfigKey.STORE_URL);
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        url = storeUrl + "/api/game_auth/confirm/";
    }

    private void postAuthCompleted(ParsedResponse parsedResponse) {
        MineStoreCommon.getInstance().debug("Posting auth completed for " + parsedResponse.username() + " with id " + parsedResponse.authId());
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
            MineStoreCommon.getInstance().debug("Error while posting auth completed for " + parsedResponse.username() + " with id " + parsedResponse.authId());
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
        AbstractUser abstractUser = new AbstractUser(parsedResponse.username());
        if (!abstractUser.user().isOnline() || abstractUser.user() instanceof CommonConsoleUser) {
            return;
        }
        AuthUser authUser = authUsers.getOrDefault(parsedResponse.username(), null);
        if (authUser == null) {
            authUsers.put(parsedResponse.username(), new AuthUser(abstractUser.user(), parsedResponse, System.currentTimeMillis()));
        } else {
            authUser.setTime(System.currentTimeMillis());
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }
}
