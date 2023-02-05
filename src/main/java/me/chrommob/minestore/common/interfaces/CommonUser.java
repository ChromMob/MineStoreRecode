package me.chrommob.minestore.common.interfaces;

public interface CommonUser {
    String getName();
    void sendMessage(String message);
    boolean hasPermission(String permission);

    boolean isOnline();
}
