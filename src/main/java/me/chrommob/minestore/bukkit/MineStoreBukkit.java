package me.chrommob.minestore.bukkit;

import me.chrommob.minestore.bukkit.config.ConfigReaderBukkit;
import me.chrommob.minestore.bukkit.logger.BukkitLogger;
import me.chrommob.minestore.bukkit.webCommand.CommandExecuterBukkit;
import me.chrommob.minestore.common.MineStoreCommon;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineStoreBukkit extends JavaPlugin {

    @Override
    public void onEnable() {
        new MineStoreCommon();
        // Plugin startup logic
        MineStoreCommon.getInstance().registerLogger(new BukkitLogger(this));
        MineStoreCommon.getInstance().registerCommandExecuter(new CommandExecuterBukkit(this));
        MineStoreCommon.getInstance().registerConfigReader(new ConfigReaderBukkit(this));
        MineStoreCommon.getInstance().init();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
