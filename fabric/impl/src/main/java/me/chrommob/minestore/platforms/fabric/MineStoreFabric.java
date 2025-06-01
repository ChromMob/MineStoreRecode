package me.chrommob.minestore.platforms.fabric;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.classloader.MineStorePlugin;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.fabric.events.FabricPlayerEvent;
import me.chrommob.minestore.platforms.fabric.logger.FabricLogger;
import me.chrommob.minestore.platforms.fabric.scheduler.FabricScheduler;
import me.chrommob.minestore.platforms.fabric.user.FabricUserGetter;
import me.chrommob.minestore.platforms.fabric.webcommand.CommandExecuterFabric;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("unused")
public class MineStoreFabric implements MineStorePlugin {
	public static final Logger LOGGER = LoggerFactory.getLogger("minestore");

	private static MineStoreFabric instance;
	private final MinecraftServer server;

	public MineStoreFabric(MinecraftServer server) {
		this.server = server;
	}
	private FabricServerAudiences adventure;

	public FabricServerAudiences adventure() {
		if (this.adventure == null) {
			throw new IllegalStateException("Tried to access Adventure without a running server!");
		}
		return this.adventure;
	}

	private MineStoreCommon common;

	@Override
	public void onEnable() {
		LOGGER.info("MineStore has been enabled!");
		adventure = FabricServerAudiences.of(server);
		common = new MineStoreCommon();
		Registries.PLATFORM.set("fabric");
		Registries.PLATFORM_NAME.set("Fabric");
		Registries.PLATFORM_VERSION.set(server.getVersion());
		Registries.LOGGER.set(new FabricLogger(LOGGER));
		Registries.SCHEDULER.set(new FabricScheduler());
		Registries.USER_GETTER.set(new FabricUserGetter(server));
		Registries.COMMAND_EXECUTER.set(new CommandExecuterFabric(server));
		Registries.CONFIG_FILE.set(FabricLoader.getInstance().getConfigDir().resolve("MineStore").resolve("config.yml").toFile());
		Registries.PLAYER_JOIN_LISTENER.set(new FabricPlayerEvent(common));
		common.init(false);
	}

	@Override
	public void onDisable() {
		LOGGER.info("MineStore has been disabled!");
		adventure = null;
	}

	public static MineStoreFabric getInstance() {
		return instance;
	}

	public MineStoreCommon getCommon() {
		return common;
	}
}
