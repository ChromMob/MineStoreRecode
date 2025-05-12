package me.chrommob.minestore.api.interfaces.user;

public class AbstractUser {
    private final CommonUser user;
    private final Object platformObject;

    public AbstractUser(CommonUser user, Object platformObject) {
        this.platformObject = platformObject;
        this.user = user;
    }

    public Object platformObject() {
        return platformObject;
    }

    public CommonUser commonUser() {
        return user;
    }
}
