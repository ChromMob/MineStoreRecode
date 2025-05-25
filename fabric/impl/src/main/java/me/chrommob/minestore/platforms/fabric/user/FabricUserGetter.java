package me.chrommob.minestore.platforms.fabric.user;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.interfaces.user.UserGetter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FabricUserGetter implements UserGetter {
    private PlayerManager pManager;
    private final MinecraftServer server;

    public FabricUserGetter(MinecraftServer server) {
        this.pManager = server.getPlayerManager();
        this.server = server;
    }

    @Override
    public AbstractUser get(UUID uuid) {
        ServerPlayerEntity player = this.pManager.getPlayer(uuid);
        return get(player);
    }

    @Override
    public AbstractUser get(String username) {
        ServerPlayerEntity player = this.pManager.getPlayer(username);
        return get(player);
    }

    private AbstractUser get(ServerPlayerEntity player) {
        CommonUser user = player == null ? new CommonConsoleUser() : new UserFabric(player);
        return new AbstractUser(user, player == null ? server.getCommandSource() : player);
    }

    @Override
    public Set<AbstractUser> getAllPlayers() {
        Set<AbstractUser> users = new HashSet<>();
        for (ServerPlayerEntity player : this.pManager.getPlayerList()) {
            users.add(get(player));
        }
        return users;
    }

}
