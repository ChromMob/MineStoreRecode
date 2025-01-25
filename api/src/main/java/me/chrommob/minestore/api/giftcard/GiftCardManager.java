package me.chrommob.minestore.api.giftcard;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GiftCardManager {
    private Function<CreateGiftCardRequest, CompletableFuture<CreateGiftCardResponse>> function;
    public CompletableFuture<CreateGiftCardResponse> createGiftCardAsync(String name, String description, int amount, int expiryYear) {
        return createGiftCardAsync(name, description, amount, expiryYear + 1, 1, 1, 0, 0, 0);
    }

    public CreateGiftCardResponse createGiftCard(String name, String description, int amount, int expiryYear) {
        return createGiftCardAsync(name, description, amount, expiryYear).join();
    }

    public CompletableFuture<CreateGiftCardResponse> createGiftCardAsync(String name, String description, int amount, int expiryYear, int expiryMonth, int expiryDay, int expiryHour, int expiryMinute, int expirySecond) {
        return function.apply(new CreateGiftCardRequest(name, description, amount, expiryYear, expiryMonth, expiryDay, expiryHour, expiryMinute, expirySecond));
    }

    public CreateGiftCardResponse createGiftCard(String name, String description, int amount, int expiryYear, int expiryMonth, int expiryDay, int expiryHour, int expiryMinute, int expirySecond) {
        return createGiftCardAsync(name, description, amount, expiryYear, expiryMonth, expiryDay, expiryHour, expiryMinute, expirySecond).join();
    }

    public void registerFunction(Function<CreateGiftCardRequest, CompletableFuture<CreateGiftCardResponse>> function) {
        this.function = function;
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
}
