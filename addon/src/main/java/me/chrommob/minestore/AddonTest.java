package me.chrommob.minestore;

import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStoreEnableEvent;
import me.chrommob.minestore.api.event.types.MineStoreLoadEvent;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.web.WebApiAccessor;
import me.chrommob.minestore.api.web.coupon.CouponManager;

import java.util.Arrays;
import java.util.UUID;

public class AddonTest implements MineStoreAddon {
    public AddonTest() {
        MineStoreEventBus.registerListener(this, MineStoreLoadEvent.class, event -> System.out.println("HELLO FROM ADDON"));

        MineStoreEventBus.registerListener(this, MineStoreEnableEvent.class, event -> {
            CouponManager.CreateCouponResponse res = WebApiAccessor.couponManager().createCoupon("Test"/* + UUID.randomUUID()*/, 1, 20, null, null, null, 0, null, null, null, null, null, null);
            System.out.println(res.isSuccess());
            System.out.println(res.getError());
            System.out.println(res.getCoupon());
            res.getDetails().forEach((key, value) -> System.out.println(key + ": " + Arrays.toString(value)));
        });
    }

    @Override
    public String getName() {
        return "MineStoreTestAddon";
    }
}