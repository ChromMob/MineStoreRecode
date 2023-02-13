package me.chrommob.minestore.platforms.velocity.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.event.PlayerJoinListener;
import me.chrommob.minestore.platforms.velocity.MineStoreVelocity;

public class VelocityPlayerJoin implements PlayerJoinListener {
    public VelocityPlayerJoin(MineStoreVelocity plugin, ProxyServer server) {
        server.getEventManager().register(plugin, this);
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        MineStoreCommon.getInstance().commandStorage().onPlayerJoin(event.getPlayer().getUsername());
    }

}
