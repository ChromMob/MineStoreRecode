package me.chrommob.minestore.common.interfaces.user;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface CommonUser {
    String getName();

    void sendMessage(String message);

    void sendMessage(Component message);

    boolean hasPermission(String permission);

    boolean isOnline();

    UUID getUUID();

    String getPrefix();

    String getSuffix();

    double getBalance();

    String getGroup();
}
