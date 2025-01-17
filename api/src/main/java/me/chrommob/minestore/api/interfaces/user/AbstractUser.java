package me.chrommob.minestore.api.interfaces.user;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;

import java.util.UUID;

public class AbstractUser {
    private final CommonUser user;
    private final Object nativeCommandSender;

    public AbstractUser(UUID uniqueId, Object nativeCommandSender) {
        this.nativeCommandSender = nativeCommandSender;
        if (uniqueId == null) {
            user = new CommonConsoleUser();
        } else
            user = Registries.USER_GETTER.get().get(uniqueId);
    }

    public AbstractUser(String username, Object nativeCommandSender) {
        this.nativeCommandSender = nativeCommandSender;
        if (username == null) {
            user = new CommonConsoleUser();
        } else
            user = Registries.USER_GETTER.get().get(username);
    }

    public Object nativeCommandSender() {
        return nativeCommandSender;
    }

    public CommonUser user() {
        return user;
    }
}
