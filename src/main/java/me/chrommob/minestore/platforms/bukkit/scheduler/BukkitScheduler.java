package me.chrommob.minestore.platforms.bukkit.scheduler;

import me.chrommob.minestore.common.interfaces.scheduler.CommonScheduler;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import org.bukkit.Bukkit;

public class BukkitScheduler implements CommonScheduler {
    private MineStoreBukkit plugin;
    public BukkitScheduler(MineStoreBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }
}
