package me.chrommob.minestore.platforms.velocity.user;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.interfaces.user.UserGetter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VelocityUserGetter implements UserGetter {
    private final ProxyServer server;

    public VelocityUserGetter(ProxyServer server) {
        this.server = server;
    }

    @Override
    public AbstractUser get(UUID uuid) {
        Player player = server.getPlayer(uuid).orElse(null);
        return get(player);
    }

    @Override
    public AbstractUser get(String username) {
        Player player = server.getPlayer(username).orElse(null);
        return get(player);
    }

    private AbstractUser get(Player player) {
        CommonUser user = player == null ? new CommonConsoleUser() : new VelocityUser(player);
        return new AbstractUser(user, player == null ? server.getConsoleCommandSource() : player);
    }

    @Override
    public Set<AbstractUser> getAllPlayers() {
        Set<AbstractUser> users = new HashSet<>();
        for (UUID uuid : server.getAllPlayers().stream().map(com.velocitypowered.api.proxy.Player::getUniqueId).toArray(UUID[]::new)) {
            users.add(get(uuid));
        }
        return users;
    }
}
