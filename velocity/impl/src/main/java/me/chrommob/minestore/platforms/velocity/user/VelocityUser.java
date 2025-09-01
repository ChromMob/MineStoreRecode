package me.chrommob.minestore.platforms.velocity.user;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.net.InetSocketAddress;
import java.util.UUID;

public class VelocityUser extends CommonUser {
    private final Player player;

    public VelocityUser(Player player) {
        this.player = player;
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
    public void sendTitle(Title title) {
        player.showTitle(title);
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
    public InetSocketAddress getAddress() {
        return player.getRemoteAddress();
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
