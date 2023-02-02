package me.chrommob.minestore.common;

import me.chrommob.minestore.common.commandGetters.WebListener;
import me.chrommob.minestore.common.commandHolder.CommandStorage;
import me.chrommob.minestore.common.templates.CommandExecuterCommon;
import me.chrommob.minestore.common.templates.CommandGetter;
import me.chrommob.minestore.common.templates.ConfigReaderCommon;
import me.chrommob.minestore.common.templates.LoggerCommon;

public class MineStoreCommon {
    private static MineStoreCommon instance;

    public MineStoreCommon() {
        instance = this;
    }

    private ConfigReaderCommon configReader;
    public void registerConfigReader(ConfigReaderCommon configReader) {
        this.configReader = configReader;
    }

    private CommandExecuterCommon commandExecuterCommon;
    public void registerCommandExecuter(CommandExecuterCommon commandExecuter) {
        this.commandExecuterCommon = commandExecuter;
    }

    private LoggerCommon logger;
    public void registerLogger(LoggerCommon logger) {
        this.logger = logger;
    }

    private CommandGetter commandGetter;
    private final CommandStorage commandStorage = new CommandStorage();
    public void init() {
        configReader.init();
        switch (configReader.commandMode()) {
            case WEBLISTENER:
                commandGetter = new WebListener(this);
                break;
            case WEBSOCKET:
        }
        commandGetter.load();
        commandGetter.start();
    }

    public static MineStoreCommon getInstance() {
        return instance;
    }

    public ConfigReaderCommon configReader() {
        return configReader;
    }

    public CommandExecuterCommon commandExecuter() {
        return commandExecuterCommon;
    }

    public void log(String message) {
        logger.log(message);
    }

    public CommandStorage commandStorage() {
        return commandStorage;
    }
}
