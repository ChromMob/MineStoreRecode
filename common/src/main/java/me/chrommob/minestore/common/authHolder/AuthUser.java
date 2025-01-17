package me.chrommob.minestore.common.authHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.api.interfaces.user.CommonUser;

public class AuthUser {
    private final MineStoreCommon plugin;
    private final CommonUser user;
    private final ParsedResponse parsedResponse;
    private long time;

    public AuthUser(MineStoreCommon plugin, CommonUser user, ParsedResponse parsedResponse, long time) {
        this.plugin = plugin;
        this.user = user;
        this.parsedResponse = parsedResponse;
        this.time = time;
        user.sendMessage(plugin.miniMessage().deserialize((String)plugin.configReader().get(ConfigKey.AUTH_INIT_MESSAGE)));
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
        plugin.authHolder().confirmAuth(this);
    }

    public ParsedResponse parsedResponse() {
        return parsedResponse;
    }
}
