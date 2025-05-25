package me.chrommob.minestore.api.classloader.dependency;

import me.chrommob.minestore.api.classloader.repository.MineStorePluginRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
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

    public boolean downloadToFolder(File folder, Set<File> used) {
        for (MineStorePluginDependency dependency : dependencies) {
            File file = new File(folder, dependency.getName() + "-" + dependency.getVersion() + ".jar");
            boolean found = false;
            for (MineStorePluginRepository repository : repositories) {
                if (file.exists()) {
                    if (dependency.verify(file, repository)) {
                        found = true;
                        used.add(file);
                        break;
                    }
                }
                Optional<byte[]> optional = dependency.download(repository);
                if (optional.isPresent()) {
                    try {
                        file.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(optional.get());
                        fileOutputStream.close();
                        found = true;
                        used.add(file);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!found) {
                System.out.println("Could not find dependency " + dependency.getName() + " with version " + dependency.getVersion());
                return false;
            }
        }
        return true;
    }

    public List<URI> getDependencyJars(File folder, Set<File> used) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        boolean download = downloadToFolder(folder, used);
        if (!download) {
            System.out.println("Could not download dependencies!");
            return Collections.emptyList();
        }
        List<URI> uris = new ArrayList<>();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            uris.add(file.toURI());
        }
        return Collections.unmodifiableList(uris);
    }
}
