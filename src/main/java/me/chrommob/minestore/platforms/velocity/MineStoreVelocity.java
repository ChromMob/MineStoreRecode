package me.chrommob.minestore.platforms.velocity;

import co.aikar.commands.VelocityCommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.velocity.events.VelocityPlayerEvent;
import me.chrommob.minestore.platforms.velocity.logger.VelocityLogger;
import me.chrommob.minestore.platforms.velocity.scheduler.VelocityScheduler;
import me.chrommob.minestore.platforms.velocity.user.VelocityUserGetter;
import me.chrommob.minestore.platforms.velocity.webCommand.CommandExecuterVelocity;

import javax.inject.Inject;
import java.nio.file.Path;
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
        common.registerUserGetter(new VelocityUserGetter(server));
        common.registerCommandManager(new VelocityCommandManager(server, this));
        common.registerCommandExecuter(new CommandExecuterVelocity(server));
        common.setConfigLocation(dataPath.resolve("config.yml").toFile());
        common.registerPlayerJoinListener(new VelocityPlayerEvent(this, server));
        common.init();
    }

    @Subscribe
    public void onServerStop(ProxyShutdownEvent event) {
        common.stop();
    }

    public ProxyServer getServer() {
        return server;
    }
}
