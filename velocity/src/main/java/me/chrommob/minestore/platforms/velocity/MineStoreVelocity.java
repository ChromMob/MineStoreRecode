package me.chrommob.minestore.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
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

@Plugin(id = "minestore", name = "MineStore", version = "0.1", description = "MineStore plugin for Velocity", authors = {"chrommob"})
public class MineStoreVelocity {

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer server;
    @Inject
    @DataDirectory
    private Path dataPath;

    private MineStoreCommon common;

    @Subscribe
    private void onProxyInitialization(ProxyInitializeEvent event) {
        common = new MineStoreCommon();
        common.setPlatform("velocity");
        common.setPlatformName(server.getVersion().getName());
        common.setPlatformVersion(server.getVersion().getVersion());
        common.registerLogger(new VelocityLogger(logger));
        common.registerScheduler(new VelocityScheduler(this));
        common.registerUserGetter(new VelocityUserGetter(server, common));

        final Function<CommandSource, AbstractUser> cToA = commandSource -> new AbstractUser(commandSource instanceof Player ? ((Player) commandSource).getUniqueId() : null, common, commandSource);
        final Function<AbstractUser, CommandSource> aToC = abstractUser -> (CommandSource) abstractUser.nativeCommandSender();
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
        common.registerCommandManager(new VelocityCommandManager<>(
                getServer().getPluginManager().getPlugin("minestore").get(),
                getServer(),
                ExecutionCoordinator.asyncCoordinator(),
                senderMapper
        ));

        common.registerCommandExecuter(new CommandExecuterVelocity(server));
        common.setConfigLocation(dataPath.resolve("config.yml").toFile());
        common.registerPlayerJoinListener(new VelocityPlayerEvent(this, server, common));
        System.out.println("MineStore has been enabled!");
        common.init(false);
    }

    @Subscribe
    public void onServerStop(ProxyShutdownEvent event) {
        common.stop();
    }

    public ProxyServer getServer() {
        return server;
    }
}
