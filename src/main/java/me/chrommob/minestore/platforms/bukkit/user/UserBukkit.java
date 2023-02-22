package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UserBukkit extends CommonUser {
    private final Player player;

    public UserBukkit(UUID uuid, MineStoreBukkit mineStoreBukkit) {
        player = mineStoreBukkit.getServer().getPlayer(uuid);
    }

    public UserBukkit(String username, MineStoreBukkit mineStoreBukkit) {
        player = mineStoreBukkit.getServer().getPlayer(username);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public void sendMessage(Component message) {
        MineStoreBukkit.getInstance().adventure().player(player).sendMessage(message);
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
        Inventory bukkitInventory = Bukkit.createInventory(null, inventory.getSize());
        List<ItemStack> bukkitItems = new ArrayList<>();
        for (CommonItem item : inventory.getItems()) {
            Material material = null;
            if (item.getMaterial() != null) {
                material = Material.matchMaterial(item.getMaterial());
            }
            if (material == null) {
                material = Material.BARRIER;
            }
            ItemStack bukkitItem = new ItemStack(material);
            ItemMeta bukkitItemMeta = bukkitItem.getItemMeta();
            bukkitItemMeta.setDisplayName(item.getName().toString());
            List<String> bukkitLore = new ArrayList<>();
            for (Component lore : item.getLore()) {
                bukkitLore.add(lore.toString());
            }
            bukkitItemMeta.setLore(bukkitLore);
        }
        //Convert List<ItemStack> to ItemStack[]
        bukkitInventory.setContents(bukkitItems.toArray(new ItemStack[0]));
        player.openInventory(bukkitInventory);
    }
}