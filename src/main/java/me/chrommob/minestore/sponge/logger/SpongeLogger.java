package me.chrommob.minestore.sponge.logger;

import me.chrommob.minestore.common.templates.LoggerCommon;
import me.chrommob.minestore.sponge.MineStoreSponge;
import org.slf4j.Logger;

public class SpongeLogger implements LoggerCommon {
    private final Logger logger;
    public SpongeLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }
}
