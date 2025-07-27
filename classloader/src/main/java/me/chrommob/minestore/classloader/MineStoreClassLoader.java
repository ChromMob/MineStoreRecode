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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        List<MineStorePluginDependency> allDependencies = dependencies.parallelStream()
                .flatMap(depend -> depend.getDependencies().stream())
                .collect(Collectors.toList());

        return IntStream.range(0, allDependencies.size())
                .parallel()
                .anyMatch(i ->
                        IntStream.range(i + 1, allDependencies.size())
                                .anyMatch(j -> allDependencies.get(i).conflictsWith(allDependencies.get(j)))
                );
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

    private final static String[] IGNORED_PACKAGES = new String[]{
            "org.incendo.cloud.",
            "io.leangen.geantyref.",
            "net.kyori.",
            "org.slf4j.",
            "com.google.gson."
            };

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Delegate core Java classes to parent
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.")) {
            return super.loadClass(name, resolve);
        }

        // If interop also load from parent
        for (String ignoredPackage : IGNORED_PACKAGES) {
            if (name.startsWith(ignoredPackage)) {
                return super.loadClass(name, resolve);
            }
        }

        // First, check if already loaded
        Class<?> c = findLoadedClass(name);

        // Try to load it from this classloader (child)
        if (c == null) {
            try {
                c = findClass(name); // from the URLs in this classloader
            } catch (ClassNotFoundException e) {
                // Not found here, delegate to parent
                c = super.loadClass(name, resolve);
            }
        }

        if (resolve) {
            resolveClass(c);
        }

        return c;
    }


    private void loadRelocateDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        dependencies.add(new MineStorePluginDependency("org.ow2.asm", "asm", "9.1", RepositoryRegistry.MAVEN.getRepository()));
        dependencies.add(new MineStorePluginDependency("org.ow2.asm", "asm-commons", "9.1", RepositoryRegistry.MAVEN.getRepository()));
        dependencies.add(new MineStorePluginDependency("me.lucko", "jar-relocator", "1.7", RepositoryRegistry.MAVEN.getRepository()));
        this.dependencies.add(new MineStoreDependencies(dependencies));
        loadDependencies();
    }

    private MineStoreDependencies getGlobalDependencies() {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();

        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-core", "2.0.0", RepositoryRegistry.MAVEN.getRepository()));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-annotations", "2.0.0", RepositoryRegistry.MAVEN.getRepository()));
        dependencies.add(new MineStorePluginDependency("org.incendo", "cloud-services", "2.0.0", RepositoryRegistry.MAVEN.getRepository()));
        dependencies.add(new MineStorePluginDependency("org.mariadb.jdbc", "mariadb-java-client", "3.5.3", RepositoryRegistry.MAVEN.getRepository()));
        dependencies.add(new MineStorePluginDependency("com.mysql", "mysql-connector-j", "9.3.0", RepositoryRegistry.MAVEN.getRepository()));
        dependencies.add(new MineStorePluginDependency("io.leangen.geantyref", "geantyref", "2.0.1", RepositoryRegistry.MAVEN.getRepository()));

        return new MineStoreDependencies(dependencies);
    }

    public void addCommonJar() {
        addCommonJar(new HashMap<>());
    }

    public void addCommonJar(Map<String, String> relocations) {
        Set<MineStorePluginDependency> dependencies = new HashSet<>();
        dependencies.add(new MineStorePluginDependency("", "MineStore-Common", "", relocations, null));
        add(new MineStoreDependencies(dependencies));
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
