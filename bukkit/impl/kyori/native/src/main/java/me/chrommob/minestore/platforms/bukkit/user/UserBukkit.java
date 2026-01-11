package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.api.event.types.GuiCloseEvent;
import me.chrommob.minestore.api.event.types.GuiOpenEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

import java.net.InetSocketAddress;
import java.util.UUID;

public class UserBukkit extends CommonUser {
    private final Player player;
    private final String name;
    private final LegacyComponentSerializer serializer = BukkitComponentSerializer.legacy();

    public UserBukkit(Player player) {
        this.player = player;
        if (player == null) {
            name = "";
            return;
        }
        name = player.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendMessage(String message) {
        if (player == null) {
            return;
        }
        player.sendMessage(message);
    }

    @Override
    public void sendTitle(Title title) {
        if (player == null) {
            return;
        }
        player.showTitle(title);
    }

    @Override
    public void sendMessage(Component message) {
        if (player == null) {
            return;
        }
        player.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        if (player == null) {
            return false;
        }
        return player.hasPermission(permission);
    }

    @Override
    public boolean isOnline() {
        if (player == null) {
            return false;
        }
        return player.isOnline();
    }

    @Override
    public UUID getUUID() {
        if (player == null) {
            return null;
        }
        return player.getUniqueId();
    }

    @Override
    public InetSocketAddress getAddress() {
        return player.getAddress();
    }

    @Override
    public void openInventory(CommonInventory inventory) {
        if (player == null) {
            return;
        }
        MineStoreInventoryHolder holder = new MineStoreInventoryHolder(inventory, serializer);
        player.openInventory(holder.getInventory());
        new GuiOpenEvent(this, inventory).call();
    }

    @Override
    public void closeInventory() {
        if (player == null) {
            return;
        }
        InventoryView view = player.getOpenInventory();
        InventoryHolder holder = view.getTopInventory().getHolder();
        if (holder instanceof MineStoreInventoryHolder) {
            CommonInventory inventory = ((MineStoreInventoryHolder) holder).getCommonInventory();
            new GuiCloseEvent(this, inventory).call();
        }
        player.closeInventory();
    }
}