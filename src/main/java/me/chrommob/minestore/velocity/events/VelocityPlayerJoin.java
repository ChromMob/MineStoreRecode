package me.chrommob.minestore.velocity.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.templates.PlayerJoinListener;
import me.chrommob.minestore.velocity.MineStoreVelocity;

public class VelocityPlayerJoin implements PlayerJoinListener {
    public VelocityPlayerJoin(MineStoreVelocity plugin, ProxyServer server) {
        server.getEventManager().register(plugin, this);
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        MineStoreCommon.getInstance().commandStorage().onPlayerJoin(event.getPlayer().getUsername());
    }

}
