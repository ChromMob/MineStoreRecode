package me.chrommob.minestore.platforms.bungee.scheduler;

import me.chrommob.minestore.api.interfaces.scheduler.CommonScheduler;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeScheduler implements CommonScheduler {
    private Plugin plugin;
    public BungeeScheduler(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public void run(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }
}
