package me.chrommob.minestore.platforms.fabric.logger;

import me.chrommob.minestore.api.interfaces.logger.LoggerCommon;
import org.slf4j.Logger;


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
