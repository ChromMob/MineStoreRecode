package me.chrommob.minestore.api.generic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

public class ParamBuilder {
    private final LinkedHashMap<String, String> params = new LinkedHashMap<>();

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
        for (int i = 0; i < params.size(); i++) {
            String key = params.keySet().toArray(new String[0])[i];
            String value = params.get(key);
            builder.append(key).append("=").append(value);
            if (i != params.size() - 1) {
                builder.append("&");
            }
        }
        return builder.toString();
    }
}
