package me.chrommob.minestore.common.interfaces.gui;

public class CommonInventory {
    private String title;
    private int size;
    private CommonItem[] items;

    public CommonInventory(String title, int size, CommonItem[] items) {
        this.title = title;
        this.size = size;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public CommonItem[] getItems() {
        return items;
    }

    public CommonItem getItem(int slot) {
        return items[slot];
    }
}
