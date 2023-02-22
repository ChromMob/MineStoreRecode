package me.chrommob.minestore.common.interfaces.gui;

import net.kyori.adventure.text.Component;

import java.util.List;

public class CommonItem {
    private Component name;
    private List<Component> lore;
    private String material;

    public CommonItem(Component name, String material, List<Component> lore) {
        this.name = name;
        this.material = material;
        this.lore = lore;
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
}
