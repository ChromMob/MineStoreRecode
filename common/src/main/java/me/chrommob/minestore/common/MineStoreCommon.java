package me.chrommob.minestore.common;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.types.MineStoreDisableEvent;
import me.chrommob.minestore.api.event.types.MineStoreEnableEvent;
import me.chrommob.minestore.api.event.types.MineStoreLoadEvent;
import me.chrommob.minestore.api.event.types.MineStoreReloadEvent;
import me.chrommob.minestore.api.generic.AuthData;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.classloader.MineStoreClassLoader;
import me.chrommob.minestore.common.api.ApiHandler;
import me.chrommob.minestore.common.authHolder.AuthHolder;
import me.chrommob.minestore.common.commandGetters.WebListener;
import me.chrommob.minestore.common.commandHolder.storage.CommandDumper;
import me.chrommob.minestore.common.commandHolder.CommandStorage;
import me.chrommob.minestore.common.commandHolder.storage.NewCommandDumper;
import me.chrommob.minestore.common.commands.*;
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.common.config.PluginConfig;
import me.chrommob.minestore.common.db.DatabaseManager;
import me.chrommob.minestore.common.dumper.Dumper;
import me.chrommob.minestore.common.gui.data.GuiData;
import me.chrommob.minestore.common.gui.payment.PaymentHandler;
import me.chrommob.minestore.common.paynow.PayNowManager;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.chrommob.minestore.common.playerInfo.LuckPermsPlayerInfoProvider;
import me.chrommob.minestore.common.scheduler.MineStoreScheduler;
import me.chrommob.minestore.common.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.common.stats.StatSender;
import me.chrommob.minestore.common.subsription.SubscriptionUtil;
import me.chrommob.minestore.common.verification.VerificationManager;
import me.chrommob.minestore.common.verification.VerificationResult;
import me.chrommob.minestore.libs.me.chrommob.config.ConfigManager.ConfigManager;
import me.chrommob.minestore.libs.me.chrommob.config.ConfigManager.ConfigWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.incendo.cloud.annotations.AnnotationParser;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MineStoreCommon {
    private ConfigManager configManager;
    private PluginConfig pluginConfig;
    private DatabaseManager databaseManager;
    private MiniMessage miniMessage;
    private WebListener webListener;
    private PayNowManager payNowManager;
    private CommandStorage commandStorage;
    private CommandDumper commandDumper;
    private NewCommandDumper newCommandDumper;
    private AuthHolder authHolder;
    private GuiData guiData;
    private PlaceHolderData placeHolderData;
    private StatSender statsSender;
    private BufferedWriter debugLogWriter;
    private File debugLogFile;
    private final PaymentHandler paymentHandler = new PaymentHandler(this);
    private final MineStoreScheduler scheduler = new MineStoreScheduler();
    private final Dumper dumper = new Dumper();
    private static MineStoreVersion version;
    private VerificationManager verificationManager;

    private final MineStoreVersion subscriptionCommandSince = new MineStoreVersion(3, 0, 8);
    private final MineStoreVersion chargeBalanceSince = new MineStoreVersion(3, 2, 5);
    private final MineStoreVersion payNowSince = new MineStoreVersion(3, 6, 0);

    public MineStoreCommon() {
        Registries.CONFIG_FILE.listen(configFile -> {
            if (!configFile.getParentFile().exists()) {
                new File(configFile.getParentFile(), "lang").mkdirs();
            }
            debugLogFile = new File(configFile.getParentFile(), "debug.log");
            if (debugLogFile.exists()) {
                debugLogFile.delete();
            }
            try {
                debugLogFile.createNewFile();
                debugLogWriter = new BufferedWriter(new FileWriter(debugLogFile));
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
            configManager = new ConfigManager(configFile.getParentFile());
            pluginConfig = new PluginConfig(configManager, configFile);
            configManager.addConfig(pluginConfig);
        });
        Registries.COMMAND_MANAGER.listen(commandManager -> {
            annotationParser = new AnnotationParser(
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
        payNowManager = new PayNowManager(this);
        guiData = new GuiData(this);
        version = MineStoreVersion.getMineStoreVersion(ConfigKeys.STORE_URL.getValue());
        placeHolderData = new PlaceHolderData(this);
        SubscriptionUtil.init(this);
        if (ConfigKeys.MYSQL_KEYS.ENABLED.getValue()) {
            if (databaseManager == null) {
                databaseManager = new DatabaseManager(this);
            }
        }
        if (!reload)
            registerEssentialCommands();
        String storeUrl = ConfigKeys.STORE_URL.getValue();
        if (!storeUrl.startsWith("https://")) {
            if (storeUrl.contains("://")) {
                String[] prefix = storeUrl.split("://");
                storeUrl = "https://" + prefix[1];
            } else
                storeUrl = "https://" + storeUrl;
            ConfigKeys.STORE_URL.setValue(storeUrl);
            pluginConfig.saveConfig();
        }
        VerificationResult lastVerificationResult = verify();
        Component message = null;
        if (!lastVerificationResult.isValid()) {
            String dump = dumper().dump(readDebugLog(), this);
            message = Component.text("If you need assitance with debugging please send the following log to the support: ").append(Component.text(dump).clickEvent(ClickEvent.openUrl(dump)));
            resetDebugLog();
        }
        verificationManager = new VerificationManager(this, lastVerificationResult, message);
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
            if (ConfigKeys.MYSQL_KEYS.ENABLED.getValue()) {
                scheduler.addTask(databaseManager.updaterTask);
            }
        }
        if (Registries.COMMAND_MANAGER.get() != null && Registries.COMMAND_MANAGER.get().isCommandRegistrationAllowed()) {
            registerCommands();
        } else {
            log("Command registration is not allowed at this point. Please restart the server.");
            for (AbstractUser user : Registries.USER_GETTER.get().getAllPlayers()) {
                if (!user.commonUser().hasPermission("minestore.admin")) {
                    return;
                }
                user.commonUser().sendMessage("[MineStore] Command registration is not allowed at this point. Please restart the server.");
            }
        }
        initialized = true;
        scheduler.addTask(statsSender.mineStoreScheduledTask);
        scheduler.addTask(guiData.mineStoreScheduledTask);
        scheduler.addTask(placeHolderData.mineStoreScheduledTask);
        scheduler.addTask(webListener.mineStoreScheduledTask);
        scheduler.addTask(authHolder.removeAndPost);

        if (payNowManager != null && payNowManager.isEnabled()) {
            scheduler.run(payNowManager.initTask);
            scheduler.addTask(payNowManager.mineStoreScheduledTask);
        }

        retryCount = 0;
        new ApiHandler(new AuthData(ConfigKeys.STORE_URL.getValue(), ConfigKeys.API_KEYS.KEY.getValue()));
        new MineStoreEnableEvent().call();
    }

    private final Map<MineStoreAddon, ConfigManager> addonConfigs = new HashMap<>();
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
                if (dependencyClassLoader instanceof MineStoreClassLoader) {
                    MineStoreClassLoader mineStoreClassLoader = (MineStoreClassLoader) dependencyClassLoader;
                    if (mineStoreClassLoader.relocateAddon()) {
                        file = mineStoreClassLoader.remapAddon(file);
                    }
                }
                URL[] urls = { new URL("jar:file:" + file.getPath() + "!/") };
                URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls, dependencyClassLoader);
                Class<?> cls = urlClassLoader.loadClass(mainClass);
                if (!MineStoreAddon.class.isAssignableFrom(cls)) {
                    log("Addon " + file.getName() + " does not implement MineStoreAddon!");
                    continue;
                }
                if (addonClasses.contains(mainClass)) {
                    continue;
                }
                log("Loading addon " + name + " from " + file.getName() + "...");
                addonClasses.add(mainClass);
                File loadedAddonFolder = new File(addonFolder, name);
                loadedAddonFolder.mkdirs();
                ConfigManager configManager = new ConfigManager(loadedAddonFolder);
                configManager.reloadConfig("config");
                try {
                    MineStoreAddon addon = (MineStoreAddon) cls.getConstructor().newInstance();
                    ConfigWrapper configWrapper = new ConfigWrapper("config", addon.getConfigKeys());
                    configManager.addConfig(configWrapper);
                    addon.setConfigWrapper(configWrapper);
                    addons.add(addon);
                    addonConfigs.put(addon, configManager);
                    addon.onEnable();
                    log("Loaded addon " + addon.getName() + " from " + file.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<MineStoreAddon> getAddons() {
        return new ArrayList<>(addons);
    }

    public void notError() {
        if (verificationManager == null) {
            return;
        }
        verificationManager.safeIncrementSuccess();
    }

    long retryCount = 0;
    public void handleError() {
        if (verificationManager == null) {
            return;
        }
        verificationManager.safeIncrementError();
        if (verificationManager.getErrorRate() < 0.15) {
            debug(this.getClass(), "[VerificationManager] Error rate is: " + verificationManager.getErrorRate() + ", continuing...");
            return;
        }
        int percent = (int) (verificationManager.getErrorRate() * 100);
        log("[VerificationManager] Error rate reached: " + percent +  "%, restarting... in 16 seconds...");
        stopFeatures();
        MineStoreScheduledTask retryTask = getRetryTask();
        scheduler.addTask(retryTask);
    }

    private @NotNull MineStoreScheduledTask getRetryTask() {
        MineStoreScheduledTask retryTask = new MineStoreScheduledTask("retry", (task) -> {
            VerificationResult verificationResult = verify();
            if (!verificationResult.isValid()) {
                retryCount++;
            } else {
                retryCount = 0;
                reload();
                scheduler.removeTask(task);
                return;
            }
            if (retryCount > 5) {
                log("[VerificationManager] Too many failed attempts, stopping...");
                scheduler.removeTask(task);
                return;
            }
            long delay = (long) ((Math.pow(2, 4 + retryCount)) * 1000);
            task.delay(delay);
            log("[VerificationManager] Retrying in " + delay / 1000 + " seconds...");
        });
        retryTask.delay(16_000);
        return retryTask;
    }

    private void stopFeatures() {
        if (statsSender != null)
            scheduler.removeTask(statsSender.mineStoreScheduledTask);
        if (guiData != null)
            scheduler.removeTask(guiData.mineStoreScheduledTask);
        if (placeHolderData != null) {
            scheduler.removeTask(placeHolderData.mineStoreScheduledTask);
            placeHolderData.stop();
        }
        if (authHolder != null)
            scheduler.removeTask(authHolder.removeAndPost);
        if (databaseManager != null)
            scheduler.removeTask(databaseManager.updaterTask);
        if (webListener != null)
            scheduler.removeTask(webListener.mineStoreScheduledTask);
        if (payNowManager != null) {
            scheduler.removeTask(payNowManager.mineStoreScheduledTask);
        }
    }

    public void stop() {
        new MineStoreDisableEvent().call();
        log("Shutting down...");
        stopFeatures();
        scheduler.stop();
    }

    public void registerEssentialCommands() {
        if (Registries.COMMAND_MANAGER.get() == null) {
            return;
        }
        annotationParser.parse(new AutoSetupCommand(this));
        annotationParser.parse(new ReloadCommand(this));
        annotationParser.parse(new DumpCommand(this));
        annotationParser.parse(new SetupCommand(this));
        annotationParser.parse(new AddonCommand(this));
    }

    private AnnotationParser<?> annotationParser;

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
        if (ConfigKeys.STORE_COMMAND_KEYS.ENABLED.getValue()) {
            annotationParser.parse(new StoreCommand(this));
        }
        if (ConfigKeys.BUY_GUI_KEYS.ENABLED.getValue()) {
            annotationParser.parse(new BuyCommand(this));
        }
        if (version.requires(subscriptionCommandSince)) {
            annotationParser.parse(new SubscriptionsCommand(this));
        }
        if (version.requires(chargeBalanceSince)) {
            annotationParser.parse(new ChargeBalanceCommand(this));
        }
    }

    public void reload() {
        for (MineStoreAddon addon : addons) {
            addonConfigs.get(addon).reloadConfig("config");
        }
        new MineStoreReloadEvent().call();
        log("Reloading...");
        pluginConfig.reload();
        if (!initialized) {
            init(true);
            log("Reloaded MineStore!");
            return;
        }
        version = MineStoreVersion.getMineStoreVersion(ConfigKeys.STORE_URL.getValue());
        if (ConfigKeys.MYSQL_KEYS.ENABLED.getValue()) {
            if (databaseManager == null) {
                databaseManager = new DatabaseManager(this);
            } else {
                scheduler.removeTask(databaseManager.updaterTask);
            }
        } else {
            if (databaseManager != null) {
                scheduler.removeTask(databaseManager.updaterTask);
            }
            databaseManager = null;
        }
        stopFeatures();
        verificationManager = null;
        VerificationResult verificationResult = verify();
        Component message = null;
        if (!verificationResult.isValid()) {
            String dump = dumper().dump(readDebugLog(), this);
            if (dump == null) dump = "Log could not be uploaded due to network failure.";
            message = Component.text("If you need assitance with debugging please send the following log to the support: ").append(Component.text(dump).clickEvent(ClickEvent.openUrl(dump)));
            resetDebugLog();
        }
        verificationManager = new VerificationManager(this, verificationResult, message);
        if (!verificationManager.isValid()) {
            log("Could not reload MineStore!");
            return;
        }
        scheduler.addTask(webListener.mineStoreScheduledTask);
        scheduler.addTask(guiData.mineStoreScheduledTask);
        scheduler.addTask(placeHolderData.mineStoreScheduledTask);
        scheduler.addTask(statsSender.mineStoreScheduledTask);
        scheduler.addTask(authHolder.removeAndPost);

        scheduler.run(payNowManager.initTask);
        if (payNowManager.isEnabled()) {
            scheduler.addTask(payNowManager.mineStoreScheduledTask);
        }

        if (ConfigKeys.MYSQL_KEYS.ENABLED.getValue() && databaseManager != null) {
            scheduler.addTask(databaseManager.updaterTask);
        }
        log("Reloaded MineStore!");
        retryCount = 0;
    }

    private VerificationResult verify() {
        if (Registries.PLAYER_JOIN_LISTENER.get() == null) {
            return new VerificationResult(false, Collections.singletonList("PlayerEventListener is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (Registries.COMMAND_MANAGER.get() == null) {
            return new VerificationResult(false, Collections.singletonList("CommandManager is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (configManager == null) {
            return new VerificationResult(false, Collections.singletonList("ConfigManager is not registered."), VerificationResult.TYPE.SUPPORT);
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
        if (version.requires(payNowSince)) {
            VerificationResult payNowVerification = payNowManager.load();
            if (!payNowVerification.isValid()) {
                return payNowVerification;
            }
        }
        if (Registries.SCHEDULER.get() == null) {
            log("Scheduler is not registered.");
            return new VerificationResult(false, Collections.singletonList("Scheduler is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (ConfigKeys.MYSQL_KEYS.ENABLED.getValue()) {
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

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public void log(Component message) {
        log(PlainTextComponentSerializer.plainText().serialize(message));
    }

    public void log(String message) {
        writeDebugLog(message + "\n");
        Registries.LOGGER.get().log(message);
    }

    private int differentCharacters(String s1, String s2) {
        int count = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (i >= s2.length()) {
                return count + s1.length() - s2.length();
            }
            if (s1.charAt(i) != s2.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    private Class<?> previousClass = null;
    public void debug(Class<?> c,String message) {
        StringBuilder debugLog = new StringBuilder();
        if (previousClass == null || (!previousClass.equals(c) && differentCharacters(previousClass.getName(), c.getName()) > 2)) {
            debugLog.append("================================================================================").append("\n");
            debugLog.append(c.getName()).append("\n");
            debugLog.append("================================================================================").append("\n");
            previousClass = c;

        }
        String[] lines = message.split(", ");
        for (String line : lines) {
            debugLog.append(line).append("\n");
        }
        if (ConfigKeys.DEBUG.getValue()) {
            log(debugLog.toString());
            return;
        }
        writeDebugLog(debugLog.toString());
    }

    private void writeDebugLog(String message) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            debugLogWriter.write(time + ": " + message);
            debugLogWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debug(Class<?> c,Throwable e) {
        if (e.getMessage() != null) {
            debug(c, e.getMessage());
        }
        if (e.getStackTrace() != null) {
            debug(c, Arrays.toString(e.getStackTrace()));
        }
        if (e.getCause() != null) {
            debug(c, e.getCause().toString());
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
        if (payNowManager != null && payNowManager.isEnabled() && ConfigKeys.PAYNOW_KEYS.SHARE_IP_ON_JOIN.getValue()) {
            payNowManager.onJoin(Registries.USER_GETTER.get().get(name));
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

    public PaymentHandler paymentHandler() {
        return paymentHandler;
    }

    private void resetDebugLog() {
        try {
            debugLogWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.deleteIfExists(Paths.get(debugLogFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        debugLogFile = new File(Registries.CONFIG_FILE.get().getParentFile(), "debug.log");
        if (debugLogFile.exists()) {
            debugLogFile.delete();
        }
        try {
            debugLogFile.createNewFile();
            debugLogWriter = new BufferedWriter(new FileWriter(debugLogFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readDebugLog() {
        try {
            debugLogWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(Files.readAllBytes(Paths.get(debugLogFile.getAbsolutePath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
