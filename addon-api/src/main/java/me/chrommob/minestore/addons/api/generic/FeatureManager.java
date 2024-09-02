package me.chrommob.minestore.addons.api.generic;

public class FeatureManager {
    private final AuthData authData;

    public FeatureManager(AuthData authData) {
        this.authData = authData;
    }

    public final AuthData authData() {
        return authData;
    }
}
