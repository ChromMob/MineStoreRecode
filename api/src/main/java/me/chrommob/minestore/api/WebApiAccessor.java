package me.chrommob.minestore.api;

import me.chrommob.minestore.api.giftcard.GiftCardManager;
import me.chrommob.minestore.api.profile.ProfileManager;

public class WebApiAccessor {
    private static final GiftCardManager giftCardManager = new GiftCardManager();
    private static final ProfileManager profileManager = new ProfileManager();

    public static GiftCardManager giftCardManager() {
        return giftCardManager;
    }

    public static ProfileManager profileManager() {
        return profileManager;
    }
}