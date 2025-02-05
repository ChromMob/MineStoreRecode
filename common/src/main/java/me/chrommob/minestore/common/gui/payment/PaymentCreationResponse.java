package me.chrommob.minestore.common.gui.payment;

public class PaymentCreationResponse {
    private boolean success;
    private String message;

    private CartResponse cart_response;
    private PaymentResponse payment_response;

    public static class CartResponse {
        private boolean success;

        public boolean isSuccess() {
            return success;
        }
    }

    public static class PaymentResponse {
        private boolean success;
        private Data data;

        public boolean isSuccess() {
            return success;
        }

        public Data getData() {
            return data;
        }

        public static class Data {
            private String type;
            private String url;
            private String order_id;

            public String getType() {
                return type;
            }

            public String getUrl() {
                return url;
            }

            public String getOrderId() {
                return order_id;
            }
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public CartResponse getCartResponse() {
        return cart_response;
    }

    public PaymentResponse getPaymentResponse() {
        return payment_response;
    }
}
