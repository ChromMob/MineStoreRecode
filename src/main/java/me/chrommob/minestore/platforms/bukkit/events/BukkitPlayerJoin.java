package me.chrommob.minestore.platforms.bukkit.events;

import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.PlayerJoinListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BukkitPlayerJoin implements Listener, PlayerJoinListener {
    public BukkitPlayerJoin(MineStoreBukkit plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MineStoreCommon.getInstance().commandStorage().onPlayerJoin(event.getPlayer().getName());
    }
}
