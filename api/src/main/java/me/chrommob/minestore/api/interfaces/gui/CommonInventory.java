package me.chrommob.minestore.api.interfaces.gui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CommonInventory {
    private final Component title;
    private final int size;
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
        return items;
    }

    public void setItems(CommonItem[] items) {
        this.items = items;
    }

    public int size() {
        return items.length;
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

    public void forEach(Consumer<CommonItem> action) {
        for (CommonItem item : items) {
            if (item != null) {
                action.accept(item);
            }
        }
    }
}
