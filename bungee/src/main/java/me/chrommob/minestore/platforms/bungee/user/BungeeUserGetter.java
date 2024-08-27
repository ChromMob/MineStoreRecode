package me.chrommob.minestore.platforms.bungee.user;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import me.chrommob.minestore.platforms.bungee.MineStoreBungee;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BungeeUserGetter implements UserGetter {
    private final MineStoreBungee mineStoreBungee;
    private final MineStoreCommon plugin;

    public BungeeUserGetter(MineStoreBungee mineStoreBungee, MineStoreCommon pl) {
        this.mineStoreBungee = mineStoreBungee;
        this.plugin = pl;
    }

    @Override
    public CommonUser get(UUID uuid) {
        return new BungeeUser(mineStoreBungee.getProxy().getPlayer(uuid), plugin);
    }

    @Override
    public CommonUser get(String username) {
        return new BungeeUser(mineStoreBungee.getProxy().getPlayer(username), plugin);
    }

    @Override
    public Set<CommonUser> getAllPlayers() {
        Set<CommonUser> users = new HashSet<>();
        for (UUID uuid : mineStoreBungee.getProxy().getPlayers().stream().map(net.md_5.bungee.api.connection.ProxiedPlayer::getUniqueId).toArray(UUID[]::new)) {
            users.add(get(uuid));
        }
        return users;
    }
}
