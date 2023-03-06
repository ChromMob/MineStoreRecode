package me.chrommob.minestore.common.command.types;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.UUID;

public class CommonConsoleUser extends CommonUser {
    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public void sendMessage(String message) {
        MineStoreCommon.getInstance().log(message);
    }

    @Override
    public void sendMessage(Component message) {
        MineStoreCommon.getInstance().log(PlainTextComponentSerializer.plainText().serialize(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public UUID getUUID() {
        return UUID.randomUUID();
    }

    @Override
    public void openInventory(CommonInventory inventory) {
        MineStoreCommon.getInstance().log("Console can't open inventory");
    }

    @Override
    public void closeInventory() {
        MineStoreCommon.getInstance().log("Console can't close inventory");
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getSuffix() {
        return "";
    }

    @Override
    public double getBalance() {
        return 0;
    }

    @Override
    public String getGroup() {
        return "";
    }
}
