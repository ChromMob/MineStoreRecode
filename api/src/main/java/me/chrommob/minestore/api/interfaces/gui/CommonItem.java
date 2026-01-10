package me.chrommob.minestore.api.interfaces.gui;

import me.chrommob.minestore.api.event.types.GuiClickEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class CommonItem {
    private int sorting = 0;
    private boolean isFeatured = false;
    private final Component name;
    private final List<Component> lore;
    private String material;
    private boolean isBackground = false;
    private int amount = 1;
    private final Consumer<GuiClickEvent> clickHandler;

    public CommonItem(Component name, String material, List<Component> lore, int amount) {
        this(name, material, lore, amount, null);
    }

    public CommonItem(Component name, String material, List<Component> lore) {
        this(name, material, lore, 1, null);
    }

    public CommonItem(Component name, String material, List<Component> lore, boolean isBackground) {
        this(name, material, lore, 1, null);
        this.isBackground = isBackground;
    }

    public CommonItem(Component name, String material, List<Component> lore, boolean isFeatured, int sorting) {
        this(name, material, lore, 1, null);
        this.isFeatured = isFeatured;
        this.sorting = sorting;
    }

    public CommonItem(Component name, String material, List<Component> lore, int amount, Consumer<GuiClickEvent> clickHandler) {
        this.name = name;
        this.material = material;
        this.lore = lore;
        this.amount = amount;
        this.clickHandler = clickHandler;
    }

    public CommonItem(Component name, String material, List<Component> lore, Consumer<GuiClickEvent> clickHandler) {
        this(name, material, lore, 1, clickHandler);
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

    public boolean hasClickHandler() {
        return clickHandler != null;
    }

    public Consumer<GuiClickEvent> getClickHandler() {
        return clickHandler;
    }

    public void invokeClickHandler(GuiClickEvent event) {
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }
}
