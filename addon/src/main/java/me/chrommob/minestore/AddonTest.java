package me.chrommob.minestore;

import me.chrommob.minestore.addons.api.WebApiAccessor;
import me.chrommob.minestore.addons.api.giftcard.GiftCardManager;
import me.chrommob.minestore.addons.api.event.MineStoreEventBus;
import me.chrommob.minestore.addons.api.event.types.MineStoreEnableEvent;
import me.chrommob.minestore.addons.api.event.types.MineStoreLoadEvent;
import me.chrommob.minestore.addons.api.generic.MineStoreAddon;

public class AddonTest implements MineStoreAddon {
    public AddonTest() {
        MineStoreEventBus.registerListener(this, MineStoreLoadEvent.class, event -> {
            System.out.println("HELLO FROM ADDON");
        });

        MineStoreEventBus.registerListener(this, MineStoreEnableEvent.class, event -> {
            GiftCardManager.CreateGiftCardResponse response = WebApiAccessor.couponManager().createGiftCard("Test", "Test", 100, 2022, 1, 1, 0, 0, 0);
            System.out.println(response.isSuccess());
            System.out.println(response.message());
        });
    }

    @Override
    public String getName() {
        return "MineStoreTestAddon";
    }
}