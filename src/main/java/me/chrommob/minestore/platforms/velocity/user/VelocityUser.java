package me.chrommob.minestore.platforms.velocity.user;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.interfaces.CommonUser;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocityUser implements CommonUser {
    private final Player player;

    public VelocityUser(UUID uuid, ProxyServer server) {
        player = server.getPlayer(uuid).get();
    }

    public VelocityUser(String username, ProxyServer server) {
        player = server.getPlayer(username).get();
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(Component.text(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public boolean isOnline() {
        return player != null && player.isActive();
    }
}
