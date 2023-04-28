package me.chrommob.minestore.platforms.bukkit.events;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.event.PlayerEventListener;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitPlayerEvent implements Listener, PlayerEventListener {
    public BukkitPlayerEvent(MineStoreBukkit plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MineStoreCommon.getInstance().onPlayerJoin(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        MineStoreCommon.getInstance().onPlayerQuit(event.getPlayer().getName());
    }
}
