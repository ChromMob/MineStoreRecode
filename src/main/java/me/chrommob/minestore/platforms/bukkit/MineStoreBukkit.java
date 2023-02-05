package me.chrommob.minestore.platforms.bukkit;

import co.aikar.commands.PaperCommandManager;
import me.chrommob.minestore.platforms.bukkit.events.BukkitPlayerJoin;
import me.chrommob.minestore.platforms.bukkit.logger.BukkitLogger;
import me.chrommob.minestore.platforms.bukkit.user.BukkitUserGetter;
import me.chrommob.minestore.platforms.bukkit.webCommand.CommandExecuterBukkit;
import me.chrommob.minestore.common.MineStoreCommon;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MineStoreBukkit extends JavaPlugin {
    private static MineStoreBukkit instance;
    private BukkitAudiences adventure;

    public @NonNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        MineStoreCommon common = new MineStoreCommon();
        // Plugin startup logic
        common.registerLogger(new BukkitLogger(this));
        common.registerUserGetter(new BukkitUserGetter(this));
        common.registerCommandExecuter(new CommandExecuterBukkit(this));
        common.setConfigLocation(getDataFolder().toPath().resolve("config.yml").toFile());
        common.registerPlayerJoinListener(new BukkitPlayerJoin(this));
        common.registerCommandManager(new PaperCommandManager(this));
        common.init();
    }

    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public static MineStoreBukkit getInstance() {
        return instance;
    }
}
