package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.gui.EnchantmentData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MineStoreInventoryHolder implements InventoryHolder {
    private final CommonInventory inventory;
    private final Inventory bukkitInventory;
    private final LegacyComponentSerializer serializer;

    public MineStoreInventoryHolder(CommonInventory inventory, LegacyComponentSerializer serializer) {
        this.inventory = inventory;
        this.serializer = serializer;
        this.bukkitInventory = constructInventory(inventory, serializer);
    }

    private Inventory constructInventory(CommonInventory common, LegacyComponentSerializer serializer) {
        Inventory inv = Bukkit.createInventory(this, common.size(), serializer.serialize(common.getTitle()));

        ItemStack[] items = new ItemStack[common.size()];
        CommonItem[] commonItems = common.getItems();
        for (int i = 0; i < items.length; i++) {
            if (i < commonItems.length && commonItems[i] != null) {
                items[i] = convertToBukkit(commonItems[i], serializer);
            } else {
                items[i] = new ItemStack(Material.AIR);
            }
        }
        inv.setContents(items);
        return inv;
    }

    public void refreshInventory() {
        CommonItem[] commonItems = inventory.getItems();
        for (int i = 0; i < commonItems.length; i++) {
            if (commonItems[i] != null) {
                bukkitInventory.setItem(i, convertToBukkit(commonItems[i], serializer));
            } else {
                bukkitInventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    private ItemStack convertToBukkit(CommonItem item, LegacyComponentSerializer serializer) {
        String itemMaterial = item.getMaterial();
        if (itemMaterial == null || itemMaterial.isEmpty()) {
            itemMaterial = "AIR";
        }
        Material material = Material.matchMaterial(itemMaterial);
        if (material == null) {
            return new ItemStack(Material.AIR, item.getAmount());
        }
        ItemStack stack = new ItemStack(material, item.getAmount());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(serializer.serialize(item.getName()));
            List<String> lore = new ArrayList<>();
            for (Component line : item.getLore()) {
                lore.add(serializer.serialize(line));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            if (item.hasEnchantments()) {
                for (EnchantmentData ench : item.getEnchantments()) {
                    String upperName = ench.name().toUpperCase();
                    Enchantment bukkitEnchant = Enchantment.getByName(upperName);
                    if (bukkitEnchant != null) {
                        meta.addEnchant(bukkitEnchant, ench.level(), true);
                    }
                }
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @Override
    public Inventory getInventory() {
        return bukkitInventory;
    }

    public CommonInventory getCommonInventory() {
        return inventory;
    }
}
