package me.chrommob.minestore.common.classloader;

import me.chrommob.minestore.common.classloader.dependency.MineStoreDependencies;
import me.chrommob.minestore.common.classloader.dependency.MineStorePluginDependency;
import me.chrommob.minestore.common.classloader.dependency.MineStorePluginRepository;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

public class Test {
    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        MineStoreClassLoader classLoader = new MineStoreClassLoader(new URL[0], Test.class.getClassLoader());
        MineStorePluginRepository repository = new MineStorePluginRepository("MavenCentral", "https://repo.maven.apache.org/maven2/");
        HashSet<MineStorePluginRepository> repositories = new HashSet<>();
        repositories.add(repository);
        MineStorePluginDependency dependency = MineStorePluginDependency.fromGradle("implementation(\"net.bytebuddy:byte-buddy:1.15.11\")");
        MineStorePluginDependency dependency2 = MineStorePluginDependency.fromGradle("implementation(\"org.mockito:mockito-core:5.17.0\")");
        MineStorePluginDependency dependency4 = MineStorePluginDependency.fromGradle("testImplementation(\"net.bytebuddy:byte-buddy-agent:1.15.11\")");
        HashSet<MineStorePluginDependency> dependencies = new HashSet<>();
        dependencies.add(dependency);
        dependencies.add(dependency2);
        dependencies.add(dependency4);
        classLoader.loadDependencies(new MineStoreDependencies(repositories, dependencies));

        classLoader.addClassToClassLoader(new File("untitled-1.0-SNAPSHOT.jar").toPath().toUri().toURL());

    }
}
