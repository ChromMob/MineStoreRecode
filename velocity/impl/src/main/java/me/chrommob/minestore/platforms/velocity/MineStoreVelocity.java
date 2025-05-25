package me.chrommob.minestore.platforms.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.classloader.MineStorePlugin;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.velocity.events.VelocityPlayerEvent;
import me.chrommob.minestore.platforms.velocity.logger.VelocityLogger;
import me.chrommob.minestore.platforms.velocity.scheduler.VelocityScheduler;
import me.chrommob.minestore.platforms.velocity.user.VelocityUserGetter;
import me.chrommob.minestore.platforms.velocity.webCommand.CommandExecuterVelocity;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.logging.Logger;

public class MineStoreVelocity implements MineStorePlugin {
    private final Object plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataPath;

    public MineStoreVelocity(Object plugin, ProxyServer server, Logger logger, Path dataPath) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
        this.dataPath = dataPath;
    }

    @Override
    public void onEnable() {
        common = new MineStoreCommon();
        Registries.PLATFORM.set("velocity");
        Registries.PLATFORM_NAME.set(server.getVersion().getName());
        Registries.PLATFORM_VERSION.set(server.getVersion().getVersion());
        Registries.LOGGER.set(new VelocityLogger(logger));
        Registries.SCHEDULER.set(new VelocityScheduler(this));
        Registries.USER_GETTER.set(new VelocityUserGetter(server));

        final Function<CommandSource, AbstractUser> cToA = commandSource -> commandSource instanceof Player
                ? Registries.USER_GETTER.get().get(((Player) commandSource).getUniqueId())
                : new AbstractUser(new CommonConsoleUser(), commandSource);
        final Function<AbstractUser, CommandSource> aToC = abstractUser -> (CommandSource) abstractUser.platformObject();
        final SenderMapper<CommandSource, AbstractUser> senderMapper = new SenderMapper<CommandSource, AbstractUser>() {
            @Override
            public @NotNull AbstractUser map(CommandSource base) {
                return cToA.apply(base);
            }

            @Override
            public @NotNull CommandSource reverse(AbstractUser mapped) {
                return aToC.apply(mapped);
            }
        };
        Registries.COMMAND_MANAGER.set(new VelocityCommandManager<>(
                getServer().getPluginManager().getPlugin("minestore").get(),
                getServer(),
                ExecutionCoordinator.asyncCoordinator(),
                senderMapper
        ));

        Registries.COMMAND_EXECUTER.set(new CommandExecuterVelocity(server));
        Registries.CONFIG_FILE.set(dataPath.resolve("config.yml").toFile());
        Registries.PLAYER_JOIN_LISTENER.set(new VelocityPlayerEvent(plugin, server, common));
        System.out.println("MineStore has been enabled!");
        common.init(false);
    }

    @Override
    public void onDisable() {
        common.stop();
    }

    private MineStoreCommon common;


    public ProxyServer getServer() {
        return server;
    }
}
