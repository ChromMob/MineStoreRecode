package me.chrommob.minestore.api.generic;

import java.net.MalformedURLException;
import java.net.URL;

public class AuthData {
    private final String storeUrl;
    private final boolean apiKeyEnabled;
    private final String apiKey;

    public AuthData(String storeUrl, boolean apiKeyEnabled, String apiKey) {
        this.storeUrl = storeUrl;
        this.apiKeyEnabled = apiKeyEnabled;
        this.apiKey = apiKey;
    }

    public URL createUrl(String url, String path, String query) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        if (path != null) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (!path.endsWith("/")) {
                path += "/";
            }
        } else {
            path = "";
        }
        try {
            return new URL(url + path + query);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public URL createUrl(String path, String query) {
        if (!apiKeyEnabled) {
            return createNonKeyUrl(path, query);
        }
        return createUrl(storeUrl + "api/" + apiKey + "/", path, query);
    }

    public URL createNonKeyUrl(String path, String query) {
        return createUrl(storeUrl + "api/", path, query);
    }
}
