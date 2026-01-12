package me.chrommob.minestore.api.interfaces.gui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CommonInventory {
    private final Component title;
    private final int size;
    private CommonItem[] items;

    public CommonInventory(Component title, int size) {
        this.title = title;
        this.size = size;
        this.items = new CommonItem[size];
    }

    public @NotNull Component getTitle() {
        return title;
    }

    public int size() {
        return size;
    }

    public CommonItem getItem(int slot) {
        if (slot < 0 || slot >= items.length) {
            return null;
        }
        return items[slot];
    }

    public void setItem(int slot, CommonItem item) {
        if (slot >= 0 && slot < items.length) {
            items[slot] = item;
        }
    }

    public CommonItem[] getItems() {
        return items.clone();
    }

    public void forEach(Consumer<CommonItem> action) {
        for (CommonItem item : items) {
            if (item != null) {
                action.accept(item);
            }
        }
    }
}
