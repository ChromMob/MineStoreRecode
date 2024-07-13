package me.chrommob.minestore.common;

import me.chrommob.minestore.addons.MineStoreAddon;
import me.chrommob.minestore.addons.events.types.MineStoreDisableEvent;
import me.chrommob.minestore.addons.events.types.MineStoreEnableEvent;
import me.chrommob.minestore.addons.events.types.MineStoreLoadEvent;
import me.chrommob.minestore.addons.events.types.MineStoreReloadEvent;
import me.chrommob.minestore.common.authHolder.AuthHolder;
import me.chrommob.minestore.common.command.*;
import me.chrommob.minestore.common.commandGetters.WebListener;
import me.chrommob.minestore.common.commandHolder.CommandDumper;
import me.chrommob.minestore.common.commandHolder.CommandStorage;
import me.chrommob.minestore.common.commandHolder.NewCommandDumper;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.config.MineStoreVersion;
import me.chrommob.minestore.common.db.DatabaseManager;
import me.chrommob.minestore.common.dumper.Dumper;
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
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.chrommob.minestore.common.stats.StatSender;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
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
    private NewCommandDumper newCommandDumper;
    private AuthHolder authHolder;
    private GuiData guiData;
    private PlaceHolderData placeHolderData;
    private PlayerInfoProvider playerInfoProvider;
    private PlayerEconomyProvider playerEconomyProvider;
    private CommonPlaceHolderProvider placeHolderProvider;
    private CommonScheduler scheduler;
    private StatSender statsSender;
    private final Dumper dumper = new Dumper();
    private static MineStoreVersion version;

    public void setConfigLocation(File configFile) {
        this.configFile = configFile;
        configReader = new ConfigReader(configFile, this);
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

    private final Set<MineStoreAddon> addons = new HashSet<>();

    private boolean initialized = false;

    public void init(boolean reload) {
        statsSender = new StatSender(this);
        registerAddons();
        new MineStoreLoadEvent();
        miniMessage = MiniMessage.miniMessage();
        commandDumper = new CommandDumper(this);
        newCommandDumper = new NewCommandDumper(this);
        commandStorage = new CommandStorage(this);
        authHolder = new AuthHolder(this);
        commandStorage.init();
        commandGetter = new WebListener(this);
        guiData = new GuiData(this);
        placeHolderData = new PlaceHolderData(this);
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            databaseManager = new DatabaseManager(this);
        }
        if (!reload)
            registerEssentialCommands();
        String storeUrl = (String) configReader.get(ConfigKey.STORE_URL);
        if (!storeUrl.startsWith("https://")) {
            if (storeUrl.contains("://")) {
                String[] prefix = storeUrl.split("://");
                storeUrl = "https://" + prefix[1];
            } else
                storeUrl = "https://" + storeUrl;
            configReader.set(ConfigKey.STORE_URL, storeUrl);
        }
        if (!verify()) {
            log("Your plugin is not configured correctly. Please check your config.yml");
            return;
        }
        if (!reload) {
            registerCommands();
            version = MineStoreVersion.getMineStoreVersion(this);
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
        new MineStoreEnableEvent();
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
                log("Loading addon " + mainClass + " from " + file.getName() + "...");
                URL[] urls = { new URL("jar:file:" + file.getPath() + "!/") };
                URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls, dependencyClassLoader);
                Class<?> cls = urlClassLoader.loadClass(mainClass);
                try {
                    MineStoreAddon addon = (MineStoreAddon) cls.newInstance();
                    addons.add(addon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        new MineStoreDisableEvent();
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
        if (commandManager == null) {
            return;
        }
        this.annotationParser = new AnnotationParser<>(
                /* Manager */ this.commandManager,
                /* Command sender type */ AbstractUser.class);
        this.annotationParser.parse(new AutoSetupCommand(this));
        this.annotationParser.parse(new ReloadCommand(this));
        this.annotationParser.parse(new DumpCommand(this));
    }

    private AnnotationParser<AbstractUser> annotationParser;

    private void registerCommands() {
        if (commandManager == null) {
            return;
        }
        // commandManager.commandSuggestionProcessor().registerAsyncCompletion("configKeys",
        // c -> {
        // Set<String> keys = new HashSet<>();
        // for (ConfigKey key : ConfigKey.values()) {
        // keys.add(key.name().toUpperCase());
        // }
        // return keys;
        // });
        annotationParser.parse(new AuthCommand(this));
        annotationParser.parse(new SetupCommand(this));
        annotationParser.parse(new VersionCommand(this));
        if (configReader.get(ConfigKey.STORE_ENABLED).equals(true)) {
            annotationParser.parse(new StoreCommand(this));
        }
        if (configReader.get(ConfigKey.BUY_GUI_ENABLED).equals(true)) {
            annotationParser.parse(new BuyCommand(this));
        }
    }

    public void reload() {
        new MineStoreReloadEvent();
        log("Reloading...");
        configReader.reload();
        if (!initialized) {
            init(true);
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
        if (!guiData.load() || !placeHolderData.load() || !commandGetter.load()) {
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
                LuckPermsPlayerInfoProvider luckPermsPlayerInfoProvider = new LuckPermsPlayerInfoProvider(this);
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

    public Dumper dumper() {
        return dumper;
    }

    public CommandGetter commandGetter() {
        return commandGetter;
    }

    public AnnotationParser<AbstractUser> annotationParser() {
        return annotationParser;
    }

    public void runOnMainThread(Runnable runnable) {
        scheduler.run(runnable);
    }

    public File jarFile() {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    public static MineStoreVersion version() {
        return version;
    }

    public NewCommandDumper newCommandDumper() {
        return newCommandDumper;
    }
}
