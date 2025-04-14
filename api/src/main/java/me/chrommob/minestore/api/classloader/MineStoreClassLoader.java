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
import java.util.*;

public class MineStoreClassLoader extends URLClassLoader {
    private final File folder;
    private final Set<MineStoreDependencies> dependencies = new HashSet<>();
    private final List<MineStorePluginDependency> loadedDependencies = new ArrayList<>();
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public MineStoreClassLoader(ClassLoader parent, File folder) {
        super(new URL[0], parent);
        this.folder = folder;
        dependencies.add(getGlobalDependencies());
    }

    public void addJarToClassLoader(URL url) {
        super.addURL(url);
    }

    private boolean checkConflict() {
        for (MineStoreDependencies depend : dependencies) {
            for (MineStoreDependencies depend2 : dependencies) {
                for (MineStorePluginDependency dependency : depend.getDependencies()) {
                    for (MineStorePluginDependency dependency2 : depend2.getDependencies()) {
                        if (dependency.conflictsWith(dependency2)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void add(MineStoreDependencies dependencies) {
        this.dependencies.add(dependencies);
    }

    public void loadDependencies() {
        if (checkConflict()) {
            throw new IllegalStateException("Conflicting dependencies found!");
        }

        for (MineStoreDependencies depend : dependencies) {
            loadedDependencies.addAll(depend.getDependencies());
        }

        Set<File> used = new HashSet<>();
        for (MineStoreDependencies depend : dependencies) {
            for (URI dependencyJar : depend.getDependencyJars(folder, used)) {
                try {
                    addJarToClassLoader(dependencyJar.toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (used.contains(file)) {
                continue;
            }
            file.delete();
        }
    }

    private MineStoreDependencies getGlobalDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        Set<MineStorePluginRepository> repositories = new HashSet<>();

        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-core", "2.0.0"));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-annotations", "2.0.0"));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-services", "2.0.0"));

        repositories.add(RepositoryRegistry.MAVEN.getRepository());
        repositories.add(RepositoryRegistry.SONATYPE.getRepository());
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
