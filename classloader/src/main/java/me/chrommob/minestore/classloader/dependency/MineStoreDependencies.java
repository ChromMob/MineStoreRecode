package me.chrommob.minestore.classloader.dependency;

import me.chrommob.minestore.classloader.MineStoreBootstrapper;
import me.chrommob.minestore.classloader.RelocationHandler;
import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MineStoreDependencies {
    private final Set<MineStorePluginRepository> repositories;
    private final Set<MineStorePluginDependency> dependencies;

    public MineStoreDependencies(Set<MineStorePluginRepository> repositories, Set<MineStorePluginDependency> dependencies) {
        this.repositories = repositories;
        this.dependencies = dependencies;
    }

    public Set<MineStorePluginRepository> getRepositories() {
        return repositories;
    }

    public Set<MineStorePluginDependency> getDependencies() {
        return dependencies;
    }

    public boolean downloadToFolder(File folder, Set<File> used, RelocationHandler relocationHandler) {
        for (MineStorePluginDependency dependency : dependencies) {
            File file = new File(folder, dependency.getName() + (dependency.getVersion().isEmpty() ? ".jar" : "-" + dependency.getVersion() + ".jar"));
            boolean found = false;
            if (file.exists()) {
                for (MineStorePluginRepository repository : repositories) {
                    if (dependency.verify(file, repository)) {
                        found = true;
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
                        break;
                    }
                }
            }
            if (!found) {
                for (MineStorePluginRepository repository : repositories) {
                    Optional<byte[]> optional = dependency.download(repository);
                    if (optional.isPresent()) {
                        try {
                            file.createNewFile();
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(optional.get());
                            fileOutputStream.close();
                            found = true;
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
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (!found) {
                try {
                    boolean same = dependency.verify("/jars/" + dependency.getName() + ".jarjar", file);
                    if (!same) {
                        System.out.println("Copying " + dependency.getName() + ".jarjar to " + file.getAbsolutePath());
                        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/jars/" + dependency.getName() + ".jarjar")), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
                                found = true;
                            }
                        } else {
                            used.add(relocated);
                            found = true;
                        }
                    } else {
                        found = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                if (found) {
                    continue;
                }
            }
            if (!found) {
                System.out.println("Could not find dependency " + dependency.getName() + " with version " + dependency.getVersion());
                return false;
            }
        }
        return true;
    }

    public List<URI> getDependencyJars(File folder, Set<File> used, RelocationHandler relocationHandler) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        boolean download = downloadToFolder(folder, used, relocationHandler);
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
}
