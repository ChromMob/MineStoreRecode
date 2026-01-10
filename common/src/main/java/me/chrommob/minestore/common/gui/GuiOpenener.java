package me.chrommob.minestore.common.gui;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.common.gui.data.GuiData;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GuiOpenener {
    private final GuiData guiData;
    private final CommonItem backItem;

    public GuiOpenener(GuiData guiData) {
        this.guiData = guiData;
        this.backItem = new CommonItem(
            guiData.getPlugin().miniMessage().deserialize(guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("back").getKey("name").getValueAsString()),
            guiData.getPlugin().pluginConfig().getKey("buy-gui").getKey("back").getKey("item").getValueAsString(),
            Collections.singletonList(guiData.getPlugin().miniMessage().deserialize(guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("back").getKey("description").getValueAsString()))
        );
    }

    public void formatInventory(CommonInventory inventory, boolean isRoot) {
        List<CommonItem> items = inventory.getItems();
        if (inventory.hasSorting()) {
            items.sort(Comparator.comparingInt(CommonItem::getSorting));
        }
        if (inventory.hasFeatured()) {
            items.sort(Comparator.comparing(CommonItem::isFeatured).reversed());
        }

        CommonItem glassPane = new CommonItem(Component.text(" "), ConfigKeys.BUY_GUI_KEYS.CATEGORY_KEYS.ITEM.getValue(), Collections.emptyList(), true);
        CommonItem air = new CommonItem(Component.text(" "), "AIR", Collections.emptyList(), true);
        boolean enabled = ConfigKeys.BUY_GUI_KEYS.CATEGORY_KEYS.ENABLED.getValue();

        List<CommonItem> newItems = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            if (enabled) {
                newItems.add(glassPane);
            } else {
                newItems.add(air);
            }
        }

        int index = 0;
        for (int i = 10; i < 17; i++) {
            if (index >= items.size()) {
                break;
            }
            newItems.set(i, items.get(index));
            index++;
        }
        if (index < items.size()) {
            for (int i = 19; i < 26; i++) {
                if (index >= items.size()) {
                    break;
                }
                newItems.set(i, items.get(index));
                index++;
            }
        }
        if (index < items.size()) {
            for (int i = 28; i < 35; i++) {
                if (index >= items.size()) {
                    break;
                }
                newItems.set(i, items.get(index));
                index++;
            }
        }
        if (index < items.size()) {
            for (int i = 37; i < 44; i++) {
                if (index >= items.size()) {
                    break;
                }
                newItems.set(i, items.get(index));
                index++;
            }
        }
        if (!isRoot) {
            newItems.set(53, backItem);
        }

        items.clear();
        items.addAll(newItems);
    }

    public CommonItem getBackItem() {
        return backItem;
    }
}
