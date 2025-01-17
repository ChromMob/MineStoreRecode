package me.chrommob.minestore.api.giftcard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.chrommob.minestore.api.generic.ParamBuilder;
import me.chrommob.minestore.api.generic.AuthData;
import me.chrommob.minestore.api.generic.FeatureManager;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

public class GiftCardManager extends FeatureManager {
    public GiftCardManager(AuthData authData) {
        super(authData);
    }

    public CreateGiftCardResponse createGiftCard(String name, String description, int amount, int expiryYear) {
        return createGiftCard(name, description, amount, expiryYear + 1, 1, 1, 0, 0, 0);
    }

    public CreateGiftCardResponse createGiftCard(String name, String description, int amount, int expiryYear, int expiryMonth, int expiryDay, int expiryHour, int expiryMinute, int expirySecond) {
        String expiryMonthString = expiryMonth < 10 ? "0" + expiryMonth : expiryMonth + "";
        String expiryDayString = expiryDay < 10 ? "0" + expiryDay : expiryDay + "";
        String expiryHourString = expiryHour < 10 ? "0" + expiryHour : expiryHour + "";
        String expiryMinuteString = expiryMinute < 10 ? "0" + expiryMinute : expiryMinute + "";
        String expirySecondString = expirySecond < 10 ? "0" + expirySecond : expirySecond + "";
        String expiry = expiryYear + "-" + expiryMonthString + "-" + expiryDayString + " " + expiryHourString + ":" + expiryMinuteString + ":" + expirySecondString;
        ParamBuilder paramBuilder = new ParamBuilder();
        paramBuilder.append("code", name).append("balance", String.valueOf(amount)).append("expire", expiry).append("note", description);
        URL createGiftCardUrl = authData().createUrl("createGiftCard", paramBuilder.build());
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) createGiftCardUrl.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.getOutputStream().write(new byte[0]);
            Reader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            boolean success = jsonObject.get("status").getAsBoolean();
            if (!success) {
                return new CreateGiftCardResponse(false, jsonObject.get("error").getAsString());
            }
            return new CreateGiftCardResponse(true, null);
        } catch (IOException e) {
            return new CreateGiftCardResponse(false, e.getMessage());
        }
    }

    public static class CreateGiftCardResponse {
        private final boolean success;
        private final String message;

        public CreateGiftCardResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        /**
         * @return Whether the request was successful or not.
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * @return The error message if the request was not successful.
         * If the request was successful, this method will return null.
         */
        public String message() {
            return message;
        }
    }
}
