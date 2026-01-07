package me.chrommob.minestore.api.web;

import me.chrommob.minestore.api.web.coupon.CouponManager;
import me.chrommob.minestore.api.web.giftcard.GiftCardManager;
import me.chrommob.minestore.api.web.payment.PaymentManager;
import me.chrommob.minestore.api.web.profile.ProfileManager;

import java.util.function.Function;

public class WebApiAccessor {
    private static final Wrapper<Function<WebRequest<?>, Result<?, WebContext>>> requestHandler = new Wrapper<>(null);

    private static final GiftCardManager giftCardManager = new GiftCardManager(requestHandler);
    private static final ProfileManager profileManager = new ProfileManager(requestHandler);
    private static final CouponManager couponManager = new CouponManager(requestHandler);
    private static final PaymentManager paymentManager = new PaymentManager(requestHandler);

    public static GiftCardManager giftCardManager() {
        return giftCardManager;
    }

    public static ProfileManager profileManager() {
        return profileManager;
    }

    public static CouponManager couponManager() {
        return couponManager;
    }

    public static PaymentManager paymentManager() {
        return paymentManager;
    }

    public static void registerRequestHandler(Function<WebRequest<?>, Result<?, WebContext>> function) {
        requestHandler.set(function);
    }

    public static <T> Result<T, WebContext> request(WebRequest<T> request) {
        return (Result<T, WebContext>) requestHandler.get().apply(request);
    }
}