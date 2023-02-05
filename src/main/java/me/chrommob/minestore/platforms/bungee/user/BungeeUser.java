package me.chrommob.minestore.platforms.bungee.user;

import me.chrommob.minestore.common.interfaces.CommonUser;
import me.chrommob.minestore.platforms.bungee.MineStoreBungee;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeUser implements CommonUser {
    private final ProxiedPlayer player;
    public BungeeUser(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public void sendMessage(Component message) {
        MineStoreBungee.getInstance().adventure().player(player).sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public boolean isOnline() {
        return player != null && player.isConnected();
    }
}
