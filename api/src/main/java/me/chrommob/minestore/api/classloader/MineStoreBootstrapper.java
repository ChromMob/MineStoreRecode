package me.chrommob.minestore.api.classloader;

import me.chrommob.minestore.api.classloader.dependency.MineStoreDependencies;

public interface MineStoreBootstrapper {
    MineStoreDependencies getDependencies();
}
