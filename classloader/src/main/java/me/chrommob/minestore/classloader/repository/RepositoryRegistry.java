package me.chrommob.minestore.classloader.repository;

public enum RepositoryRegistry {
    MAVEN("maven", "https://maven-central-eu.storage-download.googleapis.com/maven2/"),
    MAVEN1("maven1", "https://repo1.maven.org/maven2/"),
    JITPACK("jitpack", "https://jitpack.io/"),
    SONATYPE("sonatype", "https://s01.oss.sonatype.org/content/repositories/releases/");

    private final MineStorePluginRepository repository;

    RepositoryRegistry(String name, String url) {
        repository = new MineStorePluginRepository(name, url);
    }

    public MineStorePluginRepository getRepository() {
        return repository;
    }
}
