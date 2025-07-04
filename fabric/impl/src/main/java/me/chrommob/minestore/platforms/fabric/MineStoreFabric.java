package me.chrommob.minestore.platforms.fabric;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
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
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Function;


@SuppressWarnings("unused")
public class MineStoreFabric implements MineStorePlugin {
	public static Logger LOGGER = LoggerFactory.getLogger("minestore");

	private static MineStoreFabric instance;
	private MinecraftServer server;

	public void setServer(MinecraftServer server) {
		instance = this;
		this.server = server;
		adventure = FabricServerAudiences.of(server);

		Registries.PLATFORM_VERSION.set(server.getVersion());
		Registries.USER_GETTER.set(new FabricUserGetter(server));
		Registries.COMMAND_EXECUTER.set(new CommandExecuterFabric(server));
	}

	private static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	public MineStoreFabric() {
		final Function<ServerCommandSource, AbstractUser> cToA = commandSource -> Registries.USER_GETTER.get().get(commandSource.getEntity() == null ? NIL_UUID : commandSource.getEntity().getUuid());
		final Function<AbstractUser, ServerCommandSource> aToC = abstractUser -> abstractUser.commonUser() instanceof CommonConsoleUser ? server.getCommandSource() : server.getPlayerManager().getPlayer(abstractUser.commonUser().getUUID()).getCommandSource();
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

		Registries.COMMAND_MANAGER.set(new FabricServerCommandManager(
				ExecutionCoordinator.asyncCoordinator(),
				senderMapper
		));

		common = new MineStoreCommon();
		common.registerEssentialCommands();
	}
	private FabricServerAudiences adventure;

	public FabricServerAudiences adventure() {
		if (this.adventure == null) {
			throw new IllegalStateException("Tried to access Adventure without a running server!");
		}
		return this.adventure;
	}

	private final MineStoreCommon common;

	@Override
	public void onEnable() {
		Registries.PLATFORM.set("fabric");
		Registries.PLATFORM_NAME.set("Fabric");
		Registries.LOGGER.set(new FabricLogger(LOGGER));
		Registries.SCHEDULER.set(new FabricScheduler());
		Registries.CONFIG_FILE.set(FabricLoader.getInstance().getConfigDir().resolve("MineStore").resolve("config.yml").toFile());
		Registries.PLAYER_JOIN_LISTENER.set(new FabricPlayerEvent(common));
		common.init(true);
		Registries.LOGGER.get().log("MineStore has been enabled!");
	}

	@Override
	public void onDisable() {
		Registries.LOGGER.get().log("MineStore has been disabled!");
		adventure = null;
	}

	public static MineStoreFabric getInstance() {
		return instance;
	}

	public MineStoreCommon getCommon() {
		return common;
	}
}
