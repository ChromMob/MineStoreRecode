package me.chrommob.minestore.platforms.fabric.events;

import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.event.types.MineStorePlayerQuitEvent;
import me.chrommob.minestore.api.interfaces.event.PlayerEventListener;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricPlayerEvent implements PlayerEventListener {
    public FabricPlayerEvent() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            new MineStorePlayerJoinEvent(handler.getPlayer().getName().getString()).call();
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            new MineStorePlayerQuitEvent(handler.getPlayer().getName().getString()).call();
        });
    }
}
