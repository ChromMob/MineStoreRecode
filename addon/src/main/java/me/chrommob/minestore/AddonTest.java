package me.chrommob.minestore;

import me.chrommob.minestore.addons.MineStoreAddon;
import me.chrommob.minestore.addons.events.MineStoreEventBus;
import me.chrommob.minestore.addons.events.types.MineStoreLoadEvent;

public class AddonTest implements MineStoreAddon {
    public AddonTest() {
        MineStoreEventBus.registerListener(this, MineStoreLoadEvent.class, event -> {
            System.out.println("HELLO FROM ADDON");
        });
    }

    @Override
    public String getName() {
        return "MineStoreTestAddon";
    }
}