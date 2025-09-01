package me.chrommob.minestore.platforms.bungee.user;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.platforms.bungee.MineStoreBungee;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.UUID;

public class BungeeUser extends CommonUser {
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
        player.sendMessage(new TextComponent(message));
    }

    @Override
    public void sendTitle(Title title) {
        MineStoreBungee.getInstance().adventure().player(player).showTitle(title);
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

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public InetSocketAddress getAddress() {
        return player.getAddress();
    }

    @Override
    public void openInventory(CommonInventory inventory) {
        player.sendMessage(new TextComponent("Bungee not implemented yet"));
    }

    @Override
    public void closeInventory() {
        player.sendMessage(new TextComponent("Bungee not implemented yet"));
    }
}
