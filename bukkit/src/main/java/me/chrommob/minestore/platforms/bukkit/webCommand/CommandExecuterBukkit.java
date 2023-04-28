package me.chrommob.minestore.platforms.bukkit.webCommand;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import org.bukkit.Bukkit;

public class CommandExecuterBukkit implements CommandExecuterCommon {

    private final MineStoreBukkit plugin;


    public CommandExecuterBukkit(MineStoreBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(String command) {
        MineStoreCommon.getInstance().runOnMainThread(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    @Override
    public boolean isOnline(String username) {
        return plugin.getServer().getPlayer(username) != null;
    }
}
