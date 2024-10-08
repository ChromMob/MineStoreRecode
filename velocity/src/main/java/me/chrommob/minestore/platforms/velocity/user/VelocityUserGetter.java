package me.chrommob.minestore.platforms.velocity.user;

import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VelocityUserGetter implements UserGetter {
    private final ProxyServer server;
    private final MineStoreCommon common;

    public VelocityUserGetter(ProxyServer server, MineStoreCommon common) {
        this.server = server;
        this.common = common;
    }

    @Override
    public CommonUser get(UUID uuid) {
        if (server.getPlayer(uuid).isPresent()) {
            return new VelocityUser(uuid, server, common);
        } else {
            return new CommonConsoleUser(common);
        }
    }

    @Override
    public CommonUser get(String username) {
        return new VelocityUser(username, server, common);
    }

    @Override
    public Set<CommonUser> getAllPlayers() {
        Set<CommonUser> users = new HashSet<>();
        for (UUID uuid : server.getAllPlayers().stream().map(com.velocitypowered.api.proxy.Player::getUniqueId).toArray(UUID[]::new)) {
            users.add(get(uuid));
        }
        return users;
    }
}
