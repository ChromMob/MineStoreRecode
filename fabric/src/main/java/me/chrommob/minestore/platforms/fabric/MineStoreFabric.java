package me.chrommob.minestore.platforms.fabric;

import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.MinecraftServer;

import net.minecraft.server.command.ServerCommandSource;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.fabric.events.FabricPlayerEvent;
import me.chrommob.minestore.platforms.fabric.logger.FabricLogger;
import me.chrommob.minestore.platforms.fabric.scheduler.FabricScheduler;
import me.chrommob.minestore.platforms.fabric.user.FabricUserGetter;
import me.chrommob.minestore.platforms.fabric.webcommand.CommandExecuterFabric;

import java.util.function.Function;

@SuppressWarnings("unused")
public class MineStoreFabric implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("minestore");

	private static MineStoreFabric instance;
	private FabricServerAudiences adventure;

	public FabricServerAudiences adventure() {
		if (this.adventure == null) {
			throw new IllegalStateException("Tried to access Adventure without a running server!");
		}
		return this.adventure;
	}

	@Override
	public void onInitialize() {
		instance = this;
		ServerLifecycleEvents.SERVER_STARTED.register(this::onEnable);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onDisable);

	}

	private MineStoreCommon common;

	private void onEnable(MinecraftServer server) {
		LOGGER.info("MineStore has been enabled!");
		adventure = FabricServerAudiences.of(server);
		common = new MineStoreCommon();
		common.setPlatform("fabric");
		common.setPlatformName("Fabric");
		common.setPlatformVersion(server.getVersion());
		common.registerLogger(new FabricLogger(LOGGER));
		common.registerScheduler(new FabricScheduler());
		common.registerUserGetter(new FabricUserGetter(server, common));

		final Function<ServerCommandSource, AbstractUser> cToA = commandSource -> new AbstractUser(commandSource.isExecutedByPlayer() ? commandSource.getPlayer().getUuid() : null, common, commandSource);
		final Function<AbstractUser, ServerCommandSource> aToC = abstractUser -> abstractUser.user() instanceof CommonConsoleUser ? server.getCommandSource() : server.getPlayerManager().getPlayer(abstractUser.user().getUUID()).getCommandSource();
		final SenderMapper<ServerCommandSource, AbstractUser> senderMapper = new SenderMapper<ServerCommandSource, AbstractUser>() {
			@Override
			public AbstractUser map(ServerCommandSource base) {
				return cToA.apply(base);
			}

			@Override
			public ServerCommandSource reverse(AbstractUser mapped) {
				return aToC.apply(mapped);
			}
		};

		common.registerCommandManager(new FabricServerCommandManager(
				ExecutionCoordinator.asyncCoordinator(),
				senderMapper
		));

		common.registerCommandExecuter(new CommandExecuterFabric(server));
		common.setConfigLocation(FabricLoader.getInstance().getConfigDir().resolve("MineStore").resolve("config.yml").toFile());
		common.registerPlayerJoinListener(new FabricPlayerEvent(common));
		common.init(false);
	}

	private void onDisable(MinecraftServer server) {
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
