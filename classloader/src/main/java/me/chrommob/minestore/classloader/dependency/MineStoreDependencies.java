package me.chrommob.minestore.classloader.dependency;

import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.RelocationHandler;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MineStoreDependencies {
    private final Set<MineStorePluginDependency> dependencies;

    public MineStoreDependencies(Set<MineStorePluginDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public Set<MineStorePluginDependency> getDependencies() {
        return dependencies;
    }

    public boolean downloadToFolder(File folder, Set<File> used, RelocationHandler relocationHandler, boolean offlineMode) {
        for (MineStorePluginDependency dependency : dependencies) {
            File file = jarFile(folder, dependency);
            File relocated = relocatedFile(folder, dependency);
            if (file.exists() && dependency.getRepository() != null) {
                MineStorePluginDependency.Verification verification = offlineMode ? MineStorePluginDependency.Verification.UNKNOWN : dependency.verify(file, dependency.getRepository());
                if (verification == MineStorePluginDependency.Verification.UNKNOWN) {
                    // Checksums are unreachable (offline or mirror down), so start from the cache instead of failing.
                    System.out.println("Could not verify " + file.getName() + ", using the cached jar");
                }
                if (verification != MineStorePluginDependency.Verification.MISMATCH) {
                    used.add(file);
                    if (dependency.hasRelocations()) {
                        if (!relocated.exists() && !relocate(relocationHandler, file, relocated, dependency.getRelocations())) {
                            return false;
                        }
                        used.add(relocated);
                    }
                    continue;
                }
            }
            if (!offlineMode && dependency.getRepository() != null) {
                Optional<byte[]> optional = dependency.download(dependency.getRepository());
                if (!optional.isPresent()) {
                    System.out.println("Could not download " + dependency.getName() + " with version " + dependency.getVersion() + " from " + dependency.getRepository().getUrl());
                    return false;
                }
                try {
                    // Write next to the target and move it in, so a crash never leaves a truncated jar in the cache.
                    File temp = new File(folder, file.getName() + ".tmp");
                    Files.write(temp.toPath(), optional.get());
                    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                    return false;
                }
                used.add(file);
                if (dependency.hasRelocations()) {
                    if (!relocate(relocationHandler, file, relocated, dependency.getRelocations())) {
                        return false;
                    }
                    used.add(relocated);
                }
                continue;
            }
            if (dependency.getRepository() == null) {
                try {
                    boolean same = dependency.verify("/jars/" + dependency.getName() + ".jarjar", file);
                    if (!same) {
                        System.out.println("Copying " + dependency.getName() + ".jarjar to " + file.getAbsolutePath());
                        InputStream in = getClass().getResourceAsStream("/jars/" + dependency.getName() + ".jarjar");
                        if (in == null) {
                            System.out.println("Could not find " + dependency.getName() + ".jarjar in jars folder");
                            return false;
                        }
                        Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        System.out.println("Using " + dependency.getName() + ".jarjar from jars folder");
                    }
                    used.add(file);
                    if (dependency.hasRelocations()) {
                        if ((!same || !relocated.exists()) && !relocate(relocationHandler, file, relocated, dependency.getRelocations())) {
                            return false;
                        }
                        used.add(relocated);
                    }
                    continue;
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            System.out.println("Could not find dependency " + dependency.getName() + " with version " + dependency.getVersion());
            return false;
        }
        return true;
    }

    private File jarFile(File folder, MineStorePluginDependency dependency) {
        return new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? ".jar" : "-" + dependency.getVersion() + ".jar"));
    }

    // The name includes a hash of the relocation map, so a jar relocated with a different map is never reused.
    private File relocatedFile(File folder, MineStorePluginDependency dependency) {
        String relocationHash = Integer.toHexString(new TreeMap<>(dependency.getRelocations()).toString().hashCode());
        return new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? "" : "-" + dependency.getVersion()) + "-relocated-" + relocationHash + ".jar");
    }

    // Relocates into a temp file and moves it in, so a failed relocation never leaves a broken jar that is reused.
    private boolean relocate(RelocationHandler relocationHandler, File file, File relocated, Map<String, String> relocations) {
        File temp = new File(relocated.getParentFile(), relocated.getName() + ".tmp");
        temp.delete();
        if (!relocationHandler.relocate(file, temp, relocations)) {
            temp.delete();
            return false;
        }
        try {
            Files.move(temp.toPath(), relocated.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            temp.delete();
            return false;
        }
    }

    public List<URI> getDependencyJars(File folder, Set<File> used, RelocationHandler relocationHandler) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        boolean offlineMode = !hasInternet();
        System.out.println("Downloading dependencies...");
        boolean download = downloadToFolder(folder, used, relocationHandler, offlineMode);
        if (!download) {
            // Continuing would start a plugin with missing classes, and the cleanup afterwards would delete the cached jars.
            throw new IllegalStateException("MineStore could not load its dependencies (see the messages above), the plugin will not start.");
        }
        List<URI> uris = new ArrayList<>();
        for (MineStorePluginDependency dependency : dependencies) {
            File file = dependency.hasRelocations() ? relocatedFile(folder, dependency) : jarFile(folder, dependency);
            uris.add(file.toURI());
        }
        return Collections.unmodifiableList(uris);
    }

    private final URI GOOGLE = URI.create("http://google.com");

    public boolean hasInternet() {
        try {
            GOOGLE.toURL().openConnection();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
