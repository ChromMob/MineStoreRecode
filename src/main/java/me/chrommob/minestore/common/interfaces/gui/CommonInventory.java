package me.chrommob.minestore.common.interfaces.gui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommonInventory {
    private final Component title;
    private final int size;
    private List<CommonItem> items;

    public CommonInventory(Component title, int size, List<CommonItem> items) {
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

    public List<CommonItem> getItems() {
        return items;
    }

    public void setItems(List<CommonItem> items) {
        this.items = items;
    }

    public CommonInventory clone() {
        return new CommonInventory(title, size, items);
    }

    public boolean hasSorting() {
        if (items.size() == 0) {
            return false;
        }
        int sorting = items.get(0).getSorting();
        for (CommonItem item : items) {
            if (item.getSorting() != sorting) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFeatured() {
        for (CommonItem item : items) {
            if (item.isFeatured()) {
                return true;
            }
        }
        return false;
    }
}
