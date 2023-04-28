package me.chrommob.minestore.common.interfaces.playerInfo;

import me.chrommob.minestore.common.interfaces.user.CommonUser;

public interface PlayerInfoProvider {
    String getGroup(CommonUser commonUser);
    String getPrefix(CommonUser commonUser);

    String getSuffix(CommonUser commonUser);
}
