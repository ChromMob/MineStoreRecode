package me.chrommob.minestore.platforms.bungee.user;

import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.interfaces.user.UserGetter;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BungeeUserGetter implements UserGetter {
    private final Plugin mineStoreBungee;
    public BungeeUserGetter(Plugin mineStoreBungee) {
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

    @Override
    public Set<CommonUser> getAllPlayers() {
        Set<CommonUser> users = new HashSet<>();
        for (UUID uuid : mineStoreBungee.getProxy().getPlayers().stream().map(net.md_5.bungee.api.connection.ProxiedPlayer::getUniqueId).toArray(UUID[]::new)) {
            users.add(get(uuid));
        }
        return users;
    }
}
