package me.chrommob.minestore.platforms.sponge.user;

import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;

import java.util.UUID;

public class SpongeUserGetter implements UserGetter {
    @Override
    public CommonUser get(UUID uuid) {
        return new SpongeUser(uuid);
    }

    @Override
    public CommonUser get(String username) {
        return new SpongeUser(username);
    }
}
