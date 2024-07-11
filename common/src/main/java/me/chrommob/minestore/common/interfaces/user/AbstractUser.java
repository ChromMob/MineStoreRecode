package me.chrommob.minestore.common.interfaces.user;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;

import java.util.UUID;

public class AbstractUser {
    private final CommonUser user;

    public AbstractUser(UUID uniqueId, MineStoreCommon plugin) {
        if (uniqueId == null) {
            user = new CommonConsoleUser(plugin);
        } else
            user = plugin.userGetter().get(uniqueId);
    }

    public AbstractUser(String username, MineStoreCommon plugin) {
        if (username == null) {
            user = new CommonConsoleUser(plugin);
        } else
            user = plugin.userGetter().get(username);
    }

    public CommonUser user() {
        return user;
    }
}
