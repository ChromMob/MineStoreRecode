package me.chrommob.minestore.platforms.bungee;

import cloud.commandframework.SenderMapper;
import cloud.commandframework.bungee.BungeeCommandManager;
import cloud.commandframework.execution.ExecutionCoordinator;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
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
        common.setPlatform("bungee");
        common.setPlatformName(getProxy().getName());
        common.setPlatformVersion(getProxy().getVersion());
        common.registerLogger(new LoggerBungee(this));
        common.registerScheduler(new BungeeScheduler(this));
        common.registerUserGetter(new BungeeUserGetter(this));
        common.setConfigLocation(new File(getDataFolder(), "config.yml"));
        common.registerCommandExecuter(new CommandExecuterBungee(this));
        common.registerPlayerJoinListener(new PlayerEventListenerBungee(this));

        final Function<CommandSender, AbstractUser> cToA = commandSender -> new AbstractUser(commandSender instanceof ProxiedPlayer ? ((ProxiedPlayer) commandSender).getUniqueId() : null);
        final Function<AbstractUser, CommandSender> aToC = abstractUser -> abstractUser.user() instanceof CommonConsoleUser ? getProxy().getConsole() : getProxy().getPlayer(abstractUser.user().getName());
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
        common.registerCommandManager(new BungeeCommandManager<>(
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
