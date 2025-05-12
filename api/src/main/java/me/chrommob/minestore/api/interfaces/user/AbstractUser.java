package me.chrommob.minestore.api.interfaces.user;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;

import java.util.UUID;

public class AbstractUser {
    private final CommonUser user;
    private final Object platformObject;

    public AbstractUser(UUID uniqueId, Object platformObject) {
        this.platformObject = platformObject;
        if (uniqueId == null) {
            user = new CommonConsoleUser();
        } else
            user = Registries.USER_GETTER.get().get(uniqueId);
    }

    public AbstractUser(String username, Object platformObject) {
        this.platformObject = platformObject;
        if (username == null) {
            user = new CommonConsoleUser();
        } else
            user = Registries.USER_GETTER.get().get(username);
    }

    public Object platformObject() {
        return platformObject;
    }

    public CommonUser user() {
        return user;
    }
}
