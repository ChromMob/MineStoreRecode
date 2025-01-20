package me.chrommob.minestore.common.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.chrommob.minestore.api.WebApiAccessor;
import me.chrommob.minestore.api.generic.AuthData;
import me.chrommob.minestore.api.generic.ParamBuilder;
import me.chrommob.minestore.api.giftcard.GiftCardManager;
import me.chrommob.minestore.api.profile.ProfileManager;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class ApiHandler {
    private final Gson gson = new Gson();
    private final AuthData authData;
    public ApiHandler(AuthData authData) {
        this.authData = authData;
        WebApiAccessor.giftCardManager().registerFunction((createGiftCardRequest -> CompletableFuture.supplyAsync(() -> createGiftCard(createGiftCardRequest.name(), createGiftCardRequest.description(), createGiftCardRequest.amount(), createGiftCardRequest.expiryYear(), createGiftCardRequest.expiryMonth(), createGiftCardRequest.expiryDay(), createGiftCardRequest.expiryHour(), createGiftCardRequest.expiryMinute(), createGiftCardRequest.expirySecond()))));
        WebApiAccessor.profileManager().registerFunction((this::getProfile));
    }

    public ProfileManager.Profile getProfile(String username) {
        URL url = authData.createNonKeyUrl("profile/" + username, "");
        try {
            return gson.fromJson(new BufferedReader(new InputStreamReader(url.openStream())), ProfileManager.Profile.class);
        } catch (Exception e) {
            return null;
        }
    }

    private GiftCardManager.CreateGiftCardResponse createGiftCard(String name, String description, int amount, int expiryYear, int expiryMonth, int expiryDay, int expiryHour, int expiryMinute, int expirySecond) {
        String expiryMonthString = expiryMonth < 10 ? "0" + expiryMonth : expiryMonth + "";
        String expiryDayString = expiryDay < 10 ? "0" + expiryDay : expiryDay + "";
        String expiryHourString = expiryHour < 10 ? "0" + expiryHour : expiryHour + "";
        String expiryMinuteString = expiryMinute < 10 ? "0" + expiryMinute : expiryMinute + "";
        String expirySecondString = expirySecond < 10 ? "0" + expirySecond : expirySecond + "";
        String expiry = expiryYear + "-" + expiryMonthString + "-" + expiryDayString + " " + expiryHourString + ":" + expiryMinuteString + ":" + expirySecondString;
        ParamBuilder paramBuilder = new ParamBuilder();
        paramBuilder.append("code", name).append("balance", String.valueOf(amount)).append("expire", expiry).append("note", description);
        URL createGiftCardUrl = authData.createUrl("createGiftCard", paramBuilder.build());
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
                return new GiftCardManager.CreateGiftCardResponse(false, jsonObject.get("error").getAsString());
            }
            return new GiftCardManager.CreateGiftCardResponse(true, null);
        } catch (IOException e) {
            return new GiftCardManager.CreateGiftCardResponse(false, e.getMessage());
        }
    }
}
