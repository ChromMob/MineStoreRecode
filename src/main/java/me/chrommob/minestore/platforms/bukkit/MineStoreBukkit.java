package me.chrommob.minestore.platforms.bukkit;

import co.aikar.commands.PaperCommandManager;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.bukkit.db.VaultEconomyProvider;
import me.chrommob.minestore.platforms.bukkit.db.VaultPlayerInfoProvider;
import me.chrommob.minestore.platforms.bukkit.events.BukkitInventoryEvent;
import me.chrommob.minestore.platforms.bukkit.events.BukkitPlayerEvent;
import me.chrommob.minestore.platforms.bukkit.logger.BukkitLogger;
import me.chrommob.minestore.platforms.bukkit.placeholder.BukkitPlaceHolderProvider;
import me.chrommob.minestore.platforms.bukkit.user.BukkitUserGetter;
import me.chrommob.minestore.platforms.bukkit.webCommand.CommandExecuterBukkit;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MineStoreBukkit extends JavaPlugin {
    private static MineStoreBukkit instance;
    private BukkitAudiences adventure;

    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    private MineStoreCommon common;

    @Override
    public void onEnable() {
        instance = this;
        this.adventure = BukkitAudiences.create(this);
        common = new MineStoreCommon();
        // Plugin startup logic
        common.registerLogger(new BukkitLogger(this));
        common.registerUserGetter(new BukkitUserGetter(this));
        common.registerCommandExecuter(new CommandExecuterBukkit(this));
        common.setConfigLocation(getDataFolder().toPath().resolve("config.yml").toFile());
        common.registerPlayerJoinListener(new BukkitPlayerEvent(this));
        new BukkitInventoryEvent(this);
        common.registerCommandManager(new PaperCommandManager(this));
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            VaultPlayerInfoProvider vaultPlayerInfoProvider = new VaultPlayerInfoProvider(this);
            if (vaultPlayerInfoProvider.isInstalled()) {
                common.registerPlayerInfoProvider(vaultPlayerInfoProvider);
            }
            VaultEconomyProvider vaultEconomyProvider = new VaultEconomyProvider(this);
            if (vaultEconomyProvider.isInstalled()) {
                common.registerPlayerEconomyProvider(vaultEconomyProvider);
            }
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            common.registerPlaceHolderProvider(new BukkitPlaceHolderProvider(this));
        }
        common.init();
    }

    public static MineStoreBukkit getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        common.stop();
    }
}
