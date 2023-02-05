package me.chrommob.minestore.common.interfaces;

import net.kyori.adventure.text.Component;

public interface CommonUser {
    String getName();

    void sendMessage(String message);

    void sendMessage(Component message);

    boolean hasPermission(String permission);

    boolean isOnline();
}
