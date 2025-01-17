package me.chrommob.minestore.platforms.fabric.user;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.interfaces.user.UserGetter;
import me.chrommob.minestore.common.MineStoreCommon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

public class FabricUserGetter implements UserGetter {
    private PlayerManager pManager;
    private final MinecraftServer server;

    public FabricUserGetter(MinecraftServer server) {
        this.pManager = server.getPlayerManager();
        this.server = server;
    }

    @Override
    public CommonUser get(UUID uuid) {
        return new UserFabric(this.pManager.getPlayer(uuid));
    }

    @Override
    public CommonUser get(String username) {
        return new UserFabric(this.pManager.getPlayer(username));
    }

    @Override
    public Set<CommonUser> getAllPlayers() {
        Set<CommonUser> users = new HashSet<>();
        for (ServerPlayerEntity player : this.pManager.getPlayerList()) {
            users.add(new UserFabric(player));
        }
        return users;
    }

}
