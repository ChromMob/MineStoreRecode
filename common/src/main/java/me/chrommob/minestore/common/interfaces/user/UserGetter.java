package me.chrommob.minestore.common.interfaces.user;

import java.util.Set;
import java.util.UUID;

public interface UserGetter {
    CommonUser get(UUID uuid);

    CommonUser get(String username);

    Set<CommonUser> getAllPlayers();
}
