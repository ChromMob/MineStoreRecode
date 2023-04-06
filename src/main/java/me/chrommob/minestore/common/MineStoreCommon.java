package me.chrommob.minestore.common;

import co.aikar.commands.CommandManager;
import com.google.common.math.Stats;
import me.chrommob.minestore.common.addons.MineStoreAddon;
import me.chrommob.minestore.common.addons.MineStoreEventSender;
import me.chrommob.minestore.common.addons.MineStoreListener;
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
import me.chrommob.minestore.common.interfaces.commands.CommandStorageInterface;
import me.chrommob.minestore.common.interfaces.economyInfo.DefaultPlayerEconomyProvider;
import me.chrommob.minestore.common.interfaces.economyInfo.PlayerEconomyProvider;
import me.chrommob.minestore.common.interfaces.event.PlayerEventListener;
import me.chrommob.minestore.common.interfaces.logger.LoggerCommon;
import me.chrommob.minestore.common.interfaces.placeholder.CommonPlaceHolderProvider;
import me.chrommob.minestore.common.interfaces.playerInfo.DefaultPlayerInfoProvider;
import me.chrommob.minestore.common.interfaces.playerInfo.PlayerInfoProvider;
import me.chrommob.minestore.common.interfaces.playerInfo.implementation.LuckPermsPlayerInfoProvider;
import me.chrommob.minestore.common.interfaces.scheduler.CommonScheduler;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.chrommob.minestore.common.stats.StatSender;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
    private CommandStorageInterface commandStorage;
    private CommandDumper commandDumper;
    private AuthHolder authHolder;
    private GuiData guiData;
    private PlaceHolderData placeHolderData;
    private PlayerInfoProvider playerInfoProvider;
    private PlayerEconomyProvider playerEconomyProvider;
    private CommonPlaceHolderProvider placeHolderProvider;
    private CommonScheduler scheduler;
    private StatSender statsSender;

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

    private String platformType;
    public void setPlatform(String platform) {
        this.platformType = platform;
    }
    private String platformName;
    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }
    private String platformVersion;
    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getPlatform() {
        return platformType;
    }
    public String getPlatformName() {
        return platformName;
    }
    public String getPlatformVersion() {
        return platformVersion;
    }

    public void registerCommandExecuter(CommandExecuterCommon commandExecuter) {
        this.commandExecuterCommon = commandExecuter;
    }

    public void registerScheduler(CommonScheduler scheduler) {
        this.scheduler = scheduler;
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

    public void registerPlaceHolderProvider(CommonPlaceHolderProvider placeHolderProvider) {
        this.placeHolderProvider = placeHolderProvider;
    }

    @SuppressWarnings("unused")
    public void overrideCommandStorage(CommandStorageInterface commandStorage) {
        this.commandStorage = commandStorage;
    }

    private Set<MineStoreAddon> addons = new HashSet<>();
    private Set<MineStoreListener> listeners = new HashSet<>();
    @SuppressWarnings("unused")
    public void registerListener(MineStoreListener listener) {
        listeners.add(listener);
    }
    public Set<MineStoreListener> getListeners() {
        return listeners;
    }
    private MineStoreEventSender eventSender;
    private boolean initialized = false;
    public void init() {
        statsSender = new StatSender(this);
        eventSender = new MineStoreEventSender(this);
        registerAddons();
        for (MineStoreAddon addon : addons) {
            addon.onLoad();
        }
        miniMessage = MiniMessage.miniMessage();
        commandDumper = new CommandDumper();
        commandStorage = new CommandStorage();
        authHolder = new AuthHolder(this);
        commandStorage.init();
        commandGetter = new WebListener(this);
        guiData = new GuiData();
        placeHolderData = new PlaceHolderData();
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            databaseManager = new DatabaseManager(this);
        }
        registerEssentialCommands();
        if (!verify()) {
            log("Your plugin is not configured correctly. Please check your config.yml");
            return;
        }
        initialized = true;
        statsSender.start();
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            databaseManager.start();
        }
        if (placeHolderProvider != null) {
            placeHolderProvider.init();
        }
        guiData.start();
        placeHolderData.start();
        commandGetter.start();
        registerCommands();
        for (MineStoreAddon addon : addons) {
            addon.onEnable();
        }
    }

    private void registerAddons() {
        File addonFolder = new File(configFile.getParentFile(), "addons");
        if (!addonFolder.exists()) {
            addonFolder.mkdir();
        }
        if (addonFolder.listFiles() == null) {
            return;
        }
        for (File file : addonFolder.listFiles()) {
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            try {
                ZipFile zipFile = new ZipFile(file);
                if (zipFile.getEntry("addon.yml") == null) {
                    log("Addon " + file.getName() + " does not contain addon.yml!");
                    continue;
                }
                ZipEntry zipEntry = zipFile.getEntry("addon.yml");
                Yaml yaml = new Yaml();
                HashMap<String, String> object = yaml.load(zipFile.getInputStream(zipEntry));
                String mainClass = object.get("main-class");
                ClassLoader dependencyClassLoader = getClass().getClassLoader();
                log("Loading addon " + mainClass + " from " + file.getName() + "..." );
                URL[] urls = { new URL("jar:file:" + file.getPath() + "!/") };
                URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls, dependencyClassLoader);
                Class<?> cls = urlClassLoader.loadClass(mainClass);
                MineStoreAddon addon = (MineStoreAddon) cls.newInstance();
                addons.add(addon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        for (MineStoreAddon addon : addons) {
            addon.onDisable();
        }
        log("Shutting down...");
        if (statsSender != null)
            statsSender.stop();
        if (guiData != null)
            guiData.stop();
        if (placeHolderData != null)
            placeHolderData.stop();
        if (authHolder != null)
            authHolder.stop();
        if (databaseManager != null)
            databaseManager.stop();
        if (commandGetter != null)
            commandGetter.stop();
    }

    private void registerEssentialCommands() {
        commandManager.getCommandContexts().registerIssuerAwareContext(AbstractUser.class, c -> {
            try {
                return c.getIssuer().isPlayer() ? new AbstractUser(c.getIssuer().getUniqueId()) : new AbstractUser((UUID) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        commandManager.registerCommand(new AutoSetupCommand());
        commandManager.registerCommand(new ReloadCommand());
    }

    private boolean storeEnabled = false;
    private boolean buyEnabled = false;
    private void registerCommands() {
        commandManager.getCommandCompletions().registerAsyncCompletion("configKeys", c -> {
            Set<String> keys = new HashSet<>();
            for (ConfigKey key : ConfigKey.values()) {
                keys.add(key.name().toUpperCase());
            }
            return keys;
        });
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
        for (MineStoreAddon addon : addons) {
            addon.onReload();
        }
        log("Reloading...");
        configReader.reload();
        if (!initialized) {
            init();
            return;
        }
        if (commandGetter.load()) {
            log("Config reloaded.");
            commandGetter.start();
        }
        if (guiData.load()) {
            log("GuiData reloaded.");
            guiData.start();
        }
        if (placeHolderData.load()) {
            log("PlaceHolderData reloaded.");
            placeHolderData.start();
        }
        if (!storeEnabled && configReader.get(ConfigKey.STORE_ENABLED).equals(true)) {
            storeEnabled = true;
            commandManager.registerCommand(new StoreCommand());
        }
        if (statsSender != null) {
            statsSender.stop();
            statsSender.start();
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
        if (!placeHolderData.load()) {
            log("PlaceHolderData is not configured correctly.");
            return false;
        }
        if (!commandGetter.load()) {
            log("Url is not configured correctly.");
            return false;
        }
        if (scheduler == null) {
            log("Scheduler is not registered.");
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
        debug(e.getClass().getTypeName());
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
        if (!initialized) {
            return;
        }
        commandStorage.onPlayerJoin(name);
        if (databaseManager != null) {
            databaseManager.onPlayerJoin(name);
        }
    }

    public void onPlayerQuit(String name) {
        if (!initialized) {
            return;
        }
        if (databaseManager != null) {
            databaseManager.onPlayerQuit(name);
        }
    }

    public CommandStorageInterface commandStorage() {
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

    public PlaceHolderData placeHolderData() {
        return placeHolderData;
    }

    public MineStoreEventSender listener() {
        return eventSender;
    }

    public CommandManager commandManager() {
        return commandManager;
    }

    public void runOnMainThread(Runnable runnable, CommonUser user) {
        scheduler.run(runnable);
    }
}
