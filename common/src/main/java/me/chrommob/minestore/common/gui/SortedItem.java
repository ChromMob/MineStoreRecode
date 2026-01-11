package me.chrommob.minestore.common.gui;

import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.gui.EnchantmentData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SortedItem {
    private final CommonItem item;
    private final int sorting;
    private final boolean featured;

    public SortedItem(CommonItem item, int sorting, boolean featured) {
        this.item = item;
        this.sorting = sorting;
        this.featured = featured;
    }

    public CommonItem getItem() {
        if (!featured) {
            return item;
        }
        List<EnchantmentData> enchants = item.getEnchantments();
        if (enchants == null) {
            enchants = new ArrayList<>();
        }
        enchants.add(new EnchantmentData("UNBREAKING", 1));
        enchants.add(new EnchantmentData("DURABILITY", 1));
        return new CommonItem(
            item.getName(),
            item.getMaterial(),
            item.getLore(),
            enchants,
            item.getAmount(),
            item.getClickHandler()
        );
    }

    public int getSorting() {
        return sorting;
    }

    public boolean isFeatured() {
        return featured;
    }
}
