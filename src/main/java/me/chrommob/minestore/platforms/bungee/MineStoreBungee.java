package me.chrommob.minestore.platforms.bungee;

import co.aikar.commands.BungeeCommandManager;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.bungee.events.PlayerJoinListenerBungee;
import me.chrommob.minestore.platforms.bungee.logger.LoggerBungee;
import me.chrommob.minestore.platforms.bungee.user.BungeeUserGetter;
import me.chrommob.minestore.platforms.bungee.webCommand.CommandExecuterBungee;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

@SuppressWarnings("unused")
public class MineStoreBungee extends Plugin {
    private static MineStoreBungee instance;
    private BungeeAudiences adventure;

    public static MineStoreBungee getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.adventure = BungeeAudiences.create(this);
        MineStoreCommon common = new MineStoreCommon();
        common.registerLogger(new LoggerBungee(this));
        common.registerUserGetter(new BungeeUserGetter(this));
        common.setConfigLocation(new File(getDataFolder(), "config.yml"));
        common.registerCommandExecuter(new CommandExecuterBungee(this));
        common.registerPlayerJoinListener(new PlayerJoinListenerBungee(this));
        common.registerCommandManager(new BungeeCommandManager(this));
        common.init();
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public @NonNull BungeeAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }
}
