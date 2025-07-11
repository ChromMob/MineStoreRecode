package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.interfaces.user.UserGetter;
import me.chrommob.minestore.common.MineStoreCommon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BukkitUserGetter implements UserGetter {
    private final static MethodHandle mh;
    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType constructorType = MethodType.methodType(void.class, Player.class);
        Class<?> MyClass;
        MethodHandle constructorHandle = null;
        try {
            MyClass = Class.forName("me.chrommob.minestore.platforms.bukkit.user.UserBukkit");
            constructorHandle = lookup.findConstructor(MyClass, constructorType);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        mh = constructorHandle;
    }
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
        try {
            CommonUser user = player == null ? new CommonConsoleUser() : (CommonUser) mh.invoke(player);
            return new AbstractUser(user, player == null ? mineStoreBukkit.getServer().getConsoleSender() : player);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return new AbstractUser(new CommonConsoleUser(), player == null ? mineStoreBukkit.getServer().getConsoleSender() : player);
        }
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
