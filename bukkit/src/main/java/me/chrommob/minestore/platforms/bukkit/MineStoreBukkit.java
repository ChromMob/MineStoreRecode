package me.chrommob.minestore.platforms.bukkit;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.platforms.bukkit.db.VaultEconomyProvider;
import me.chrommob.minestore.platforms.bukkit.db.VaultPlayerInfoProvider;
import me.chrommob.minestore.platforms.bukkit.events.BukkitInventoryEvent;
import me.chrommob.minestore.platforms.bukkit.events.BukkitPlayerEvent;
import me.chrommob.minestore.platforms.bukkit.logger.BukkitLogger;
import me.chrommob.minestore.platforms.bukkit.placeholder.BukkitPlaceHolderProvider;
import me.chrommob.minestore.platforms.bukkit.scheduler.BukkitScheduler;
import me.chrommob.minestore.platforms.bukkit.user.BukkitUserGetter;
import me.chrommob.minestore.platforms.bukkit.webCommand.CommandExecuterBukkit;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.util.function.Function;

public final class MineStoreBukkit extends JavaPlugin {
    private static MineStoreBukkit instance;
    private BukkitAudiences adventure;

    public BukkitAudiences adventure() {
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
        common.setPlatform("bukkit");
        common.setPlatformName(Bukkit.getName());
        common.setPlatformVersion(Bukkit.getVersion());
        common.registerLogger(new BukkitLogger(this));
        common.registerScheduler(new BukkitScheduler(this));
        common.registerUserGetter(new BukkitUserGetter(this, common));
        common.registerCommandExecuter(new CommandExecuterBukkit(this, common));
        common.setConfigLocation(getDataFolder().toPath().resolve("config.yml").toFile());
        common.registerPlayerJoinListener(new BukkitPlayerEvent(this, common));
        new BukkitInventoryEvent(this, common);

        final Function<CommandSender, AbstractUser> cToA = commandSender -> (commandSender instanceof ConsoleCommandSender)
                ? new AbstractUser((String) null, common, commandSender)
                : new AbstractUser(((HumanEntity) commandSender).getUniqueId(), common, commandSender);
        final Function<AbstractUser, CommandSender> aToC = abstractUser -> (CommandSender) abstractUser.nativeCommandSender();

        final SenderMapper<CommandSender, AbstractUser> senderMapper = new SenderMapper<CommandSender, AbstractUser>() {
            @Override
            public @NonNull AbstractUser map(@NonNull CommandSender base) {
                return cToA.apply(base);
            }

            @Override
            public @NonNull CommandSender reverse(@NonNull AbstractUser mapped) {
                return aToC.apply(mapped);
            }
        };
        try {
            common.registerCommandManager(new LegacyPaperCommandManager<>(
                    /* Owning plugin */ this,
                    /* Coordinator function */ ExecutionCoordinator.asyncCoordinator(),
                    /* Command Sender -> C */ senderMapper));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            VaultPlayerInfoProvider vaultPlayerInfoProvider = new VaultPlayerInfoProvider(this, common);
            if (vaultPlayerInfoProvider.isInstalled()) {
                common.registerPlayerInfoProvider(vaultPlayerInfoProvider);
            }
            VaultEconomyProvider vaultEconomyProvider = new VaultEconomyProvider(this, common);
            if (vaultEconomyProvider.isInstalled()) {
                common.registerPlayerEconomyProvider(vaultEconomyProvider);
            }
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            common.registerPlaceHolderProvider(new BukkitPlaceHolderProvider(this, common));
        }
        common.init(false);
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
