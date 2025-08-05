package me.chrommob.minestore.classloader.dependency;

import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.RelocationHandler;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
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
            File file = new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? ".jar" : "-" + dependency.getVersion() + ".jar"));
            if (file.exists() && dependency.getRepository() != null) {
                if (offlineMode || dependency.verify(file, dependency.getRepository())) {
                    used.add(file);
                    File relocated = new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? "-relocated.jar" : "-" + dependency.getVersion() + "-relocated.jar"));
                    if (dependency.hasRelocations()) {
                        if (!relocated.exists()) {
                            boolean res = relocationHandler.relocate(file, relocated, dependency.getRelocations());
                            if (res) {
                                file = relocated;
                            }
                            used.add(file);
                        } else {
                            used.add(relocated);
                        }
                    }
                    continue;
                }
            }
            if (!offlineMode && dependency.getRepository() != null) {
                Optional<byte[]> optional = dependency.download(dependency.getRepository());
                if (optional.isPresent()) {
                    try {
                        file.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(optional.get());
                        fileOutputStream.close();
                        used.add(file);
                        File relocated = new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? "-relocated.jar" : "-" + dependency.getVersion() + "-relocated.jar"));
                        if (dependency.hasRelocations()) {
                            if (relocated.exists()) {
                                relocated.delete();
                            }
                            boolean res = relocationHandler.relocate(file, relocated, dependency.getRelocations());
                            if (res) {
                                file = relocated;
                                used.add(file);
                            }
                        }
                        continue;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Could not download " + dependency.getName() + " with version " + dependency.getVersion() + " from " + dependency.getRepository().getUrl());
                    return false;
                }
            }
            if (dependency.getRepository() == null) {
                try {
                    boolean same = dependency.verify(File.separator + "jars" + File.separator + dependency.getName() + ".jarjar", file);
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
                        File relocated = new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? "-relocated.jar" : "-" + dependency.getVersion() + "-relocated.jar"));
                        if (!same) {
                            boolean res = relocationHandler.relocate(file, relocated, dependency.getRelocations());
                            if (res) {
                                file = relocated;
                                used.add(file);
                                continue;
                            }
                        } else {
                            used.add(relocated);
                            continue;
                        }
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            System.out.println("Could not find dependency " + dependency.getName() + " with version " + dependency.getVersion());
            return false;
        }
        return true;
    }

    public List<URI> getDependencyJars(File folder, Set<File> used, RelocationHandler relocationHandler) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        boolean offlineMode = !hasInternet();
        System.out.println("Downloading dependencies...");
        boolean download = downloadToFolder(folder, used, relocationHandler, offlineMode);
        if (!download) {
            System.out.println("Could not download dependencies!");
            return Collections.emptyList();
        }
        List<URI> uris = new ArrayList<>();
        for (MineStorePluginDependency dependency : dependencies) {
            File file = new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? ".jar" : "-" + dependency.getVersion() + ".jar"));
            if (dependency.hasRelocations()) {
                file = new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? "-relocated.jar" : "-" + dependency.getVersion() + "-relocated.jar"));
            }
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
