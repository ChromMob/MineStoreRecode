package me.chrommob.minestore.api.classloader.repository;

public class MineStorePluginRepository {
    private final String name;
    private final String url;

    public MineStorePluginRepository(String name, String url) {
        this.name = name;
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
