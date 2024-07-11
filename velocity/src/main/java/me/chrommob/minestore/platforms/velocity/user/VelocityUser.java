package me.chrommob.minestore.platforms.velocity.user;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocityUser extends CommonUser {
    private final Player player;

    public VelocityUser(UUID uuid, ProxyServer server, MineStoreCommon plugin) {
        super(plugin);
        player = server.getPlayer(uuid).get();
    }

    public VelocityUser(String username, ProxyServer server, MineStoreCommon plugin) {
        super(plugin);
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
    public void sendMessage(Component message) {
        player.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public boolean isOnline() {
        return player != null && player.isActive();
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public void openInventory(CommonInventory inventory) {
        player.sendMessage(Component.text("Velocity not implemented yet"));
    }

    @Override
    public void closeInventory() {
        player.sendMessage(Component.text("Velocity not implemented yet"));
    }
}
