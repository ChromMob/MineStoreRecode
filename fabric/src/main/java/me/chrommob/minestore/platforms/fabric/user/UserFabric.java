package me.chrommob.minestore.platforms.fabric.user;

import java.util.UUID;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;

public class UserFabric extends CommonUser {
    private final ServerPlayerEntity player;

    public UserFabric(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName().getString();
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
        return false;
    }

    @Override
    public boolean isOnline() {
        return player != null;
    }

    @Override
    public UUID getUUID() {
        return player.getUuid();
    }

    @Override
    public void openInventory(CommonInventory inventory) {
        // This is unimplemented on Fabric for now
        throw new UnsupportedOperationException("Unimplemented method 'openInventory'");
    }

    @Override
    public void closeInventory() {
        // This is unimplemented on Fabric for now
        throw new UnsupportedOperationException("Unimplemented method 'closeInventory'");
    }

}
