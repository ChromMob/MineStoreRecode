package me.chrommob.minestore.platforms.sponge;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.sponge.logger.SpongeLogger;
import me.chrommob.minestore.platforms.sponge.config.ConfigReaderSponge;
import me.chrommob.minestore.platforms.sponge.events.SpongePlayerJoin;
import me.chrommob.minestore.platforms.sponge.webCommand.CommandExecuterSponge;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;
import java.nio.file.Path;

@Plugin(id = "minestore", name = "MineStore", version = "0.1", description = "MineStore")
public class MineStoreSponge {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        MineStoreCommon common = new MineStoreCommon();
        common.registerLogger(new SpongeLogger(logger));
        common.registerCommandExecuter(new CommandExecuterSponge());
        common.registerConfigReader(new ConfigReaderSponge(defaultConfig));
        common.registerPlayerJoinListener(new SpongePlayerJoin(this));
        common.init();
    }
}
