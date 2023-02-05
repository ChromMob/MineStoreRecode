package me.chrommob.minestore.common.authHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.interfaces.CommonUser;

public class AuthUser {
    private final CommonUser user;
    private final ParsedResponse parsedResponse;
    private long time;
    public AuthUser(CommonUser user, ParsedResponse parsedResponse, long time) {
        this.user = user;
        this.parsedResponse = parsedResponse;
        this.time = time;
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

    public ParsedResponse parsedResponse() {
        return parsedResponse;
    }
}
