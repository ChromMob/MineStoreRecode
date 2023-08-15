package me.chrommob.minestore.platforms.fabric.events;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.event.PlayerEventListener;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricPlayerEvent implements PlayerEventListener {
    public FabricPlayerEvent() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            MineStoreCommon.getInstance().onPlayerJoin(handler.getPlayer().getName().getString());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            MineStoreCommon.getInstance().onPlayerQuit(handler.getPlayer().getName().getString());
        });
    }
}
