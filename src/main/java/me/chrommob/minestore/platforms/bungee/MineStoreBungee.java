package me.chrommob.minestore.platforms.bungee;

import co.aikar.commands.BungeeCommandManager;
import me.chrommob.minestore.platforms.bungee.config.ConfigReaderBungee;
import me.chrommob.minestore.platforms.bungee.events.PlayerJoinListenerBungee;
import me.chrommob.minestore.platforms.bungee.logger.LoggerBungee;
import me.chrommob.minestore.platforms.bungee.webCommand.CommandExecuterBungee;
import me.chrommob.minestore.common.MineStoreCommon;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("unused")
public class MineStoreBungee extends Plugin {
    @Override
    public void onEnable() {
        MineStoreCommon common = new MineStoreCommon();
        common.registerLogger(new LoggerBungee(this));
        common.registerConfigReader(new ConfigReaderBungee(this));
        common.registerCommandExecuter(new CommandExecuterBungee(this));
        common.registerPlayerJoinListener(new PlayerJoinListenerBungee(this));
        common.registerCommandManager(new BungeeCommandManager(this));
        common.init();
    }
}
