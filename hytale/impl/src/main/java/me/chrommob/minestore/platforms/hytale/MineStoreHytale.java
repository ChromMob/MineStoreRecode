package me.chrommob.minestore.platforms.hytale;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.classloader.MineStorePlugin;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.hytale.command.HytaleCommandExecuter;
import me.chrommob.minestore.platforms.hytale.events.HytalePlayerJoinListener;
import me.chrommob.minestore.platforms.hytale.user.HytaleUserGetter;

import java.net.InetSocketAddress;
import java.util.logging.Level;

public class MineStoreHytale implements MineStorePlugin {
    private final JavaPlugin plugin;
    public MineStoreHytale(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        MineStoreCommon common = new MineStoreCommon();
        Registries.CONFIG_FILE.set(plugin.getDataDirectory().resolve("config.yml").toFile());
        Registries.PLATFORM.set("hytale");
        Registries.PLATFORM_NAME.set("Hytale");
        Registries.PLATFORM_VERSION.set("1.0.0");
        Registries.LOGGER.set(s -> plugin.getLogger().at(Level.INFO).log("[MineStore]: " + s));
        Registries.SCHEDULER.set(runnable -> Registries.MINESTORE_SCHEDULER.get().runDelayed(new MineStoreScheduledTask("hytale", runnable, 0)));
        Registries.USER_GETTER.set(new HytaleUserGetter());
        Registries.HOSTNAME.set(HytaleServer.get().getConfig().getMotd());
        Registries.IP.set(InetSocketAddress.createUnresolved("0.0.0.0", HytaleServer.DEFAULT_PORT));
        Registries.PLAYER_JOIN_LISTENER.set(new HytalePlayerJoinListener());
        Registries.COMMAND_EXECUTER.set(new HytaleCommandExecuter());
        common.init(false);
    }

    @Override
    public void onDisable() {

    }
}
