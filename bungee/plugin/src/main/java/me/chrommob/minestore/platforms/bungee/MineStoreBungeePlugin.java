package me.chrommob.minestore.platforms.bungee;

import me.chrommob.minestore.api.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.api.classloader.MineStoreClassLoader;
import me.chrommob.minestore.api.classloader.MineStorePlugin;
import me.chrommob.minestore.api.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.api.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.api.classloader.repository.MineStorePluginRepository;
import me.chrommob.minestore.api.classloader.repository.RepositoryRegistry;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

public class MineStoreBungeePlugin extends Plugin implements MineStoreBootstrapper {
    private static final String MAIN_CLASS = "me.chrommob.minestore.platforms.bungee.MineStoreBungee";
    private MineStorePlugin plugin;
    @Override
    public void onEnable() {
        try (MineStoreClassLoader classLoader = new MineStoreClassLoader(this.getClass().getClassLoader(), getDataFolder().toPath().resolve("dependencies").toFile())) {
            classLoader.loadDependencies(getDependencies());
            File file = new File(getDataFolder().toPath().resolve("dependencies").toFile(), "MineStore-Bungee.jar");
            try (InputStream in = getClass().getResourceAsStream("/jars/MineStore-Bungee.jarjar")) {
                Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            classLoader.addJarToClassLoader(file.toURI().toURL());
            classLoader.loadCommonJar();
            classLoader.loadClass("org.incendo.cloud.bungee.BungeeContextKeys");
            Class<? extends MineStorePlugin> mainClass = (Class<? extends MineStorePlugin>) classLoader.loadClass(MAIN_CLASS);
            plugin = mainClass.getDeclaredConstructor(Plugin.class).newInstance(this);
            plugin.onEnable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (plugin == null) {
            return;
        }
        plugin.onDisable();
    }

    @Override
    public MineStoreDependencies getDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        Set<MineStorePluginRepository> repositories = new HashSet<>();
        repositories.add(RepositoryRegistry.MAVEN.getRepository());
        repositories.add(RepositoryRegistry.SONATYPE.getRepository());
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-bungee", "2.0.0-beta.10"));

        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-bungeecord", "4.3.4"));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-api", "4.3.4"));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-facet", "4.3.4"));
        return new MineStoreDependencies(repositories, dependencies);
    }
}
