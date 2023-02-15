package me.chrommob.minestore.platforms.bungee.events;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.event.PlayerEventListener;
import me.chrommob.minestore.platforms.bungee.MineStoreBungee;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerEventListenerBungee implements Listener, PlayerEventListener {
    public PlayerEventListenerBungee(MineStoreBungee mineStoreBungee) {
        mineStoreBungee.getProxy().getPluginManager().registerListener(mineStoreBungee, this);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        MineStoreCommon.getInstance().onPlayerJoin(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(ServerDisconnectEvent event ) {
        MineStoreCommon.getInstance().onPlayerQuit(event.getPlayer().getName());
    }
}
