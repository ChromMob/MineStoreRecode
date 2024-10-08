package me.chrommob.minestore.addons.api;

import me.chrommob.minestore.addons.api.giftcard.GiftCardManager;
import me.chrommob.minestore.addons.api.generic.AuthData;
import me.chrommob.minestore.addons.api.generic.MineStoreVersion;
import me.chrommob.minestore.addons.api.generic.OnlySupportedSince;
import me.chrommob.minestore.addons.api.profile.ProfileManager;

public class WebApiAccessor {
    private static GiftCardManager giftCardManager;
    private static ProfileManager profileManager;
    private static MineStoreVersion version;
    private static AuthData authData;
    public static void setAuthData(String storeUrl, String apiKey) {
        if (!storeUrl.endsWith("/")) {
            storeUrl += "/";
        }
        if (!storeUrl.startsWith("https://")) {
            storeUrl = "https://" + storeUrl;
        }
        authData = new AuthData(storeUrl, apiKey);
        version = MineStoreVersion.getMineStoreVersion(storeUrl);
    }

    /**
     * @return GiftCardManager instance.
     * @throws IllegalStateException If the version is not initialized.
     * @throws OnlySupportedSince If the version is not supported.
     * @throws IllegalStateException If the authData is not initialized.
    **/
    public static GiftCardManager giftCardManager() throws IllegalStateException, OnlySupportedSince {
        isVersionChecked();
        if (giftCardManager == null || !giftCardManager.authData().equals(authData)) {
            giftCardManager = new GiftCardManager(authData);
        }
        return giftCardManager;
    }

    public static ProfileManager profileManager() throws IllegalStateException, OnlySupportedSince {
        isVersionChecked();
        if (profileManager == null || !profileManager.authData().equals(authData)) {
            profileManager = new ProfileManager(authData);
        }
        return profileManager;
    }

    private static void isVersionChecked() throws OnlySupportedSince {
        if (version == null) {
            throw new IllegalStateException("Version is not initialized!");
        }
        if (!version.requires("3.0.0")) {
            throw new OnlySupportedSince(version, new MineStoreVersion("3.0.0"));
        }
        if (authData == null) {
            throw new IllegalStateException("AuthData is not initialized!");
        }
    }
}