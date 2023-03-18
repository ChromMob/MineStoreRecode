package me.chrommob.minestore.platforms.bungee.scheduler;

import me.chrommob.minestore.common.interfaces.scheduler.CommonScheduler;
import me.chrommob.minestore.platforms.bungee.MineStoreBungee;

public class BungeeScheduler implements CommonScheduler {
    private MineStoreBungee plugin;
    public BungeeScheduler(MineStoreBungee plugin) {
        this.plugin = plugin;
    }
    @Override
    public void run(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }
}
