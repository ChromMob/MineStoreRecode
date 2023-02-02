package me.chrommob.minestore.bukkit.webCommand;

import me.chrommob.minestore.bukkit.MineStoreBukkit;
import me.chrommob.minestore.common.templates.CommandExecuterCommon;

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
