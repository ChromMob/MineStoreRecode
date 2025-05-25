package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.interfaces.user.UserGetter;
import me.chrommob.minestore.common.MineStoreCommon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BukkitUserGetter implements UserGetter {
    private final JavaPlugin mineStoreBukkit;

    public BukkitUserGetter(JavaPlugin mineStoreBukkit) {
        this.mineStoreBukkit = mineStoreBukkit;
    }

    @Override
    public AbstractUser get(UUID uuid) {
        Player player = mineStoreBukkit.getServer().getPlayer(uuid);
        return get(player);
    }

    @Override
    public AbstractUser get(String username) {
        Player player = mineStoreBukkit.getServer().getPlayer(username);
        return get(player);
    }

    private AbstractUser get(Player player) {
        CommonUser user = player == null ? new CommonConsoleUser() : new UserBukkit(player);
        return new AbstractUser(user, player == null ? mineStoreBukkit.getServer().getConsoleSender() : player);
    }

    @Override
    public Set<AbstractUser> getAllPlayers() {
        Set<AbstractUser> users = new HashSet<>();
        for (UUID uuid : mineStoreBukkit.getServer().getOnlinePlayers().stream()
                .map(org.bukkit.entity.Player::getUniqueId).toArray(UUID[]::new)) {
            users.add(get(uuid));
        }
        return users;
    }
}
