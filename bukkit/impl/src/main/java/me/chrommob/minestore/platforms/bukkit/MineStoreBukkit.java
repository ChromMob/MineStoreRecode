package me.chrommob.minestore.platforms.bukkit;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.classloader.MineStorePlugin;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.MineStoreCommon;
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

public final class MineStoreBukkit implements MineStorePlugin {
    private final JavaPlugin plugin;
    private static MineStoreBukkit instance;
    private BukkitAudiences adventure;

    public MineStoreBukkit(JavaPlugin plugin) {
        this.plugin = plugin;
    }

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
        this.adventure = BukkitAudiences.create(plugin);
        common = new MineStoreCommon();
        // Plugin startup logic
        Registries.PLATFORM.set("bukkit");
        Registries.PLATFORM_NAME.set(Bukkit.getName());
        Registries.PLATFORM_VERSION.set(Bukkit.getVersion());
        Registries.LOGGER.set(new BukkitLogger(plugin));
        Registries.SCHEDULER.set(new BukkitScheduler(plugin));
        Registries.USER_GETTER.set(new BukkitUserGetter(plugin));
        Registries.COMMAND_EXECUTER.set(new CommandExecuterBukkit(plugin, common));
        Registries.CONFIG_FILE.set(plugin.getDataFolder().toPath().resolve("config.yml").toFile());
        Registries.PLAYER_JOIN_LISTENER.set(new BukkitPlayerEvent(plugin, common));
        new BukkitInventoryEvent(plugin, common);

        final Function<CommandSender, AbstractUser> cToA = commandSender -> (commandSender instanceof ConsoleCommandSender)
                ? new AbstractUser(new CommonConsoleUser(), commandSender)
                : Registries.USER_GETTER.get().get(((HumanEntity) commandSender).getUniqueId());
        final Function<AbstractUser, CommandSender> aToC = abstractUser -> (CommandSender) abstractUser.platformObject();

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
        Registries.COMMAND_MANAGER.set(new LegacyPaperCommandManager<>(
                /* Owning plugin */ plugin,
                /* Coordinator function */ ExecutionCoordinator.asyncCoordinator(),
                /* Command Sender -> C */ senderMapper));
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            VaultPlayerInfoProvider vaultPlayerInfoProvider = new VaultPlayerInfoProvider(plugin, common);
            if (vaultPlayerInfoProvider.isInstalled()) {
                Registries.PLAYER_INFO_PROVIDER.set(vaultPlayerInfoProvider);
            }
            VaultEconomyProvider vaultEconomyProvider = new VaultEconomyProvider(plugin, common);
            if (vaultEconomyProvider.isInstalled()) {
                Registries.PLAYER_ECONOMY_PROVIDER.set(vaultEconomyProvider);
            }
        }
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Registries.PLACE_HOLDER_PROVIDER.set(new BukkitPlaceHolderProvider(common));
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
