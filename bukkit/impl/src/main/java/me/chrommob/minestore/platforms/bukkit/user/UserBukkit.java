package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserBukkit extends CommonUser {
    private final MineStoreCommon plugin;
    private final Player player;
    private final String name;
    private final UUID uuid;
    private LegacyComponentSerializer serializer = BukkitComponentSerializer.legacy();

    public UserBukkit(UUID uuid, JavaPlugin mineStoreBukkit, MineStoreCommon plugin) {
        this.plugin = plugin;
        player = mineStoreBukkit.getServer().getPlayer(uuid);
        this.uuid = uuid;
        if (player == null) {
            name = null;
            return;
        }
        name = player.getName();
    }

    public UserBukkit(String username, JavaPlugin mineStoreBukkit, MineStoreCommon plugin) {
        this.plugin = plugin;
        player = mineStoreBukkit.getServer().getPlayer(username);
        name = username;
        if (player == null) {
            uuid = null;
            return;
        }
        uuid = player.getUniqueId();
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
        MineStoreBukkit.getInstance().adventure().player(player).showTitle(title);
    }

    @Override
    public void sendMessage(Component message) {
        if (player == null) {
            return;
        }
        MineStoreBukkit.getInstance().adventure().player(player).sendMessage(message);
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
        return player != null && player.isOnline();
    }

    @Override
    public UUID getUUID() {
        return uuid;
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
            Material material = null;
            if (item.getMaterial() == null) {
                if (item.isBackground()) {
                    material = Material.STAINED_GLASS_PANE;
                } else {
                    material = Material.CHEST;
                }
            } else {
                material = Material.matchMaterial(item.getMaterial());
            }
            if (material == null) {
                plugin.log("Material " + item.getMaterial() + " is not valid!");
                if (item.isBackground()) {
                    material = Material.STAINED_GLASS_PANE;
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
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
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