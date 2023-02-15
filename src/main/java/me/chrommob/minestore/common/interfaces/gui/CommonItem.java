package me.chrommob.minestore.common.interfaces.gui;

import net.kyori.adventure.text.Component;

import java.util.List;

public class CommonItem {
    private Component name;
    private List<Component> lore;

    public CommonItem(Component name, List<Component> lore) {
        this.name = name;
        this.lore = lore;
    }

    public Component getName() {
        return name;
    }
}
