package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BukkitUserGetter implements UserGetter {
    private final MineStoreBukkit mineStoreBukkit;

    public BukkitUserGetter(MineStoreBukkit mineStoreBukkit) {
        this.mineStoreBukkit = mineStoreBukkit;
    }

    @Override
    public CommonUser get(UUID uuid) {
        return new UserBukkit(uuid, mineStoreBukkit);
    }

    @Override
    public CommonUser get(String username) {
        return new UserBukkit(username, mineStoreBukkit);
    }

    @Override
    public Set<CommonUser> getAllPlayers() {
        Set<CommonUser> users = new HashSet<>();
        for (UUID uuid : mineStoreBukkit.getServer().getOnlinePlayers().stream()
                .map(org.bukkit.entity.Player::getUniqueId).toArray(UUID[]::new)) {
            users.add(get(uuid));
        }
        return users;
    }
}
