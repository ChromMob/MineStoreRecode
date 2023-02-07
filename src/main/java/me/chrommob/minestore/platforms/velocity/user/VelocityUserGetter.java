package me.chrommob.minestore.platforms.velocity.user;

import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.interfaces.CommonUser;
import me.chrommob.minestore.common.interfaces.UserGetter;

import java.util.UUID;

public class VelocityUserGetter implements UserGetter {
    private final ProxyServer server;

    public VelocityUserGetter(ProxyServer server) {
        this.server = server;
    }

    @Override
    public CommonUser get(UUID uuid) {
        return new VelocityUser(uuid, server);
    }

    @Override
    public CommonUser get(String username) {
        return new VelocityUser(username, server);
    }
}
