package me.chrommob.minestore.common.authHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.JsonRoot;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

public class AuthUser {
    private final CommonUser user;
    private final JsonRoot parsedResponse;
    private long time;

    public AuthUser(CommonUser user, JsonRoot parsedResponse, long time) {
        this.user = user;
        this.parsedResponse = parsedResponse;
        this.time = time;
        user.sendMessage(MineStoreCommon.getInstance().miniMessage().deserialize((String)MineStoreCommon.getInstance().configReader().get(ConfigKey.AUTH_INIT_MESSAGE)));
    }

    public long time() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public CommonUser user() {
        return user;
    }

    public void confirmAuth() {
        MineStoreCommon.getInstance().authHolder().confirmAuth(this);
    }

    public JsonRoot parsedResponse() {
        return parsedResponse;
    }
}
