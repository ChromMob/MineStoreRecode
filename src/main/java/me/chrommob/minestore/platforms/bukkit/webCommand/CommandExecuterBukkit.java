package me.chrommob.minestore.platforms.bukkit.webCommand;

import me.chrommob.minestore.common.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandExecuterBukkit implements CommandExecuterCommon {

    private final MineStoreBukkit plugin;
    private final boolean isFolia;

    public CommandExecuterBukkit(MineStoreBukkit plugin) {
        boolean isFolia1;
        this.plugin = plugin;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia1 = true;
        } catch (ClassNotFoundException e) {
            isFolia1 = false;
        }
        isFolia = isFolia1;
    }

    @Override
    public void execute(String command) {
        if (isFolia) {
            Runnable runnable = () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
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
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

    @Override
    public boolean isOnline(String username) {
        return plugin.getServer().getPlayer(username) != null;
    }
}
