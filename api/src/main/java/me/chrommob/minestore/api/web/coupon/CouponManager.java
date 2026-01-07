package me.chrommob.minestore.api.web.coupon;

import com.google.gson.annotations.SerializedName;
import me.chrommob.minestore.api.generic.ParamBuilder;
import me.chrommob.minestore.api.web.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class CouponManager extends FeatureManager {

    public CouponManager(Wrapper<Function<WebRequest<?>, Result<?, WebContext>>> requestHandler) {
        super(requestHandler);
    }

    /**
     * Creates a new coupon with the specified parameters.
     *
     * @param name             Unique coupon name. (Required)
     * @param type             Discount type: 0 = percent, 1 = amount. (Required)
     * @param discount         Discount value based on {@code type}. (Required)
     * @param available        Total available uses for the coupon. Can be {@code null}.
     * @param limitPerUser     Maximum number of times a user can use the coupon. Can be {@code null}.
     * @param minBasket        Minimum basket amount required to use the coupon. Can be {@code null}.
     * @param applyType        Application type: 0 = whole store, 1 = categories, 2 = packages. (Required)
     * @param applyCategories  List of category IDs the coupon applies to. Required if {@code applyType} = 1, otherwise can be {@code null}.
     * @param applyItems       List of package IDs the coupon applies to. Required if {@code applyType} = 2, otherwise can be {@code null}.
     * @param note             Optional note for the coupon. Can be {@code null}.
     * @param startAt          Coupon start date in the format {@code YYYY-MM-DD HH:MM:SS}. Can be {@code null}.
     * @param expireAt         Coupon expiration date in the format {@code YYYY-MM-DD HH:MM:SS}. Can be {@code null}.
     * @param username         Username the coupon is limited to. Can be {@code null}.
     * @return                 A newly created {@code Coupon} object.
     * @throws IllegalArgumentException if required fields are missing or invalid.
     */
    public CreateCouponResponse createCoupon(String name, int type, int discount, Integer available, Integer limitPerUser, Integer minBasket, int applyType, String[] applyCategories, String[] applyItems, String note, LocalDateTime startAt, LocalDateTime expireAt, String username) {
        WebRequest<CreateCouponResponse> request = new WebRequest.Builder<>(CreateCouponResponse.class)
                .requiresApiKey(true)
                .type(WebRequest.Type.POST)
                .path("createCoupon")
                .paramBuilder(new ParamBuilder()
                        .append("name", name)
                        .append("type", String.valueOf(type))
                        .append("discount", String.valueOf(discount))
                        .append("available", available == null ? null : String.valueOf(available))
                        .append("limit_per_user", limitPerUser == null ? null : String.valueOf(limitPerUser))
                        .append("min_basket", minBasket == null ? null : String.valueOf(minBasket))
                        .append("apply_type", String.valueOf(applyType))
                        .append("apply_categories", applyCategories == null ? null : Arrays.toString(applyCategories))
                        .append("apply_items", applyItems == null ? null : Arrays.toString(applyItems))
                        .append("note", note)
                        .appendDate("start_at", startAt)
                        .appendDate("expire_at", expireAt)
                        .append("username", username))
                .build();
        Result<CreateCouponResponse, WebContext> result = request(request);
        if (result.isError()) {
            return new CreateCouponResponse(result.context().getMessage());
        };
        return result.value();
    }

    public static class CreateCouponResponse {
        private boolean status;
        private String error;
        private Map<String, String[]> details;
        private Coupon coupon;

        public CreateCouponResponse(String error) {
            this.status = false;
            this.error = error;
        }

        public boolean isSuccess() {
            return status;
        }

        public String getError() {
            return error;
        }

        public Map<String, String[]> getDetails() {
            return details;
        }

        public Coupon getCoupon() {
            return coupon;
        }
    }

    public static class Coupon {
        private double id;
        private String name;
        private double type;
        private double discount;
        private double uses;
        private Double available;

        @SerializedName("limit_per_user")
        private double limitPerUser;

        @SerializedName("min_basket")
        private double minBasket;

        @SerializedName("apply_type")
        private double applyType;

        private String note;

        @SerializedName("user_id")
        private Double userId;

        private double deleted;

        @SerializedName("start_at")
        private String startAt;

        @SerializedName("expire_at")
        private String expireAt;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        public double getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getType() {
            return type;
        }

        public double getDiscount() {
            return discount;
        }

        public double getUses() {
            return uses;
        }

        public Double getAvailable() {
            return available;
        }

        public double getLimitPerUser() {
            return limitPerUser;
        }

        public double getMinBasket() {
            return minBasket;
        }

        public double getApplyType() {
            return applyType;
        }

        public String getNote() {
            return note;
        }

        public Double getUserId() {
            return userId;
        }

        public double getDeleted() {
            return deleted;
        }

        public String getStartAt() {
            return startAt;
        }

        public String getExpireAt() {
            return expireAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}
