package me.chrommob.minestore.api.generic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

public class ParamBuilder {
    private final LinkedHashMap<String, String> params = new LinkedHashMap<>();

    public ParamBuilder append(String key, String value) {
        if (value == null || value.isEmpty()) {
            return this;
        }
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

    public ParamBuilder appendDate(String key, LocalDateTime date) {
        if (date == null) {
            return this;
        }
        int expiryYear = date.getYear();
        int expiryMonth = date.getMonthValue();
        int expiryDay = date.getDayOfMonth();
        int expiryHour = date.getHour();
        int expiryMinute = date.getMinute();
        int expirySecond = date.getSecond();
        String expiryMonthString = expiryMonth < 10 ? "0" + expiryMonth : expiryMonth + "";
        String expiryDayString = expiryDay < 10 ? "0" + expiryDay : expiryDay + "";
        String expiryHourString = expiryHour < 10 ? "0" + expiryHour : expiryHour + "";
        String expiryMinuteString = expiryMinute < 10 ? "0" + expiryMinute : expiryMinute + "";
        String expirySecondString = expirySecond < 10 ? "0" + expirySecond : expirySecond + "";
        String expiry = expiryYear + "-" + expiryMonthString + "-" + expiryDayString + " " + expiryHourString + ":" + expiryMinuteString + ":" + expirySecondString;
        return append(key, expiry);
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        String[] keys = params.keySet().toArray(new String[0]);
        for (int i = 0; i < params.size(); i++) {
            String key = keys[i];
            String value = params.get(key);
            builder.append(key).append("=").append(value);
            if (i != params.size() - 1) {
                builder.append("&");
            }
        }
        return builder.toString();
    }
}
