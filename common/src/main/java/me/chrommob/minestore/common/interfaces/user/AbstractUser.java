package me.chrommob.minestore.common.interfaces.user;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;

import java.util.UUID;

public class AbstractUser {
    private final CommonUser user;
    private final Object nativeCommandSender;

    public AbstractUser(UUID uniqueId, MineStoreCommon plugin, Object nativeCommandSender) {
        this.nativeCommandSender = nativeCommandSender;
        if (uniqueId == null) {
            user = new CommonConsoleUser(plugin);
        } else
            user = plugin.userGetter().get(uniqueId);
    }

    public AbstractUser(String username, MineStoreCommon plugin, Object nativeCommandSender) {
        this.nativeCommandSender = nativeCommandSender;
        if (username == null) {
            user = new CommonConsoleUser(plugin);
        } else
            user = plugin.userGetter().get(username);
    }

    public Object nativeCommandSender() {
        return nativeCommandSender;
    }

    public CommonUser user() {
        return user;
    }
}
