package me.chrommob.minestore.platforms.fabric.logger;

import org.slf4j.Logger;

import me.chrommob.minestore.common.interfaces.logger.LoggerCommon;

public class FabricLogger implements LoggerCommon {
    private final Logger logger;

    public FabricLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String message) {
        this.logger.info(message);
    }

}
