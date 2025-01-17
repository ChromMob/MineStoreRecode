package me.chrommob.minestore.platforms.bungee;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.bungee.events.PlayerEventListenerBungee;
import me.chrommob.minestore.platforms.bungee.logger.LoggerBungee;
import me.chrommob.minestore.platforms.bungee.scheduler.BungeeScheduler;
import me.chrommob.minestore.platforms.bungee.user.BungeeUserGetter;
import me.chrommob.minestore.platforms.bungee.webCommand.CommandExecuterBungee;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bungee.BungeeCommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;

import java.io.File;
import java.util.function.Function;

@SuppressWarnings("unused")
public class MineStoreBungee extends Plugin {
    private static MineStoreBungee instance;
    private BungeeAudiences adventure;
    private MineStoreCommon common;

    @Override
    public void onEnable() {
        instance = this;
        this.adventure = BungeeAudiences.create(this);
        common = new MineStoreCommon();
        Registries.PLATFORM.set("bungee");
        Registries.PLATFORM_NAME.set(getProxy().getName());
        Registries.PLATFORM_VERSION.set(getProxy().getVersion());
        Registries.LOGGER.set(new LoggerBungee(this));
        Registries.SCHEDULER.set(new BungeeScheduler(this));
        Registries.USER_GETTER.set(new BungeeUserGetter(this));
        Registries.COMMAND_EXECUTER.set(new CommandExecuterBungee(this));
        Registries.CONFIG_FILE.set(new File(getDataFolder(), "config.yml"));
        Registries.PLAYER_JOIN_LISTENER.set(new PlayerEventListenerBungee(this, common));

        final Function<CommandSender, AbstractUser> cToA = commandSender -> new AbstractUser(commandSender instanceof ProxiedPlayer ? ((ProxiedPlayer) commandSender).getUniqueId() : null, commandSender);
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
        Registries.COMMAND_MANAGER.set(new BungeeCommandManager<>(
                /* Owning plugin */ this,
                /* Execution coordinator */ ExecutionCoordinator.asyncCoordinator(),
                /* Sender mapper */ senderMapper
        ));
        common.init(false);
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        common.stop();
    }

    public static MineStoreBungee getInstance() {
        return instance;
    }

    public @NonNull BungeeAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }
}
