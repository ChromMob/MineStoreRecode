package me.chrommob.minestore.platforms.bungee;

import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.MineStoreClassLoader;
import me.chrommob.minestore.classloader.MineStorePlugin;
import me.chrommob.minestore.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;
import me.chrommob.minestore.classloader.repository.RepositoryRegistry;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MineStoreBungeePlugin extends Plugin implements MineStoreBootstrapper {
    private static final String MAIN_CLASS = "me.chrommob.minestore.platforms.bungee.MineStoreBungee";
    private MineStorePlugin plugin;
    private MineStoreClassLoader classLoader;

    @Override
    public void onEnable() {
        try {
            Map<String, String> relocations = new HashMap<>();
            relocations.put("net.kyori", "me.chrommob.minestore.libs.net.kyori");
            classLoader = new MineStoreClassLoader(this.getClass().getClassLoader(), getDataFolder().toPath().resolve("dependencies").toFile(), relocations);

            classLoader.add(getDependencies());
            classLoader.addCommonJar(relocations);
            classLoader.loadDependencies();
            classLoader.removeUnusedDependencies();

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
        Set<MineStorePluginRepository> repositories = new HashSet<>();

        Map<String, String> relocations = new HashMap<>();
        relocations.put("net.kyori", "me.chrommob.minestore.libs.net.kyori");

        repositories.add(RepositoryRegistry.MAVEN.getRepository());
        repositories.add(RepositoryRegistry.SONATYPE.getRepository());
        dependencies.add(new MineStorePluginDependency("", "MineStore-Bungee", "", relocations));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-bungee", "2.0.0-beta.10"));

        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-bungeecord", "4.3.4", relocations));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-api", "4.3.4", relocations));
        dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-platform-facet", "4.3.4", relocations));
        return new MineStoreDependencies(repositories, dependencies);
    }
}
