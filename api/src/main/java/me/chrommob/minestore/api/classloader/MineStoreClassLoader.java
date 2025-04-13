package me.chrommob.minestore.api.classloader;

import me.chrommob.minestore.api.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.api.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.api.classloader.repository.MineStorePluginRepository;
import me.chrommob.minestore.api.classloader.repository.RepositoryRegistry;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MineStoreClassLoader extends URLClassLoader {
    private final File folder;
    private final List<MineStorePluginDependency> loadedDependencies = new ArrayList<>();
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public MineStoreClassLoader(ClassLoader parent, File folder) {
        super(new URL[0], parent);
        this.folder = folder;
        loadDependencies(getGlobalDependencies());
    }

    public void addJarToClassLoader(URL url) {
        super.addURL(url);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name, true);
    }

    private boolean checkConflict(MineStoreDependencies dependencies) {
        for (MineStorePluginDependency dependency : loadedDependencies) {
            for (MineStorePluginDependency dependency2 : dependencies.getDependencies()) {
                if (dependency.conflictsWith(dependency2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void loadDependencies(MineStoreDependencies dependencies) {
        if (checkConflict(dependencies)) {
            throw new IllegalStateException("Conflicting dependencies found!");
        }

        for (MineStorePluginDependency dependency : dependencies.getDependencies()) {
            for (MineStorePluginDependency dependency2 : dependencies.getDependencies()) {
                if (dependency == dependency2) {
                    continue;
                }
                if (dependency.conflictsWith(dependency2)) {
                    throw new IllegalStateException("Conflicting dependencies found!");
                }
            }
        }

        loadedDependencies.addAll(dependencies.getDependencies());

        for (URI dependencyJar : dependencies.getDependencyJars(folder)) {
            try {
                addJarToClassLoader(dependencyJar.toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private MineStoreDependencies getGlobalDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        Set<MineStorePluginRepository> repositories = new HashSet<>();
        repositories.add(RepositoryRegistry.MAVEN.getRepository());
        return new MineStoreDependencies(repositories, dependencies);
    }

    public void loadCommonJar() {
        File file = new File(folder, "MineStore-Common.jar");
        try (InputStream in = getClass().getResourceAsStream("/jars/MineStore-Common.jarjar")) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            addJarToClassLoader(file.toURI().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
