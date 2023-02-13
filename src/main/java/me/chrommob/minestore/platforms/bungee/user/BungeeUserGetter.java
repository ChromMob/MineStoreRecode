package me.chrommob.minestore.platforms.bungee.user;

import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import me.chrommob.minestore.platforms.bungee.MineStoreBungee;

import java.util.UUID;

public class BungeeUserGetter implements UserGetter {
    private final MineStoreBungee mineStoreBungee;

    public BungeeUserGetter(MineStoreBungee mineStoreBungee) {
        this.mineStoreBungee = mineStoreBungee;
    }

    @Override
    public CommonUser get(UUID uuid) {
        return new BungeeUser(mineStoreBungee.getProxy().getPlayer(uuid));
    }

    @Override
    public CommonUser get(String username) {
        return new BungeeUser(mineStoreBungee.getProxy().getPlayer(username));
    }
}
