package me.chrommob.minestore.platforms.bungee.user;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.interfaces.user.UserGetter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
    public AbstractUser get(UUID uuid) {
        ProxiedPlayer player = mineStoreBungee.getProxy().getPlayer(uuid);
        return get(player);
    }

    @Override
    public AbstractUser get(String username) {
        ProxiedPlayer player = mineStoreBungee.getProxy().getPlayer(username);
        return get(player);
    }

    private AbstractUser get(ProxiedPlayer player) {
        CommonUser user = player == null ? new CommonConsoleUser() : new BungeeUser(player);
        return new AbstractUser(user, player == null ? mineStoreBungee.getProxy().getConsole() : player);
    }

    @Override
    public Set<AbstractUser> getAllPlayers() {
        Set<AbstractUser> users = new HashSet<>();
        for (UUID uuid : mineStoreBungee.getProxy().getPlayers().stream().map(ProxiedPlayer::getUniqueId).toArray(UUID[]::new)) {
            users.add(get(uuid));
        }
        return users;
    }
}
