package me.chrommob.minestore.platforms.bukkit;

import me.chrommob.minestore.api.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.api.classloader.MineStoreClassLoader;
import me.chrommob.minestore.api.classloader.MineStorePlugin;
import me.chrommob.minestore.api.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.api.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.api.classloader.repository.MineStorePluginRepository;
import me.chrommob.minestore.api.classloader.repository.RepositoryRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

public class MineStoreBukkitPlugin extends JavaPlugin implements MineStoreBootstrapper {
    private static final String MAIN_CLASS = "me.chrommob.minestore.platforms.bukkit.MineStoreBukkit";
    private MineStorePlugin plugin;
    private MineStoreClassLoader classLoader;

    @Override
    public void onEnable() {
        try {
            classLoader = new MineStoreClassLoader(this.getClass().getClassLoader(), getDataFolder().toPath().resolve("dependencies").toFile());
            classLoader.loadDependencies(getDependencies());
            File file = new File(getDataFolder().toPath().resolve("dependencies").toFile(), "MineStore-Bukkit.jar");
            try (InputStream in = getClass().getResourceAsStream("/jars/MineStore-Bukkit.jarjar")) {
                Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            classLoader.loadCommonJar();
            classLoader.addJarToClassLoader(file.toURI().toURL());
            classLoader.loadClass("org.incendo.cloud.bukkit.BukkitCommandContextKeys");
            Class<? extends MineStorePlugin> mainClass = (Class<? extends MineStorePlugin>) classLoader.loadClass(MAIN_CLASS);
            plugin = mainClass.getDeclaredConstructor(JavaPlugin.class).newInstance(this);
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
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MineStoreDependencies getDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();

        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-bukkit", "4.3.4"));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-api", "4.3.4"));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-facet", "4.3.4"));

        MineStorePluginDependency cloudPaper = MineStorePluginDependency.fromGradle("org.incendo:cloud-paper:2.0.0-beta.10");
        MineStorePluginDependency cloudBukkit = MineStorePluginDependency.fromGradle("org.incendo:cloud-bukkit:2.0.0-beta.10");
        MineStorePluginDependency cloudBrigadier = MineStorePluginDependency.fromGradle("org.incendo:cloud-brigadier:2.0.0-beta.10");

        dependencies.add(cloudPaper);
        dependencies.add(cloudBukkit);
        dependencies.add(cloudBrigadier);
        Set<MineStorePluginRepository> repositories = new HashSet<>();
        repositories.add(RepositoryRegistry.SONATYPE.getRepository());
        repositories.add(RepositoryRegistry.MAVEN.getRepository());
        return new MineStoreDependencies(repositories, dependencies);
    }
}
