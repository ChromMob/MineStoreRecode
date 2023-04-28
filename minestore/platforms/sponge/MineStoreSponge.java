package me.chrommob.minestore.platforms.sponge;

import co.aikar.commands.SpongeCommandManager;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.sponge.events.SpongePlayerEvent;
import me.chrommob.minestore.platforms.sponge.logger.SpongeLogger;
import me.chrommob.minestore.platforms.sponge.scheduler.SpongeScheduler;
import me.chrommob.minestore.platforms.sponge.user.SpongeUserGetter;
import me.chrommob.minestore.platforms.sponge.webCommand.CommandExecuterSponge;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;

@Plugin(id = "minestore", name = "MineStore", version = "0.1", description = "MineStore")
public class MineStoreSponge {

    private static MineStoreSponge instance;
    @Inject
    private Logger logger;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;
    @Inject
    private PluginContainer pluginContainer;
    @Inject
    private SpongeAudiences adventure;

    private MineStoreCommon common;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        instance = this;
        common = new MineStoreCommon();
        common.setPlatform("sponge");
        common.setPlatformName(Sponge.getGame().getPlatform().getType().name());
        common.setPlatformVersion(Sponge.getGame().getPlatform().getMinecraftVersion().getName());
        common.registerLogger(new SpongeLogger(logger));
        common.registerScheduler(new SpongeScheduler(this));
        common.registerUserGetter(new SpongeUserGetter());
        common.registerCommandManager(new SpongeCommandManager(pluginContainer));
        common.registerCommandExecuter(new CommandExecuterSponge());
        common.setConfigLocation(new File(defaultConfig.toFile(), "config.yml"));
        common.registerPlayerJoinListener(new SpongePlayerEvent(this));
        common.init();
    }

    @Listener
    public void onServerStop(GameStoppedEvent event) {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        common.stop();
    }

    public static MineStoreSponge getInstance() {
        return instance;
    }

    public @NonNull SpongeAudiences adventure() {
        return this.adventure;
    }
}
