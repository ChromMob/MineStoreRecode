package me.chrommob.minestore.common.classloader;

import me.chrommob.minestore.common.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.common.classloader.dependency.MineStorePluginDependency;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MineStoreClassLoader extends URLClassLoader {
    private final List<MineStorePluginDependency> loadedDependencies = new ArrayList<>();
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public MineStoreClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addJarToClassLoader(URL url) {
        super.addURL(url);
    }

    public void addClassToClassLoader(URL url) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        super.addURL(url);
        System.out.println(Arrays.toString(getURLs()));
        Class<?> c = loadClass("me.chrommob.Main", true);
        c.getDeclaredConstructor().newInstance();
        //cal public static main
        c.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
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

        for (URI dependencyJar : dependencies.getDependencyJars()) {
            try {
                addJarToClassLoader(dependencyJar.toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
