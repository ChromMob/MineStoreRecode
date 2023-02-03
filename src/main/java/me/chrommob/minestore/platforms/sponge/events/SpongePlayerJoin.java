package me.chrommob.minestore.platforms.sponge.events;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.templates.PlayerJoinListener;
import me.chrommob.minestore.platforms.sponge.MineStoreSponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class SpongePlayerJoin implements PlayerJoinListener {
    public SpongePlayerJoin(MineStoreSponge plugin) {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        MineStoreCommon.getInstance().commandStorage().onPlayerJoin(event.getTargetEntity().getName());
    }
}
