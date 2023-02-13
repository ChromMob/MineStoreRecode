package me.chrommob.minestore.common;

import co.aikar.commands.CommandManager;
import me.chrommob.minestore.common.authHolder.AuthHolder;
import me.chrommob.minestore.common.command.AuthCommand;
import me.chrommob.minestore.common.command.ReloadCommand;
import me.chrommob.minestore.common.command.StoreCommand;
import me.chrommob.minestore.common.interfaces.event.PlayerJoinListener;
import me.chrommob.minestore.common.interfaces.logger.LoggerCommon;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.commandGetters.WebListener;
import me.chrommob.minestore.common.commandHolder.CommandDumper;
import me.chrommob.minestore.common.commandHolder.CommandStorage;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.gui.GuiData;
import me.chrommob.minestore.common.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.common.interfaces.commands.CommandGetter;
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public class MineStoreCommon {
    private static MineStoreCommon instance;
    private ConfigReader configReader;
    private File configFile;
    private CommandExecuterCommon commandExecuterCommon;
    private LoggerCommon logger;
    private PlayerJoinListener playerJoinListener;
    private CommandManager commandManager;
    private MiniMessage miniMessage;
    private UserGetter userGetter;
    private CommandGetter commandGetter;
    private CommandStorage commandStorage;
    private CommandDumper commandDumper;
    private AuthHolder authHolder;
    private GuiData guiData;

    public MineStoreCommon() {
        instance = this;
    }

    public static MineStoreCommon getInstance() {
        return instance;
    }

    public void setConfigLocation(File configFile) {
        this.configFile = configFile;
    }

    public void registerCommandExecuter(CommandExecuterCommon commandExecuter) {
        this.commandExecuterCommon = commandExecuter;
    }

    public void registerLogger(LoggerCommon logger) {
        this.logger = logger;
    }

    public void registerPlayerJoinListener(PlayerJoinListener playerJoinListener) {
        this.playerJoinListener = playerJoinListener;
    }

    public void registerCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void registerUserGetter(UserGetter userGetter) {
        this.userGetter = userGetter;
    }

    public void init() {
        miniMessage = MiniMessage.miniMessage();
        configReader = new ConfigReader(configFile);
        commandDumper = new CommandDumper();
        commandStorage = new CommandStorage();
        authHolder = new AuthHolder(this);
        commandStorage.init();
        commandGetter = new WebListener(this);
        guiData = new GuiData();
        if (!verify()) {
            log("Your plugin is not configured correctly. Please check your config.yml");
            return;
        }
        guiData.start();
        commandGetter.start();
        registerCommands();
    }

    private boolean storeEnabled = false;
    private void registerCommands() {
        commandManager.getCommandContexts().registerIssuerAwareContext(AbstractUser.class, c -> {
            try {
                return c.getIssuer().isPlayer() ? new AbstractUser(c.getIssuer().getUniqueId()) : new AbstractUser((UUID) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new AuthCommand());
        if (configReader.get(ConfigKey.STORE_COMMAND).equals(true)) {
            storeEnabled = true;
            commandManager.registerCommand(new StoreCommand());
        }
    }

    public void reload() {
        log("Reloading...");
        configReader.reload();
        if (commandGetter.load()) {
            log("Config reloaded.");
        }
        commandGetter.start();
        if (!storeEnabled && configReader.get(ConfigKey.STORE_COMMAND).equals(true)) {
            storeEnabled = true;
            commandManager.registerCommand(new StoreCommand());
        }
    }

    private boolean verify() {
        if (commandManager == null) {
            log("CommandManager is not registered.");
            return false;
        }
        if (configReader == null) {
            log("ConfigReader is not registered.");
            return false;
        }
        if (commandExecuterCommon == null) {
            log("CommandExecuter is not registered.");
            return false;
        }
        if (logger == null) {
            log("Logger is not registered.");
            return false;
        }
        if (commandGetter == null) {
            log("CommandGetter is not registered.");
            return false;
        }
        if (userGetter == null) {
            log("UserGetter is not registered.");
            return false;
        }
        if (!guiData.load()) {
            log("GuiData is not configured correctly.");
            return false;
        }
        if (!commandGetter.load()) {
            log("Url is not configured correctly.");
            return false;
        }
        return true;
    }

    public ConfigReader configReader() {
        return configReader;
    }

    public CommandExecuterCommon commandExecuter() {
        return commandExecuterCommon;
    }

    public UserGetter userGetter() {
        return userGetter;
    }

    public void log(String message) {
        logger.log(message);
    }

    public void debug(String message) {
        if ((boolean) configReader.get(ConfigKey.DEBUG)) {
            String[] lines = message.split(", ");
            for (String line : lines) {
                try {
                    logger.log("[DEBUG] " + line);
                } catch (Exception ignored) {
                    System.out.println("[DEBUG] " + line);
                }
            }
        }
    }

    public void debug(Exception e) {
        if (e.getMessage() != null) {
            debug(e.getMessage());
        }
        if (e.getStackTrace() != null) {
            debug(Arrays.toString(e.getStackTrace()));
        }
        if (e.getCause() != null) {
            debug(e.getCause().toString());
        }
    }

    public CommandStorage commandStorage() {
        return commandStorage;
    }

    public CommandDumper commandDumper() {
        return commandDumper;
    }

    public AuthHolder authHolder() {
        return authHolder;
    }

    public File configFile() {
        return configFile;
    }

    public MiniMessage miniMessage() {
        return miniMessage;
    }
}
