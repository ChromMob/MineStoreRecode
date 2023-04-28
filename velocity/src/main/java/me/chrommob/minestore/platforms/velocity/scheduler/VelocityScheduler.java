package me.chrommob.minestore.platforms.velocity.scheduler;

import me.chrommob.minestore.common.interfaces.scheduler.CommonScheduler;
import me.chrommob.minestore.platforms.velocity.MineStoreVelocity;

public class VelocityScheduler implements CommonScheduler {
    private MineStoreVelocity plugin;
    public VelocityScheduler(MineStoreVelocity plugin) {
        this.plugin = plugin;
    }
    @Override
    public void run(Runnable runnable) {
        plugin.getServer().getScheduler().buildTask(plugin, runnable).schedule();
    }
}
