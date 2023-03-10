package me.chrommob.minestore.platforms.sponge.user;

import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.platforms.sponge.MineStoreSponge;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.UUID;

public class SpongeUser extends CommonUser {
    private final Player player;

    public SpongeUser(UUID uuid) {
        player = Sponge.getServer().getPlayer(uuid).get();
    }

    public SpongeUser(String username) {
        player = Sponge.getServer().getPlayer(username).get();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(Text.of(message));
    }

    @Override
    public void sendMessage(Component message) {
        MineStoreSponge.getInstance().adventure().player(player).sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public boolean isOnline() {
        return player != null && player.isOnline();
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public void openInventory(CommonInventory inventory) {
        player.sendMessage(Text.of("Sponge not implemented yet"));
    }

    @Override
    public void closeInventory() {
        player.sendMessage(Text.of("Sponge not implemented yet"));
    }
}
