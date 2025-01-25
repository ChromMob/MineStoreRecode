package me.chrommob.minestore.platforms.bukkit.webCommand;

import me.chrommob.minestore.api.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import org.bukkit.Bukkit;

public class CommandExecuterBukkit extends CommandExecuterCommon {

    private final MineStoreBukkit plugin;
    private final MineStoreCommon pl;

    public CommandExecuterBukkit(MineStoreBukkit plugin, MineStoreCommon pl) {
        this.plugin = plugin;
        this.pl = pl;
    }

    @Override
    public void execute(String command) {
        pl.runOnMainThread(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    @Override
    public boolean isOnline(String username) {
        return plugin.getServer().getPlayer(username) != null;
    }
}
