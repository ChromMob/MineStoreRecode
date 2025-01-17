package me.chrommob.minestore.platforms.bukkit.scheduler;

import me.chrommob.minestore.api.interfaces.scheduler.CommonScheduler;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class BukkitScheduler implements CommonScheduler {
    private MineStoreBukkit plugin;
    private static final boolean isFolia;
    private static final MethodHandle execute;
    private static final Object globalRegionScheduler;
    public BukkitScheduler(MineStoreBukkit plugin) {
        this.plugin = plugin;
    }

    static {
        MethodHandle execute1 = null;
        Object globalRegionScheduler1 = null;
        boolean isFolia1;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia1 = true;
        } catch (ClassNotFoundException e) {
            isFolia1 = false;
        }
        isFolia = isFolia1;
        if (isFolia) {
            Class<? extends Server> serverClass = Bukkit.getServer().getClass();
            Object globalRegionScheduler = null;
            try {
                for (Method method : serverClass.getMethods()) {
                    if (method.getName().equals("getGlobalRegionScheduler")) {
                        globalRegionScheduler = method.invoke(Bukkit.getServer());
                        break;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            globalRegionScheduler1 = globalRegionScheduler;
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                for (Method method : globalRegionScheduler.getClass().getMethods()) {
                    if (method.getName().equals("execute")) {
                        execute1 = lookup.unreflect(method);
                        break;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        globalRegionScheduler = globalRegionScheduler1;
        execute = execute1;
    }

    @Override
    public void run(Runnable runnable) {
        if (isFolia) {
            try {
                execute.invoke(globalRegionScheduler, plugin, runnable);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }
}
