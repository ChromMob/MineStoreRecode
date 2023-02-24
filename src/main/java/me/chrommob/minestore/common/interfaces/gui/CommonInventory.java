package me.chrommob.minestore.common.interfaces.gui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CommonInventory {
    private Component title;
    private int size;
    private CommonItem[] items;

    public CommonInventory(Component title, int size, CommonItem[] items) {
        this.title = title;
        this.size = size;
        this.items = items;
    }

    public @NotNull Component getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public CommonItem[] getItems() {
        return Arrays.copyOf(items, items.length);
    }

    public CommonItem getItem(int slot) {
        return items[slot];
    }
}
