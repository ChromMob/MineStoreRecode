package me.chrommob.minestore.common.command.types;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class CommonConsoleUser implements CommonUser {
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
}
