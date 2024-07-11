package me.chrommob.minestore.platforms.velocity.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.event.PlayerEventListener;
import me.chrommob.minestore.platforms.velocity.MineStoreVelocity;

public class VelocityPlayerEvent implements PlayerEventListener {
    private final MineStoreCommon pl;
    public VelocityPlayerEvent(MineStoreVelocity plugin, ProxyServer server, MineStoreCommon pl) {
        this.pl = pl;
        server.getEventManager().register(plugin, this);
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        pl.onPlayerJoin(event.getPlayer().getUsername());
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        pl.onPlayerQuit(event.getPlayer().getUsername());
    }
}
