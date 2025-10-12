package me.chrommob.minestore.api.interfaces.commands;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

public class CommonConsoleUser extends CommonUser {

    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public void sendMessage(String message) {
        Registries.LOGGER.get().log(message);
    }

    @Override
    public void sendTitle(Title title) {
    }

    @Override
    public void sendMessage(Component message) {
        Registries.LOGGER.get().log(PlainTextComponentSerializer.plainText().serialize(message));
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
    public InetSocketAddress getAddress() {
        return Registries.IP.get();
    }

    @Override
    public void openInventory(CommonInventory inventory) {
        Registries.LOGGER.get().log("Console can't open inventory");
    }

    @Override
    public void closeInventory() {
        Registries.LOGGER.get().log("Console can't close inventory");
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
