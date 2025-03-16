package me.chrommob.minestore.common;

import me.chrommob.config.ConfigManager;
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
import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.common.config.PluginConfig;
import me.chrommob.minestore.common.db.DatabaseManager;
import me.chrommob.minestore.common.dumper.Dumper;
import me.chrommob.minestore.common.gui.data.GuiData;
import me.chrommob.minestore.common.gui.payment.PaymentHandler;
import me.chrommob.minestore.common.playerInfo.LuckPermsPlayerInfoProvider;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.chrommob.minestore.common.stats.StatSender;
import me.chrommob.minestore.common.subsription.SubscriptionUtil;
import me.chrommob.minestore.common.verification.VerificationManager;
import me.chrommob.minestore.common.verification.VerificationResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.setting.ManagerSetting;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MineStoreCommon {
    private ConfigManager configManager;
    private PluginConfig pluginConfig;
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
    private BufferedWriter debugLogWriter;
    private File debugLogFile;
    private final PaymentHandler paymentHandler = new PaymentHandler(this);
    private final Dumper dumper = new Dumper();
    private static MineStoreVersion version;
    private VerificationManager verificationManager;

    private final MineStoreVersion subscriptionCommandSince = new MineStoreVersion(3, 0, 8);
    private final MineStoreVersion chargeBalanceSince = new MineStoreVersion(3, 2, 5);

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
                e.printStackTrace();
            }
            configManager = new ConfigManager(configFile.getParentFile());
            pluginConfig = new PluginConfig(configManager, configFile);
            configManager.addConfig(pluginConfig);
        });
        Registries.COMMAND_MANAGER.listen(commandManager -> {
            commandManager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
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
        guiData = new GuiData(this);
        version = MineStoreVersion.getMineStoreVersion(pluginConfig.getKey("store-url").getAsString());
        placeHolderData = new PlaceHolderData(this);
        SubscriptionUtil.init(this);
        if (pluginConfig.getKey("mysql").getKey("enabled").getAsBoolean()) {
            if (databaseManager == null) {
                databaseManager = new DatabaseManager(this);
            }
        }
        if (!reload)
            registerEssentialCommands();
        String storeUrl = pluginConfig.getKey("store-url").getAsString();
        if (!storeUrl.startsWith("https://")) {
            if (storeUrl.contains("://")) {
                String[] prefix = storeUrl.split("://");
                storeUrl = "https://" + prefix[1];
            } else
                storeUrl = "https://" + storeUrl;
            pluginConfig.getKey("store-url").setValue(storeUrl);
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
            if (pluginConfig.getKey("mysql").getKey("enabled").getAsBoolean()) {
                databaseManager.start();
            }
        }
        if (Registries.COMMAND_MANAGER.get() != null && Registries.COMMAND_MANAGER.get().isCommandRegistrationAllowed()) {
            registerCommands();
        } else {
            log("Command registration is not allowed at this point. Please restart the server.");
        }
        initialized = true;
        statsSender.start();
        guiData.start();
        placeHolderData.start();
        webListener.start();
        new MineStoreEnableEvent().call();
        new ApiHandler(new AuthData(pluginConfig.getKey("store-url").getAsString(), pluginConfig.getKey("api").getKey("key").getAsString()));
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

    public void notError() {
        if (verificationManager == null) {
            return;
        }
        verificationManager.safeIncrementSuccess();
    }

    public void handleError() {
        if (verificationManager == null) {
            return;
        }
        verificationManager.safeIncrementError();
        if (verificationManager.getErrorRate() < 0.1) {
            debug(this.getClass(), "[VerificationManager] Error rate is is: " + verificationManager.getErrorRate() + ", continuing...");
            return;
        }
        debug(this.getClass(), "[VerificationManager] Error rate is is: " + verificationManager.getErrorRate() + ", restarting...");
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
        reload();
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
        if (pluginConfig.getKey("store-command").getKey("enabled").getAsBoolean()) {
            annotationParser.parse(new StoreCommand(this));
        }
        if (pluginConfig.getKey("buy-gui").getKey("enabled").getAsBoolean()) {
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
        new MineStoreReloadEvent().call();
        log("Reloading...");
        pluginConfig.reload();
        if (!initialized) {
            init(true);
            return;
        }
        verificationManager = null;
        VerificationResult verificationResult = verify();
        Component message = null;
        if (!verificationResult.isValid()) {
            String dump = dumper().dump(readDebugLog(), this);
            message = Component.text("If you need assitance with debugging please send the following log to the support: ").append(Component.text(dump).clickEvent(ClickEvent.openUrl(dump)));
            resetDebugLog();
        }
        verificationManager = new VerificationManager(this, verificationResult, message);
        if (!verificationManager.isValid()) {
            return;
        }
        webListener.start();
        guiData.start();
        placeHolderData.start();
        statsSender.start();
        if (pluginConfig.getKey("mysql").getKey("enabled").getAsBoolean()) {
            if (databaseManager == null) {
                databaseManager = new DatabaseManager(this);
                databaseManager.start();
            } else {
                databaseManager.stop();
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
        if (Registries.SCHEDULER.get() == null) {
            log("Scheduler is not registered.");
            return new VerificationResult(false, Collections.singletonList("Scheduler is not registered."), VerificationResult.TYPE.SUPPORT);
        }
        if (pluginConfig.getKey("mysql").getKey("enabled").getAsBoolean()) {
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
        writeDebugLog(message);
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
        if (pluginConfig.getKey("debug").getAsBoolean()) {
            log(debugLog.toString());
            return;
        }
        writeDebugLog(debugLog.toString());
    }

    private void writeDebugLog(String message) {
        try {
            debugLogWriter.write(message);
            debugLogWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debug(Class<?> c,Exception e) {
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
