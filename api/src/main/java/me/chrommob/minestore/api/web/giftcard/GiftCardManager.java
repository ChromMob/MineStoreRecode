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
    public CreateGiftCardResponse createGiftCard(String name, String description, double amount, LocalDateTime expiry, @Nullable String tiedUsername) {
        WebApiRequest<JsonObject> request = new WebApiRequest<>("createGiftCard", WebApiRequest.Type.POST, new ParamBuilder()
                .append("code", name)
                .append("balance", String.valueOf(amount))
                .appendDate("expire", expiry)
                .append("note", description)
                .append("username", tiedUsername), JsonObject.class, true);
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
        WebApiRequest<JsonObject> request = new WebApiRequest<>("cart/getGift", WebApiRequest.Type.POST, new ParamBuilder()
                .append("gift", code), JsonObject.class, false);
        Result<JsonObject, Exception> result = request(request);
        if (result.value() == null) {
            return new ValidateGiftCardResponse(result.error().getMessage());
        }
        boolean success = result.value().get("status").getAsBoolean();
        if (!success) {
            return new ValidateGiftCardResponse(result.error().getMessage());
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
            this.success = false;
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
