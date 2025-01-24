package me.chrommob.minestore.common;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.generic.AuthData;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.event.types.MineStoreDisableEvent;
import me.chrommob.minestore.api.event.types.MineStoreEnableEvent;
import me.chrommob.minestore.api.event.types.MineStoreLoadEvent;
import me.chrommob.minestore.api.event.types.MineStoreReloadEvent;
import me.chrommob.minestore.common.api.ApiHandler;
import me.chrommob.minestore.common.authHolder.AuthHolder;
import me.chrommob.minestore.common.command.*;
import me.chrommob.minestore.common.commandGetters.WebListener;
import me.chrommob.minestore.common.commandHolder.CommandDumper;
import me.chrommob.minestore.common.commandHolder.CommandStorage;
import me.chrommob.minestore.common.commandHolder.NewCommandDumper;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.common.db.DatabaseManager;
import me.chrommob.minestore.common.dumper.Dumper;
import me.chrommob.minestore.common.gui.data.GuiData;
import me.chrommob.minestore.common.playerInfo.LuckPermsPlayerInfoProvider;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.chrommob.minestore.common.stats.StatSender;
import me.chrommob.minestore.common.subsription.SubscriptionUtil;
import me.chrommob.minestore.common.verification.VerificationManager;
import me.chrommob.minestore.common.verification.VerificationResult;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.setting.ManagerSetting;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MineStoreCommon {
    private ConfigReader configReader;
    private DatabaseManager databaseManager;
    private MiniMessage miniMessage;
    private WebListener webListener;
    private CommandStorage commandStorage;
    private CommandDumper commandDumper;
    private NewCommandDumper newCommandDumper;
    private AuthHolder authHolder;
    private GuiData guiData;
    private PlaceHolderData placeHolderData;
    private StatSender statsSender;
    private final Dumper dumper = new Dumper();
    private static MineStoreVersion version;
    private VerificationManager verificationManager;

    public MineStoreCommon() {
        Registries.CONFIG_FILE.listen(configFile -> configReader = new ConfigReader(configFile, this));
        Registries.COMMAND_MANAGER.listen(commandManager -> {
            commandManager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
            annotationParser = new AnnotationParser<>(
                    /* Manager */ Registries.COMMAND_MANAGER.get(),
                    /* Command sender type */ AbstractUser.class);
        });
    }

    private final Set<MineStoreAddon> addons = new HashSet<>();
    private final Set<String> addonClasses = new HashSet<>();

    private boolean initialized = false;

    public void init(boolean reload) {
        registerAddons();
        statsSender = new StatSender(this);
        new MineStoreLoadEvent().call();
        miniMessage = MiniMessage.miniMessage();
        commandDumper = new CommandDumper(this);
        newCommandDumper = new NewCommandDumper(this);
        commandStorage = new CommandStorage(this);
        authHolder = new AuthHolder(this);
        commandStorage.init();
        webListener = new WebListener(this);
        guiData = new GuiData(this);
        version = new MineStoreVersion("3.2.5");
        //version = MineStoreVersion.getMineStoreVersion((String) configReader.get(ConfigKey.STORE_URL));
        placeHolderData = new PlaceHolderData(this);
        SubscriptionUtil.init(this);
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            if (databaseManager == null) {
                databaseManager = new DatabaseManager(this);
            }
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
        VerificationResult lastVerificationResult = verify();
        verificationManager = new VerificationManager(lastVerificationResult);
        if (!verificationManager.isValid()) {
            return;
        }
        if (!reload) {
            if (Registries.PLACE_HOLDER_PROVIDER.get() != null) {
                boolean init = Registries.PLACE_HOLDER_PROVIDER.get().init();
                if (!init) {
                    log("Failed to register PlaceHolderAPI expansion!");
                }
            }
            if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
                databaseManager.start();
            }
        }
        registerCommands();
        initialized = true;
        statsSender.start();
        guiData.start();
        placeHolderData.start();
        webListener.start();
        new MineStoreEnableEvent().call();
        new ApiHandler(new AuthData((String) configReader.get(ConfigKey.STORE_URL), (String) configReader.get(ConfigKey.API_KEY)));
    }

    private void registerAddons() {
        File addonFolder = new File(Registries.CONFIG_FILE.get().getParentFile(), "addons");
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
            try (ZipFile zipFile = new ZipFile(file)) {
                if (zipFile.getEntry("addon.yml") == null) {
                    log("Addon " + file.getName() + " does not contain addon.yml!");
                    continue;
                }
                ZipEntry zipEntry = zipFile.getEntry("addon.yml");
                Yaml yaml = new Yaml();
                HashMap<String, String> object = yaml.load(zipFile.getInputStream(zipEntry));
                String mainClass = object.get("main-class");
                String name = object.get("name");
                if (mainClass == null) {
                    log("Addon " + file.getName() + " does not contain main-class attribute!");
                    continue;
                }
                if (name == null) {
                    log("Addon " + file.getName() + " does not contain name attribute!");
                    continue;
                }
                ClassLoader dependencyClassLoader = getClass().getClassLoader();
                log("Loading addon " + name + " from " + file.getName() + "...");
                URL[] urls = { new URL("jar:file:" + file.getPath() + "!/") };
                URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls, dependencyClassLoader);
                Class<?> cls = urlClassLoader.loadClass(mainClass);
                if (!MineStoreAddon.class.isAssignableFrom(cls)) {
                    log("Addon " + file.getName() + " does not implement MineStoreAddon!");
                    continue;
                }
                if (addonClasses.contains(mainClass)) {
                    log("Addon " + file.getName() + " is already loaded!");
                    continue;
                }
                addonClasses.add(mainClass);
                try {
                    MineStoreAddon addon = (MineStoreAddon) cls.getConstructor().newInstance();
                    addons.add(addon);
                    log("Loaded addon " + addon.getName() + " from " + file.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getAddons() {
        return addons.stream().map(MineStoreAddon::getName).collect(Collectors.joining(", "));
    }

    public void stop() {
        new MineStoreDisableEvent().call();
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
        if (webListener != null)
            webListener.stop();
    }

    private void registerEssentialCommands() {
        if (Registries.COMMAND_MANAGER.get() == null) {
            return;
        }
        annotationParser.parse(new AutoSetupCommand(this));
        annotationParser.parse(new ReloadCommand(this));
        annotationParser.parse(new DumpCommand(this));
        annotationParser.parse(new SetupCommand(this));
        annotationParser.parse(new AddonCommand(this));
    }

    private AnnotationParser annotationParser;

    private void registerCommands() {
        if (Registries.COMMAND_MANAGER.get() == null) {
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
        annotationParser.parse(new VersionCommand());
        if (configReader.get(ConfigKey.STORE_ENABLED).equals(true)) {
            annotationParser.parse(new StoreCommand(this));
        }
        if (configReader.get(ConfigKey.BUY_GUI_ENABLED).equals(true)) {
            annotationParser.parse(new BuyCommand(this));
        }
        if (version.requires(new MineStoreVersion(3, 0, 8))) {
            annotationParser.parse(new SubscriptionsCommand(this));
        }
        if (version.requires(new MineStoreVersion(3, 2, 5))) {
            annotationParser.parse(new ChargeBalanceCommand(this));
        }
    }

    public void reload() {
        new MineStoreReloadEvent().call();
        log("Reloading...");
        configReader.reload();
        if (!initialized) {
            init(true);
            return;
        }
        if (webListener.load().isValid()) {
            log("Config reloaded.");
            webListener.start();
        }
        if (guiData.load().isValid()) {
            log("GuiData reloaded.");
            guiData.start();
        }
        if (placeHolderData.load().isValid()) {
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
                if (!databaseManager().load().isValid()) {
                    log("Failed to initialize database.");
                    return;
                }
                databaseManager.start();
            } else {
                databaseManager.stop();
                if (!databaseManager().load().isValid()) {
                    log("Failed to reload database.");
                    return;
                }
                databaseManager.start();
            }
        }
    }

    private VerificationResult verify() {
        if (Registries.PLAYER_JOIN_LISTENER.get() == null) {
            return new VerificationResult(false, Collections.singletonList("PlayerEventListener is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (Registries.COMMAND_MANAGER.get() == null) {
            return new VerificationResult(false, Collections.singletonList("CommandManager is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (configReader == null) {
            return new VerificationResult(false, Collections.singletonList("ConfigReader is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (Registries.COMMAND_EXECUTER.get() == null) {
            return new VerificationResult(false, Collections.singletonList("CommandExecuter is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (Registries.LOGGER.get() == null) {
            return new VerificationResult(false, Collections.singletonList("Logger is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (webListener == null) {
            return new VerificationResult(false, Collections.singletonList("CommandGetter is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (Registries.USER_GETTER.get() == null) {
            return new VerificationResult(false, Collections.singletonList("UserGetter is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        VerificationResult guiDataVerification = guiData.load();
        if (!guiDataVerification.isValid()) {
            return guiDataVerification;
        }
        VerificationResult placeHolderDataVerification = placeHolderData.load();
        if (!placeHolderDataVerification.isValid()) {
            return placeHolderDataVerification;
        }
        VerificationResult webListenerVerification = webListener.load();
        if (!webListenerVerification.isValid()) {
            return webListenerVerification;
        }
        if (Registries.SCHEDULER.get() == null) {
            log("Scheduler is not registered.");
            return new VerificationResult(false, Collections.singletonList("Scheduler is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (configReader.get(ConfigKey.MYSQL_ENABLED).equals(true)) {
            if (databaseManager == null) {
                return new VerificationResult(false, Collections.singletonList("DatabaseManager is not registered."), VerificationResult.TYPE.SUPPORT);
            }
            VerificationResult databaseVerification = databaseManager.load();
            if (!databaseVerification.isValid()) {
                return databaseVerification;
            }
            if (Registries.PLAYER_INFO_PROVIDER.get() == null) {
                LuckPermsPlayerInfoProvider luckPermsPlayerInfoProvider = new LuckPermsPlayerInfoProvider(this);
                if (luckPermsPlayerInfoProvider.isInstalled()) {
                    Registries.PLAYER_INFO_PROVIDER.set(luckPermsPlayerInfoProvider);
                }
            }
        }
        return VerificationResult.valid();
    }

    public ConfigReader configReader() {
        return configReader;
    }

    public void log(String message) {
        Registries.LOGGER.get().log(message);
    }

    public void debug(String message) {
        if ((boolean) configReader.get(ConfigKey.DEBUG)) {
            String[] lines = message.split(", ");
            for (String line : lines) {
                try {
                    Registries.LOGGER.get().log("[DEBUG] " + line);
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
        if (verificationManager != null) {
            verificationManager.onJoin(name);
        }
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

    public CommandStorage commandStorage() {
        return commandStorage;
    }

    public CommandDumper commandDumper() {
        return commandDumper;
    }

    public AuthHolder authHolder() {
        return authHolder;
    }

    public MiniMessage miniMessage() {
        return miniMessage;
    }

    public DatabaseManager databaseManager() {
        return databaseManager;
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

    public WebListener webListener() {
        return webListener;
    }

    public void runOnMainThread(Runnable runnable) {
        Registries.SCHEDULER.get().run(runnable);
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
