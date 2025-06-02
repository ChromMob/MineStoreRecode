package me.chrommob.minestore.classloader;

import me.chrommob.minestore.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;
import me.chrommob.minestore.classloader.repository.RepositoryRegistry;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class MineStoreClassLoader extends URLClassLoader {
    private final File folder;
    private final RelocationHandler relocationHandler;
    private final Set<MineStoreDependencies> dependencies = new HashSet<>();
    private final Set<MineStoreDependencies> loadedDependencies = new HashSet<>();
    private final Map<String, String> addonRelocations;
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public MineStoreClassLoader(ClassLoader parent, File folder, Map<String, String> addonRelocations) {
        super(new URL[0], parent);
        this.folder = folder;
        this.addonRelocations = addonRelocations;
        loadRelocateDependencies();
        relocationHandler = new RelocationHandler(this);
        dependencies.add(getGlobalDependencies());
    }

    public MineStoreClassLoader(ClassLoader parent, File folder) {
        this(parent, folder, new HashMap<>());
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

    private final Set<File> used = new HashSet<>();
    public void loadDependencies() {
        if (checkConflict()) {
            throw new IllegalStateException("Conflicting dependencies found!");
        }

        for (MineStoreDependencies depend : dependencies) {
            if (loadedDependencies.contains(depend)) {
                continue;
            }
            for (URI dependencyJar : depend.getDependencyJars(folder, used, relocationHandler)) {
                try {
                    addJarToClassLoader(dependencyJar.toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            loadedDependencies.add(depend);
        }
    }

    public void removeUnusedDependencies() {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (used.contains(file)) {
                continue;
            }
            System.out.println("Removing unused dependency: " + file.getName());
            file.delete();
        }
    }

    private void loadRelocateDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        Set<MineStorePluginRepository> repositories = new HashSet<>();
        repositories.add(RepositoryRegistry.MAVEN.getRepository());
        dependencies.add(new MineStorePluginDependency("org.ow2.asm", "asm", "9.1"));
        dependencies.add(new MineStorePluginDependency("org.ow2.asm", "asm-commons", "9.1"));
        dependencies.add(new MineStorePluginDependency("me.lucko", "jar-relocator", "1.7"));
        this.dependencies.add(new MineStoreDependencies(repositories, dependencies));
        loadDependencies();
    }

    private MineStoreDependencies getGlobalDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        Set<MineStorePluginRepository> repositories = new HashSet<>();

        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-core", "2.0.0"));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-annotations", "2.0.0"));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-services", "2.0.0"));
        dependencies.add(new MineStorePluginDependency("org.mariadb.jdbc", "mariadb-java-client", "3.5.3"));
        dependencies.add(new MineStorePluginDependency("com.mysql", "mysql-connector-j", "9.3.0"));
        dependencies.add(new MineStorePluginDependency("io.leangen.geantyref", "geantyref", "2.0.1"));

        repositories.add(RepositoryRegistry.MAVEN.getRepository());
        repositories.add(RepositoryRegistry.MAVEN1.getRepository());
        repositories.add(RepositoryRegistry.SONATYPE.getRepository());
        return new MineStoreDependencies(repositories, dependencies);
    }

    public void addCommonJar() {
        addCommonJar(new HashMap<>());
    }

    public void addCommonJar(Map<String, String> relocations) {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        Set<MineStorePluginRepository> repositories = new HashSet<>();

        dependencies.add(new MineStorePluginDependency("", "MineStore-Common", "", relocations));
        repositories.add(RepositoryRegistry.MAVEN.getRepository());
        add(new MineStoreDependencies(repositories, dependencies));
    }

    public boolean relocateAddon() {
        return addonRelocations.isEmpty();
    }

    public File remapAddon(File file) {
        File relocated = new File(folder, file.getName().replace(".jar", "-relocated.jar"));
        relocationHandler.relocate(file, relocated, addonRelocations);
        return relocated;
    }
}
