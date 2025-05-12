package me.chrommob.minestore.api.interfaces.user;

import java.util.Set;
import java.util.UUID;

public interface UserGetter {
    AbstractUser get(UUID uuid);

    AbstractUser get(String username);

    Set<AbstractUser> getAllPlayers();
}
