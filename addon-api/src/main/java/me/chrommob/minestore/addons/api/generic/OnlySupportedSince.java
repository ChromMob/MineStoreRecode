package me.chrommob.minestore.addons.api.generic;

public class OnlySupportedSince extends UnsupportedOperationException {
    public OnlySupportedSince(MineStoreVersion currentVersion, MineStoreVersion sinceVersion) {
        super("This method is supported since version " + sinceVersion.toString() + ", but the current version is " + currentVersion.toString());
    }
}
