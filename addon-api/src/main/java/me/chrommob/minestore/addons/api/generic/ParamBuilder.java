package me.chrommob.minestore.addons.api.generic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class ParamBuilder {
    private final HashMap<String, String> params = new HashMap<>();

    public ParamBuilder append(String key, String value) {
        if (params.containsKey(key)) {
            return this;
        }
        try {
            params.put(key, URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        for (String key : params.keySet()) {
            builder.append(key).append("=").append(params.get(key)).append("&");
        }
        return builder.toString();
    }
}
