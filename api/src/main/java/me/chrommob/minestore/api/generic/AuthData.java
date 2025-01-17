package me.chrommob.minestore.api.generic;

import java.net.MalformedURLException;
import java.net.URL;

public class AuthData {
    private final String storeUrl;
    private final String apiKey;

    public AuthData(String storeUrl, String apiKey) {
        this.storeUrl = storeUrl;
        this.apiKey = apiKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuthData) {
            AuthData authData = (AuthData) obj;
            return authData.storeUrl.equals(this.storeUrl) && authData.apiKey.equals(this.apiKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return storeUrl.hashCode() + apiKey.hashCode();
    }

    public URL createUrl(String path, String query) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        try {
            return new URL(storeUrl + "api/" + apiKey + "/" + path + query);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public URL createNonKeyUrl(String path, String query) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        try {
            return new URL(storeUrl + "api/" + path + query);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
