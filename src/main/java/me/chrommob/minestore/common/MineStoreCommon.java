package me.chrommob.minestore.common;

import co.aikar.commands.CommandManager;
import me.chrommob.minestore.common.authHolder.AuthHolder;
import me.chrommob.minestore.common.command.*;
import me.chrommob.minestore.common.commandGetters.WebListener;
import me.chrommob.minestore.common.commandHolder.CommandDumper;
import me.chrommob.minestore.common.commandHolder.CommandStorage;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.db.DatabaseManager;
import me.chrommob.minestore.common.gui.data.GuiData;
import me.chrommob.minestore.common.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.common.interfaces.commands.CommandGetter;
import me.chrommob.minestore.common.interfaces.economyInfo.DefaultPlayerEconomyProvider;
import me.chrommob.minestore.common.interfaces.economyInfo.PlayerEconomyProvider;
import me.chrommob.minestore.common.interfaces.event.PlayerEventListener;
import me.chrommob.minestore.common.interfaces.logger.LoggerCommon;
import me.chrommob.minestore.common.interfaces.playerInfo.DefaultPlayerInfoProvider;
import me.chrommob.minestore.common.interfaces.playerInfo.PlayerInfoProvider;
import me.chrommob.minestore.common.interfaces.playerInfo.implementation.LuckPermsPlayerInfoProvider;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MineStoreCommon {
    private static MineStoreCommon instance;
    private ConfigReader configReader;
    private File configFile;
    private CommandExecuterCommon commandExecuterCommon;
    private LoggerCommon logger;
    private PlayerEventListener playerEventListener;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;
    private MiniMessage miniMessage;
    private UserGetter userGetter;
    private CommandGetter commandGetter;
    private CommandStorage commandStorage;
    private CommandDumper commandDumper;
    private AuthHolder authHolder;
    private GuiData guiData;
    private PlayerInfoProvider playerInfoProvider;
    private PlayerEconomyProvider playerEconomyProvider;

    public MineStoreCommon() {
        instance = this;
    }

    public static MineStoreCommon getInstance() {
        return instance;
    }

    public void setConfigLocation(File configFile) {
        this.configFile = configFile;
        configReader = new ConfigReader(configFile);
    }

    public void registerCommandExecuter(CommandExecuterCommon commandExecuter) {
        this.commandExecuterCommon = commandExecuter;
    }

    public void registerLogger(LoggerCommon logger) {
        this.logger = logger;
    }

    public void registerPlayerJoinListener(PlayerEventListener playerEventListener) {
        this.playerEventListener = playerEventListener;
    }

    public void registerCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void registerUserGetter(UserGetter userGetter) {
        this.userGetter = userGetter;
    }

    public void registerPlayerInfoProvider(PlayerInfoProvider playerInfoProvider) {
        this.playerInfoProvider = playerInfoProvider;
    }

    public void registerPlayerEconomyProvider(PlayerEconomyProvider playerEconomyProvider) {
        this.playerEconomyProvider = playerEconomyProvider;
    }

    public void init() {
        miniMessage = MiniMessage.miniMessage();
        commandDumper = new CommandDumper();
        commandStorage = new CommandStorage();
        authHolder = new AuthHolder(this);
        commandStorage.init();
        commandGetter = new WebListener(this);
        guiData = new GuiData();
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            databaseManager = new DatabaseManager(this);
        }
        if (!verify()) {
            log("Your plugin is not configured correctly. Please check your config.yml");
            return;
        }
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            databaseManager.start();
        }
        guiData.start();
        commandGetter.start();
        registerCommands();
    }

    public void stop() {
        log("Shutting down...");
        if (guiData != null)
            guiData.stop();
        if (authHolder != null)
            authHolder.stop();
        if (databaseManager != null)
            databaseManager.stop();
        if (commandGetter != null)
            commandGetter.stop();
    }

    private boolean storeEnabled = false;
    private boolean buyEnabled = false;
    private void registerCommands() {
        commandManager.getCommandContexts().registerIssuerAwareContext(AbstractUser.class, c -> {
            try {
                return c.getIssuer().isPlayer() ? new AbstractUser(c.getIssuer().getUniqueId()) : new AbstractUser((UUID) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        commandManager.getCommandCompletions().registerAsyncCompletion("configKeys", c -> {
            Set<String> keys = new HashSet<>();
            for (ConfigKey key : ConfigKey.values()) {
                keys.add(key.name().toUpperCase());
            }
            return keys;
        });
        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new AuthCommand());
        commandManager.registerCommand(new SetupCommand(this));
        if (!storeEnabled && configReader.get(ConfigKey.STORE_ENABLED).equals(true)) {
            storeEnabled = true;
            commandManager.registerCommand(new StoreCommand());
        }
        if (!buyEnabled && configReader.get(ConfigKey.BUY_GUI_ENABLED).equals(true)) {
            buyEnabled = true;
            commandManager.registerCommand(new BuyCommand());
        }
    }

    public void reload() {
        log("Reloading...");
        configReader.reload();
        if (commandGetter.load()) {
            log("Config reloaded.");
            commandGetter.start();
        }
        if (guiData.load()) {
            log("GuiData reloaded.");
            guiData.start();
        }
        if (!storeEnabled && configReader.get(ConfigKey.STORE_ENABLED).equals(true)) {
            storeEnabled = true;
            commandManager.registerCommand(new StoreCommand());
        }
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            if (databaseManager == null) {
                databaseManager = new DatabaseManager(this);
                if (!databaseManager().load()) {
                    log("Failed to initialize database.");
                    return;
                }
                databaseManager.start();
            } else {
                if (!databaseManager().load()) {
                    log("Failed to reload database.");
                    return;
                }
                databaseManager.start();
            }
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
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            if (databaseManager == null) {
                log("DatabaseManager is not registered.");
                return false;
            }
            if (!databaseManager.load()) {
                log("Database is not configured correctly.");
                return false;
            }
            if (playerInfoProvider == null) {
                LuckPermsPlayerInfoProvider luckPermsPlayerInfoProvider = new LuckPermsPlayerInfoProvider();
                if (luckPermsPlayerInfoProvider.isInstalled()) {
                    playerInfoProvider = luckPermsPlayerInfoProvider;
                } else {
                    log("PlayerInfoProvider is not registered.");
                    playerInfoProvider = new DefaultPlayerInfoProvider();
                }
            }
            if (playerEconomyProvider == null) {
                playerEconomyProvider = new DefaultPlayerEconomyProvider();
            }
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

    public void onPlayerJoin(String name) {
        commandStorage.onPlayerJoin(name);
        if (databaseManager != null) {
            databaseManager.onPlayerJoin(name);
        }
    }

    public void onPlayerQuit(String name) {
        if (databaseManager != null) {
            databaseManager.onPlayerQuit(name);
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

    public DatabaseManager databaseManager() {
        return databaseManager;
    }

    public PlayerInfoProvider playerInfoProvider() {
        return playerInfoProvider;
    }

    public PlayerEconomyProvider playerEconomyProvider() {
        return playerEconomyProvider;
    }

    public GuiData guiData() {
        return guiData;
    }
}
