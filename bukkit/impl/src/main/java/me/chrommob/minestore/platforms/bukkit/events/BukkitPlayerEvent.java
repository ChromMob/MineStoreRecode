package me.chrommob.minestore.platforms.bukkit.events;

import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.event.types.MineStorePlayerQuitEvent;
import me.chrommob.minestore.api.interfaces.event.PlayerEventListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlayerEvent implements Listener, PlayerEventListener {
    public BukkitPlayerEvent(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new MineStorePlayerJoinEvent(event.getPlayer().getName()).call();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        new MineStorePlayerQuitEvent(event.getPlayer().getName()).call();
    }
}
