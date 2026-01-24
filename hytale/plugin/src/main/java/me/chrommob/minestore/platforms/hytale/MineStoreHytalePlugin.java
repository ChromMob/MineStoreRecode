package me.chrommob.minestore.platforms.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.MineStoreClassLoader;
import me.chrommob.minestore.classloader.MineStorePlugin;
import me.chrommob.minestore.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.classloader.repository.RepositoryRegistry;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;

public class MineStoreHytalePlugin extends JavaPlugin implements MineStoreBootstrapper {
    private MineStoreClassLoader classLoader;
    private static final String MAIN_CLASS = "me.chrommob.minestore.platforms.hytale.MineStoreHytale";
    private MineStorePlugin plugin;
    public MineStoreHytalePlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        try {
            classLoader = new MineStoreClassLoader(getClass().getClassLoader(), getDataDirectory().resolve("dependencies").toFile());

            classLoader.add(getDependencies());
            classLoader.addCommonJar();
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
    public MineStoreDependencies getDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
        } catch (ClassNotFoundException e) {
            dependencies.add(new MineStorePluginDependency("net.kyori", "adventure-text-minimessage", "4.18.0", null, RepositoryRegistry.MAVEN.getRepository()));
        }
        dependencies.add(new MineStorePluginDependency("", "MineStore-Hytale", "", null));
        return new MineStoreDependencies(dependencies);
    }
}
