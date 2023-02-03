package me.chrommob.minestore.platforms.sponge.user;

import me.chrommob.minestore.common.interfaces.CommonUser;
import me.chrommob.minestore.common.interfaces.UserGetter;

import java.util.UUID;

public class SpongeUserGetter implements UserGetter {
    @Override
    public CommonUser get(UUID uuid) {
        return new SpongeUser(uuid);
    }
}
