package me.chrommob.minestore.platforms.bukkit.user;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
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

    public MineStoreInventoryHolder(CommonInventory inventory, LegacyComponentSerializer serializer) {
        this.inventory = inventory;
        this.bukkitInventory = constructInventory(inventory, serializer);
    }

    private Inventory constructInventory(CommonInventory common, LegacyComponentSerializer serializer) {
        Inventory inv = Bukkit.createInventory(this, common.getSize(), serializer.serialize(common.getTitle()));

        List<ItemStack> items = new ArrayList<>();
        for (CommonItem item : common.getItems()) {
            items.add(convertToBukkit(item, serializer));
        }
        inv.setContents(items.toArray(new ItemStack[0]));
        return inv;
    }

    private ItemStack convertToBukkit(CommonItem item, LegacyComponentSerializer serializer) {
        Material material = Material.matchMaterial(item.getMaterial());
        if (material == null) {
            material = item.isBackground() ? Material.WHITE_STAINED_GLASS_PANE : Material.CHEST;
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
            if (item.isFeatured()) {
                meta.addEnchant(getDurabilityEnchantment(), 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private Enchantment getDurabilityEnchantment() {
        Enchantment enchantment = Enchantment.getByName("DURABILITY");
        if (enchantment == null) {
            enchantment = Enchantment.getByName("UNBREAKING");
        }
        return enchantment;
    }

    @Override
    public Inventory getInventory() {
        return bukkitInventory;
    }

    public CommonInventory getCommonInventory() {
        return inventory;
    }
}
