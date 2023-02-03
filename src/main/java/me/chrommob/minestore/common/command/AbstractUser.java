package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.CommonUser;

import java.util.UUID;

public class AbstractUser {
    private final CommonUser user;

    public AbstractUser(UUID uniqueId) {
        if (uniqueId == null) {
            user = new CommonConsoleUser();
        } else
            user = MineStoreCommon.getInstance().userGetter().get(uniqueId);
    }

    public CommonUser user() {
        return user;
    }
}
