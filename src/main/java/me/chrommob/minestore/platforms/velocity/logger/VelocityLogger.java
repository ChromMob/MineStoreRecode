package me.chrommob.minestore.platforms.velocity.logger;

import me.chrommob.minestore.common.templates.LoggerCommon;

import java.util.logging.Logger;

public class VelocityLogger implements LoggerCommon {

    private final Logger logger;
    public VelocityLogger(Logger logger) {
        this.logger = logger;
    }
    @Override
    public void log(String message) {
        logger.info(message);
    }
}
