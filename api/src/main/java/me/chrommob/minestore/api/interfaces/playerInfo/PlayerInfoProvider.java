package me.chrommob.minestore.api.interfaces.playerInfo;

import me.chrommob.minestore.api.interfaces.user.CommonUser;

public interface PlayerInfoProvider {
    String getGroup(CommonUser commonUser);
    String getPrefix(CommonUser commonUser);

    String getSuffix(CommonUser commonUser);
}
