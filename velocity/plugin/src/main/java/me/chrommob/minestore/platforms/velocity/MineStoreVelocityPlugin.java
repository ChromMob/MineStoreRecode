package me.chrommob.minestore.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.MineStoreClassLoader;
import me.chrommob.minestore.classloader.MineStorePlugin;
import me.chrommob.minestore.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;
import me.chrommob.minestore.classloader.repository.RepositoryRegistry;
import me.chrommob.minestore.api.stats.BuildConstats;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

@Plugin(id = "minestore", name = "MineStore", version = BuildConstats.VERSION, description = "MineStore plugin for Velocity", authors = {"chrommob"})
public class MineStoreVelocityPlugin implements MineStoreBootstrapper {
    private static final String MAIN_CLASS = "me.chrommob.minestore.platforms.velocity.MineStoreVelocity";

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer server;
    @Inject
    @DataDirectory
    private Path dataPath;

    private MineStorePlugin plugin;
    private MineStoreClassLoader classLoader;

    @Subscribe
    private void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            classLoader = new MineStoreClassLoader(getClass().getClassLoader(), dataPath.resolve("dependencies").toFile());

            classLoader.add(getDependencies());
            classLoader.addCommonJar();
            classLoader.loadDependencies();
            classLoader.removeUnusedDependencies();
            Class<? extends MineStorePlugin> mainClass = (Class<? extends MineStorePlugin>) classLoader.loadClass(MAIN_CLASS);
            plugin = mainClass.getDeclaredConstructor(Object.class, ProxyServer.class, Logger.class, Path.class).newInstance(this, server, logger, dataPath);
            plugin.onEnable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onServerStop(ProxyShutdownEvent event) {
        plugin.onDisable();
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public MineStoreDependencies getDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        dependencies.add(new MineStorePluginDependency("", "MineStore-Velocity", "", null));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-velocity", "2.0.0-beta.10", RepositoryRegistry.MAVEN.getRepository()));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-brigadier", "2.0.0-beta.10", RepositoryRegistry.MAVEN.getRepository()));
        return new MineStoreDependencies(dependencies);
    }
}
