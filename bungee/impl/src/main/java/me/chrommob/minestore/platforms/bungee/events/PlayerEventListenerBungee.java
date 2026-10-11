package me.chrommob.minestore.platforms.bungee.events;

import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.event.types.MineStorePlayerQuitEvent;
import me.chrommob.minestore.api.interfaces.event.PlayerEventListener;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PlayerEventListenerBungee implements Listener, PlayerEventListener {
    public PlayerEventListenerBungee(Plugin mineStoreBungee) {
        mineStoreBungee.getProxy().getPluginManager().registerListener(mineStoreBungee, this);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        new MineStorePlayerJoinEvent(event.getPlayer().getName()).call();
    }

    // PlayerDisconnectEvent, ServerDisconnectEvent also fires on every backend server switch.
    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        new MineStorePlayerQuitEvent(event.getPlayer().getName()).call();
    }
}
