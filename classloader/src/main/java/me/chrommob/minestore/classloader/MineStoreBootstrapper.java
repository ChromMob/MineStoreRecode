package me.chrommob.minestore.classloader;

import me.chrommob.minestore.classloader.dependency.MineStoreDependencies;

public interface MineStoreBootstrapper {
    MineStoreDependencies getDependencies();
}
