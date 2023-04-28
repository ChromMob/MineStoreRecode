package me.chrommob.minestore.common.interfaces.playerInfo;

import me.chrommob.minestore.common.interfaces.user.CommonUser;

public class DefaultPlayerInfoProvider implements PlayerInfoProvider {
    @Override
    public String getGroup(CommonUser commonUser) {
        return "";
    }

    @Override
    public String getPrefix(CommonUser commonUser) {
        return "";
    }

    @Override
    public String getSuffix(CommonUser commonUser) {
        return "";
    }
}
