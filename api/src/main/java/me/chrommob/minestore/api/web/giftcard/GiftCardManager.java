package me.chrommob.minestore.api.web.giftcard;

import com.google.gson.JsonObject;
import me.chrommob.minestore.api.generic.ParamBuilder;
import me.chrommob.minestore.api.web.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.function.Function;

public class GiftCardManager extends FeatureManager {
    public GiftCardManager(Wrapper<Function<WebRequest<?>, Result<?, WebContext>>> requestHandler) {
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
    public CreateGiftCardResponse createGiftCard(String name, String description, double amount, LocalDateTime expiry, @Nullable String tiedUsername) {
        WebRequest<JsonObject> request = new WebRequest.Builder<>(JsonObject.class)
                .requiresApiKey(true)
                .type(WebRequest.Type.POST)
                .path("createGiftCard")
                .paramBuilder(new ParamBuilder()
                        .append("code", name)
                        .append("balance", String.valueOf(amount))
                        .appendDate("expire", expiry)
                        .append("note", description)
                        .append("username", tiedUsername))
                .build();
        Result<JsonObject, WebContext> result = request(request);
        if (result.isError()) {
            return new GiftCardManager.CreateGiftCardResponse(false, result.context().getMessage());
        }
        boolean success = result.value().get("status").getAsBoolean();
        if (!success) {
            return new GiftCardManager.CreateGiftCardResponse(false, result.value().get("error").getAsString());
        }
        return new GiftCardManager.CreateGiftCardResponse(true, null);
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

    public ValidateGiftCardResponse validateGiftCard(String code) {
        WebRequest<JsonObject> request = new WebRequest.Builder<>(JsonObject.class)
                .requiresApiKey(true)
                .type(WebRequest.Type.POST)
                .path("cart/getGift")
                .paramBuilder(new ParamBuilder()
                        .append("gift", code))
                .build();
        Result<JsonObject, WebContext> result = request(request);
        if (result.isError()) {
            return new ValidateGiftCardResponse(result.context().getMessage());
        }
        boolean success = result.value().get("status").getAsBoolean();
        if (!success) {
            return new ValidateGiftCardResponse(result.context().getMessage());
        }
        return new ValidateGiftCardResponse(result.value().get("start_balance").getAsDouble(), result.value().get("end_balance").getAsDouble(), result.value().get("currency").getAsString());
    }


    public static class ValidateGiftCardResponse {
        private final boolean success;
        private String message;
        public ValidateGiftCardResponse(String message) {
            this.success = false;
            this.message = message;
        }
        private double startBalance;
        private double currentBalance;
        private String currency;

        public ValidateGiftCardResponse(double startBalance, double currentBalance, String currency) {
            this.success = true;
            this.startBalance = startBalance;
            this.currentBalance = currentBalance;
            this.currency = currency;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public double getStartBalance() {
            return startBalance;
        }

        public double getCurrentBalance() {
            return currentBalance;
        }

        public String getCurrency() {
            return currency;
        }
    }
}
