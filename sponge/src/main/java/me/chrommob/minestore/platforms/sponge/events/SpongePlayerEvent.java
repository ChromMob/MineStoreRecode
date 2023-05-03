package me.chrommob.minestore.platforms.sponge.events;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.event.PlayerEventListener;
import me.chrommob.minestore.platforms.sponge.MineStoreSponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class SpongePlayerEvent implements PlayerEventListener {
    public SpongePlayerEvent(MineStoreSponge plugin) {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        MineStoreCommon.getInstance().onPlayerJoin(event.getTargetEntity().getName());
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        MineStoreCommon.getInstance().onPlayerQuit(event.getTargetEntity().getName());
    }
}
