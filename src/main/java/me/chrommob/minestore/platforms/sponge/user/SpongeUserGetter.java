package me.chrommob.minestore.platforms.sponge.user;

import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.interfaces.user.UserGetter;
import org.spongepowered.api.Sponge;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpongeUserGetter implements UserGetter {
    @Override
    public CommonUser get(UUID uuid) {
        return new SpongeUser(uuid);
    }

    @Override
    public CommonUser get(String username) {
        return new SpongeUser(username);
    }

    @Override
    public Set<CommonUser> getAllPlayers() {
        Set<CommonUser> users = new HashSet<>();
        for (UUID uuid : Sponge.getServer().getOnlinePlayers().stream().map(org.spongepowered.api.entity.living.player.Player::getUniqueId).toArray(UUID[]::new)) {
            users.add(get(uuid));
        }
        return users;
    }
}
