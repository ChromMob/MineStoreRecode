package me.chrommob.minestore.platforms.hytale.user;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HytaleUser extends CommonUser {
    private final PlayerRef player;
    public HytaleUser(PlayerRef player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public void sendMessage(String s) {
        player.sendMessage(Message.raw(s));
    }

    @Override
    public void sendTitle(Title title) {
        if (player.getWorldUuid() == null) {
            return;
        }
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return;
        }
        world.execute(() -> EventTitleUtil.showEventTitleToPlayer(player, Message.raw(PlainTextComponentSerializer.plainText().serialize(title.title())), Message.raw(LegacyComponentSerializer.legacySection().serialize(title.subtitle())), false));
    }

    @Override
    public void sendMessage(Component component) {
        player.sendMessage(Message.raw(PlainTextComponentSerializer.plainText().serialize(component)));
    }

    @Override
    public boolean hasPermission(String s) {
        if (player.getReference() == null) {
            return false;
        }
        if (player.getWorldUuid() == null) {
            return false;
        }
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return false;
        }
        CompletableFuture<Boolean> res = new CompletableFuture<>();
        world.execute(() -> {
            Player playerR = player.getReference().getStore().getComponent(player.getReference(), Player.getComponentType());
            if (playerR == null) {
                res.complete(false);
                return;
            }
            res.complete(playerR.hasPermission(s));
        });
        return res.join();
    }

    @Override
    public boolean isOnline() {
        return player.isValid();
    }

    @Override
    public UUID getUUID() {
        return player.getUuid();
    }

    @Override
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(0);
    }

    @Override
    public void openInventory(CommonInventory commonInventory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeInventory() {
        throw new UnsupportedOperationException();
    }
}
