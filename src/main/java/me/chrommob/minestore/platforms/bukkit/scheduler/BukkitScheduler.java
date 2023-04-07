package me.chrommob.minestore.platforms.bukkit.scheduler;

import me.chrommob.minestore.common.interfaces.scheduler.CommonScheduler;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Method;

public class BukkitScheduler implements CommonScheduler {
    private MineStoreBukkit plugin;
    private final boolean isFolia;
    public BukkitScheduler(MineStoreBukkit plugin) {
        boolean isFolia1;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia1 = true;
        } catch (ClassNotFoundException e) {
            isFolia1 = false;
        }
        isFolia = isFolia1;
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable runnable) {
        if (isFolia) {
            Server server = Bukkit.getServer();
            for (Method method: server.getClass().getMethods()) {
                if (method.getName().equals("getGlobalRegionScheduler")) {
                    try {
                        Object regionScheduler = method.invoke(server);
                        for (Method schedulerMethods : regionScheduler.getClass().getMethods()) {
                            if (schedulerMethods.getName().equals("execute")) {
                                schedulerMethods.invoke(regionScheduler, plugin, runnable);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }
}
