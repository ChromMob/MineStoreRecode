package me.chrommob.minestore.platforms.bukkit.webCommand;

import me.chrommob.minestore.common.interfaces.CommandExecuterCommon;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;

public class CommandExecuterBukkit implements CommandExecuterCommon {

    private final MineStoreBukkit plugin;

    public CommandExecuterBukkit(MineStoreBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(String command) {
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command));
    }

    @Override
    public boolean isOnline(String username) {
        return plugin.getServer().getPlayer(username) != null;
    }
}
