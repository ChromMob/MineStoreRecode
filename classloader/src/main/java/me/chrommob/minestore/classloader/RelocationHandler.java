package me.chrommob.minestore.classloader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

public class RelocationHandler {
    private static final String JAR_RELOCATOR_CLASS = "me.lucko.jarrelocator.JarRelocator";
    private static final String JAR_RELOCATOR_RUN_METHOD = "run";

    private final Constructor<?> jarRelocatorConstructor;
    private final Method jarRelocatorRunMethod;
    public RelocationHandler(MineStoreClassLoader classLoader) {
        try {
            Class<?> jarRelocatorClass = classLoader.loadClass(JAR_RELOCATOR_CLASS);

            this.jarRelocatorConstructor = jarRelocatorClass.getDeclaredConstructor(File.class, File.class, Map.class);
            this.jarRelocatorConstructor.setAccessible(true);

            this.jarRelocatorRunMethod = jarRelocatorClass.getDeclaredMethod(JAR_RELOCATOR_RUN_METHOD);
            this.jarRelocatorRunMethod.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JarRelocator", e);
        }
    }

    public boolean relocate(File file, File relocated, Map<String, String> relocations) {
        try {
            Object relocator = this.jarRelocatorConstructor.newInstance(file, relocated, relocations);
            this.jarRelocatorRunMethod.invoke(relocator);
        } catch (Exception e) {
            System.out.println("Failed to relocate " + file.getName() + " to " + relocated.getName());
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }
}
