package me.chrommob.minestore.platforms.fabric;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.MineStoreClassLoader;
import me.chrommob.minestore.classloader.MineStorePlugin;
import me.chrommob.minestore.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;
import me.chrommob.minestore.classloader.repository.RepositoryRegistry;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class MineStoreFabricPlugin implements MineStoreBootstrapper, ModInitializer {
    private static final String MAIN_CLASS = "me.chrommob.minestore.platforms.fabric.MineStoreFabric";
    private MineStoreClassLoader classLoader;
    private MineStorePlugin plugin;
    private MinecraftServer server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onEnable);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onDisable);

        final Function<ServerCommandSource, AbstractUser> cToA = commandSource -> Registries.USER_GETTER.get().get(commandSource.getEntity().getUuid());
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
    }

    private void onEnable(MinecraftServer server) {
        this.server = server;
        try {
            classLoader = new MineStoreClassLoader(getClass().getClassLoader(), FabricLoader.getInstance().getConfigDir().resolve("MineStore").resolve("dependencies").toFile());
            classLoader.add(getDependencies());
            classLoader.addCommonJar();
            classLoader.loadDependencies();
            classLoader.removeUnusedDependencies();

            Class<? extends MineStorePlugin> mainClass = (Class<? extends MineStorePlugin>) classLoader.loadClass(MAIN_CLASS);
            plugin = mainClass.getDeclaredConstructor(MinecraftServer.class).newInstance(server);
            plugin.onEnable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onDisable(MinecraftServer server) {
        plugin.onDisable();
    }

    @Override
    public MineStoreDependencies getDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        Set<MineStorePluginRepository> repositories = new HashSet<>();
        dependencies.add(new MineStorePluginDependency("", "MineStore-Fabric", ""));
        return new MineStoreDependencies(repositories, dependencies);
    }
}
