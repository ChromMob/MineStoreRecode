package me.chrommob.minestore.platforms.bukkit;

import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.MineStoreClassLoader;
import me.chrommob.minestore.classloader.MineStorePlugin;
import me.chrommob.minestore.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;
import me.chrommob.minestore.classloader.repository.RepositoryRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;

public class MineStoreBukkitPlugin extends JavaPlugin implements MineStoreBootstrapper {
    private static final String MAIN_CLASS = "me.chrommob.minestore.platforms.bukkit.MineStoreBukkit";
    private MineStorePlugin plugin;
    private MineStoreClassLoader classLoader;

    @Override
    public void onEnable() {
        try {
            Map<String, String> relocations = new HashMap<>();
            try {
                Class.forName("net.kyori.adventure.Adventure");
            } catch (ClassNotFoundException e) {
                relocations.put("net.kyori", "me.chrommob.minestore.libs.net.kyori");
            }
            classLoader = new MineStoreClassLoader(this.getClass().getClassLoader(), getDataFolder().toPath().resolve("dependencies").toFile(), relocations);

            classLoader.add(getDependencies());
            classLoader.addCommonJar(relocations);
            classLoader.loadDependencies();
            classLoader.removeUnusedDependencies();
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

        Map<String, String> relocations = new HashMap<>();
        try {
            Class.forName("net.kyori.adventure.Adventure");
            dependencies.add(new MineStorePluginDependency("", "MineStore-Bukkit-Kyori-Native", "", relocations));
        } catch (ClassNotFoundException e) {
            relocations.put("net.kyori", "me.chrommob.minestore.libs.net.kyori");
            dependencies.add(new MineStorePluginDependency("", "MineStore-Bukkit-Kyori-Compat", "", relocations));
        }

        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-bukkit", "4.3.4", relocations));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-api", "4.3.4", relocations));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-facet", "4.3.4", relocations));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-text-serializer-gson-legacy-impl", "4.13.1", relocations));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-nbt", "4.13.1", relocations));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-text-serializer-gson", "4.13.1", relocations));

        dependencies.add(new MineStorePluginDependency("", "MineStore-Bukkit", "", relocations));

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
