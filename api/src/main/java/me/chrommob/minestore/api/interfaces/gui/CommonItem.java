package me.chrommob.minestore.api.interfaces.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommonItem {
    private int sorting = 0;
    private boolean isFeatured = false;
    private final Component name;
    private final List<Component> lore;
    private String material;
    private boolean isBackground = false;

    private int amount = 1;

    public CommonItem(Component name, String material, List<Component> lore, int amount) {
        this.name = name;
        this.material = material;
        this.lore = lore;
        this.amount = amount;
    }

    public CommonItem(Component name, String material, List<Component> lore) {
        this.name = name;
        this.material = material;
        this.lore = lore;
    }

    public CommonItem(Component name, String material, List<Component> lore, boolean isBackground) {
        this.name = name;
        this.material = material;
        this.lore = lore;
        this.isBackground = isBackground;
    }

    public CommonItem(Component name, String material, List<Component> lore, boolean isFeatured, int sorting) {
        this.name = name;
        this.material = material;
        this.lore = lore;
        this.isFeatured = isFeatured;
        this.sorting = sorting;
    }

    public Component getName() {
        return name;
    }

    public List<Component> getLore() {
        return lore;
    }

    public String getMaterial() {
        return material;
    }

    public boolean equals(CommonItem item) {
        @NotNull LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        boolean name = serializer.serialize(this.name).equalsIgnoreCase(serializer.serialize(item.name));
        StringBuilder lore = new StringBuilder();
        StringBuilder lore2 = new StringBuilder();
        for (Component line : this.lore) {
            lore.append(serializer.serialize(line));
        }
        for (Component line : item.lore) {
            lore2.append(serializer.serialize(line));
        }
        boolean lore3 = lore.toString().equalsIgnoreCase(lore2.toString());
        if (this.material == null) {
            material = "CHEST";
        }
        this.material = this.material.replace("minecraft:", "");
        boolean material = this.material.equalsIgnoreCase(item.material);
        return name && lore3 && material;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public int getSorting() {
        return sorting;
    }

    public void setMaterial(String chest) {
        this.material = chest;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public int getAmount() {
        return amount;
    }
}

