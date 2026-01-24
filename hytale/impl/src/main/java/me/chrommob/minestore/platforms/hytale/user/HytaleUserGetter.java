package me.chrommob.minestore.platforms.hytale.user;

import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.UserGetter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HytaleUserGetter implements UserGetter {
    private final Universe universe = Universe.get();

    @Override
    public AbstractUser get(UUID uuid) {
        PlayerRef player = universe.getPlayer(uuid);
        return new AbstractUser(player == null ? new CommonConsoleUser() : new HytaleUser(player), player);
    }

    @Override
    public AbstractUser get(String s) {
        PlayerRef player = universe.getPlayerByUsername(s, NameMatching.EXACT_IGNORE_CASE);
        return new AbstractUser(player == null ? new CommonConsoleUser() : new HytaleUser(player), player);
    }

    @Override
    public Set<AbstractUser> getAllPlayers() {
        List<PlayerRef> players = universe.getPlayers();
        Set<AbstractUser> users = new HashSet<>();
        for (PlayerRef player : players) {
            users.add(new AbstractUser(player == null ? new CommonConsoleUser() : new HytaleUser(player), player));
        }
        return users;
    }
}
