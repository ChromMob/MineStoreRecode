package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
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

    private Enchantment getDurabilityEnchantment() {
        Enchantment enchantment = null;
        if (Enchantment.getByName("DURABILITY") != null) {
            enchantment = Enchantment.getByName("DURABILITY");
        } else {
            enchantment = Enchantment.getByName("UNBREAKING");
        }
        return enchantment;
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
        Inventory bukkitInventory = Bukkit.createInventory(null, inventory.getSize(),
                serializer.serialize(inventory.getTitle()));
        player.openInventory(bukkitInventory);
        List<ItemStack> bukkitItems = new ArrayList<>();
        for (CommonItem item : inventory.getItems()) {
            Material material;
            if (item.getMaterial() == null) {
                if (item.isBackground()) {
                    material = Material.WHITE_STAINED_GLASS_PANE;
                } else {
                    material = Material.CHEST;
                }
            } else {
                material = Material.matchMaterial(item.getMaterial());
            }
            if (material == null) {
                if (item.isBackground()) {
                    material = Material.WHITE_STAINED_GLASS_PANE;
                    item.setMaterial("STAINED_GLASS_PANE");
                } else {
                    material = Material.CHEST;
                    item.setMaterial("CHEST");
                }
            }
            ItemStack bukkitItem = new ItemStack(material, item.getAmount());
            ItemMeta meta = bukkitItem.getItemMeta();
            LegacyComponentSerializer serializer = BukkitComponentSerializer.legacy();
            List<String> lore = new ArrayList<>();
            for (Component line : item.getLore()) {
                lore.add(serializer.serialize(line));
            }
            meta.setDisplayName(serializer.serialize(item.getName()));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (item.isFeatured()) {
                meta.addEnchant(getDurabilityEnchantment(), 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            bukkitItem.setItemMeta(meta);
            bukkitItems.add(bukkitItem);
        }
        bukkitInventory.setContents(bukkitItems.toArray(new ItemStack[0]));
        player.openInventory(bukkitInventory);
    }

    @Override
    public void closeInventory() {
        player.closeInventory();
    }
}