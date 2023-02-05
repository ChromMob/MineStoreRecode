package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.common.interfaces.CommonUser;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UserBukkit implements CommonUser {
    private final Player player;
    public UserBukkit(UUID uuid, MineStoreBukkit mineStoreBukkit) {
        player = mineStoreBukkit.getServer().getPlayer(uuid);
    }

    public UserBukkit(String username, MineStoreBukkit mineStoreBukkit) {
        player = mineStoreBukkit.getServer().getPlayer(username);
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
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public boolean isOnline() {
        return player != null && player.isOnline();
    }
}
