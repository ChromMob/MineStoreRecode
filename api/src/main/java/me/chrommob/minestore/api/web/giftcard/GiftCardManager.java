package me.chrommob.minestore.api.web.giftcard;

import com.google.gson.JsonObject;
import me.chrommob.minestore.api.generic.ParamBuilder;
import me.chrommob.minestore.api.web.FeatureManager;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebApiRequest;
import me.chrommob.minestore.api.web.Wrapper;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.function.Function;

public class GiftCardManager extends FeatureManager {
    public GiftCardManager(Wrapper<Function<WebApiRequest<?>, Result<?, ? extends Exception>>> requestHandler) {
        super(requestHandler);
    }

    /**
     * @param name The name of the gift card, also the coupon code.
     * @param amount The amount of the gift card.
     * @param expiry The expiry date of the gift card.
     * @param description The description of the gift card.
     *                    This is shown to the user when they redeem the gift card.
     * @param tiedUsername The username of the user that the gift card is tied to.
     *                     This is nullable.
     * @return The response of the gift card creation request.
     */
    public CreateGiftCardResponse createGiftCard(String name, String description, int amount, LocalDateTime expiry, @Nullable String tiedUsername) {
        WebApiRequest<JsonObject> request = new WebApiRequest<>("createGiftCard", WebApiRequest.Type.POST, new ParamBuilder()
                .append("code", name)
                .append("balance", String.valueOf(amount))
                .appendDate("expire", expiry)
                .append("note", description)
                .append("username", tiedUsername), JsonObject.class);
        Result<JsonObject, Exception> result = request(request);
        if (result.value() == null) {
            return new GiftCardManager.CreateGiftCardResponse(false, result.error().getMessage());
        }
        boolean success = result.value().get("status").getAsBoolean();
        if (!success) {
            return new GiftCardManager.CreateGiftCardResponse(false, result.value().get("error").getAsString());
        }
        return new GiftCardManager.CreateGiftCardResponse(true, null);
    }

    public ValidateGiftCardResponse validateGiftCard(String coupon) {
        WebApiRequest<ValidateGiftCardResponse> request = new WebApiRequest<>("validateGiftCard", WebApiRequest.Type.GET, new ParamBuilder()
                .append("code", coupon), ValidateGiftCardResponse.class);
        Result<ValidateGiftCardResponse, Exception> result = request(request);
        if (result.value() != null) {
            return result.value();
        }
        return new ValidateGiftCardResponse(null, 0, result.error().getMessage());
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

    public static class CreateGiftCardRequest {
        private final String name;
        private final String description;
        private final int amount;
        private final int expiryYear;
        private final int expiryMonth;
        private final int expiryDay;
        private final int expiryHour;
        private final int expiryMinute;
        private final int expirySecond;

        public CreateGiftCardRequest(String name, String description, int amount, int expiryYear, int expiryMonth, int expiryDay, int expiryHour, int expiryMinute, int expirySecond) {
            this.name = name;
            this.description = description;
            this.amount = amount;
            this.expiryYear = expiryYear;
            this.expiryMonth = expiryMonth;
            this.expiryDay = expiryDay;
            this.expiryHour = expiryHour;
            this.expiryMinute = expiryMinute;
            this.expirySecond = expirySecond;
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
        }

        public int amount() {
            return amount;
        }

        public int expiryYear() {
            return expiryYear;
        }

        public int expiryMonth() {
            return expiryMonth;
        }

        public int expiryDay() {
            return expiryDay;
        }

        public int expiryHour() {
            return expiryHour;
        }

        public int expiryMinute() {
            return expiryMinute;
        }

        public int expirySecond() {
            return expirySecond;
        }
    }

    public static class ValidateGiftCardResponse {
        private String name;
        private int amount;
        private String error;

        public ValidateGiftCardResponse(String name, int amount, String error) {
            this.name = name;
            this.amount = amount;
            this.error = error;
        }

        public  String getName() {
            return name;
        }

        public int getAmount() {
            return amount;
        }

        public String getError() {
            return error;
        }
    }
}
