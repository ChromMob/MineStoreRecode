package me.chrommob.minestore.platforms.bukkit;

import co.aikar.commands.PaperCommandManager;
import me.chrommob.minestore.platforms.bukkit.config.ConfigReaderBukkit;
import me.chrommob.minestore.platforms.bukkit.events.BukkitPlayerJoin;
import me.chrommob.minestore.platforms.bukkit.logger.BukkitLogger;
import me.chrommob.minestore.platforms.bukkit.user.BukkitUserGetter;
import me.chrommob.minestore.platforms.bukkit.webCommand.CommandExecuterBukkit;
import me.chrommob.minestore.common.MineStoreCommon;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineStoreBukkit extends JavaPlugin {

    @Override
    public void onEnable() {
        MineStoreCommon common = new MineStoreCommon();
        // Plugin startup logic
        common.registerLogger(new BukkitLogger(this));
        common.registerUserGetter(new BukkitUserGetter(this));
        common.registerCommandExecuter(new CommandExecuterBukkit(this));
        common.registerConfigReader(new ConfigReaderBukkit(this));
        common.registerPlayerJoinListener(new BukkitPlayerJoin(this));
        common.registerCommandManager(new PaperCommandManager(this));
        common.init();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
