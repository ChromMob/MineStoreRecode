package me.chrommob.minestore;

import me.chrommob.minestore.api.WebApiAccessor;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStoreEnableEvent;
import me.chrommob.minestore.api.event.types.MineStoreLoadEvent;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.giftcard.GiftCardManager;

public class AddonTest implements MineStoreAddon {
    public AddonTest() {
        MineStoreEventBus.registerListener(this, MineStoreLoadEvent.class, event -> System.out.println("HELLO FROM ADDON"));

        MineStoreEventBus.registerListener(this, MineStoreEnableEvent.class, event -> {
            GiftCardManager.CreateGiftCardResponse response = WebApiAccessor.giftCardManager().createGiftCard("Test", "Test", 100, 2022, 1, 1, 0, 0, 0);
            System.out.println(response.isSuccess());
            System.out.println(response.message());
        });
    }

    @Override
    public String getName() {
        return "MineStoreTestAddon";
    }
}