package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;

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
}
