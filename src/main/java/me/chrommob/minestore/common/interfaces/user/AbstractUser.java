package me.chrommob.minestore.common.interfaces.user;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

import java.util.UUID;

public class AbstractUser {
    private final CommonUser user;

    public AbstractUser(UUID uniqueId) {
        if (uniqueId == null) {
            user = new CommonConsoleUser();
        } else
            user = MineStoreCommon.getInstance().userGetter().get(uniqueId);
    }

    public AbstractUser(String username) {
        if (username == null) {
            user = new CommonConsoleUser();
        } else
            user = MineStoreCommon.getInstance().userGetter().get(username);
    }

    public CommonUser user() {
        return user;
    }
}
