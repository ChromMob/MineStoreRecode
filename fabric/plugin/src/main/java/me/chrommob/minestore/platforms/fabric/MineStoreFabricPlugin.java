package me.chrommob.minestore.platforms.fabric;

import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.MineStoreClassLoader;
import me.chrommob.minestore.classloader.MineStorePlugin;
import me.chrommob.minestore.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class MineStoreFabricPlugin implements MineStoreBootstrapper, ModInitializer {
    private static final String MAIN_CLASS = "me.chrommob.minestore.platforms.fabric.MineStoreFabric";
    private MineStoreClassLoader classLoader;
    private MineStorePlugin plugin;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onEnable);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onDisable);
        try {
            File config = FabricLoader.getInstance().getConfigDir().resolve("MineStore").resolve("dependencies").toFile();
            classLoader = new MineStoreClassLoader(getClass().getClassLoader(), config);
            classLoader.add(getDependencies());
            classLoader.addCommonJar();
            classLoader.loadDependencies();
            classLoader.removeUnusedDependencies();

            Class<? extends MineStorePlugin> mainClass = (Class<? extends MineStorePlugin>) classLoader.loadClass(MAIN_CLASS);
            plugin = mainClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onEnable(MinecraftServer server) {
        try {
            plugin.getClass().getMethod("setServer", MinecraftServer.class).invoke(plugin, server);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace(System.err);
        }
        plugin.onEnable();
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
