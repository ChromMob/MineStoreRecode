package me.chrommob.minestore.common.interfaces;

import java.util.UUID;

public interface UserGetter {
    CommonUser get(UUID uuid);
}
