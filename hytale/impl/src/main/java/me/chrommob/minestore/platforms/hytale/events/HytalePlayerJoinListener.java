package me.chrommob.minestore.platforms.hytale.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.event.types.MineStorePlayerQuitEvent;
import me.chrommob.minestore.api.interfaces.event.PlayerEventListener;

public class HytalePlayerJoinListener implements PlayerEventListener {
    public HytalePlayerJoinListener() {
        Registries.LOGGER.get().log("Registering PlayerJoinEvent");
        Universe.get().getEventRegistry().register(PlayerConnectEvent.class, event -> new MineStorePlayerJoinEvent(event.getPlayerRef().getUsername()).call());
        Universe.get().getEventRegistry().register(PlayerDisconnectEvent.class, event -> new MineStorePlayerQuitEvent(event.getPlayerRef().getUsername()).call());
    }
}
