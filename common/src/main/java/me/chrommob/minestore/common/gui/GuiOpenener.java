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

    private static final int[] AUTO_POSITIONS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    public GuiOpenener(GuiData guiData) {
        this.guiData = guiData;
        this.backItem = new CommonItem(
            guiData.getPlugin().miniMessage().deserialize(guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("back").getKey("name").getValueAsString()),
            guiData.getPlugin().pluginConfig().getKey("buy-gui").getKey("back").getKey("item").getValueAsString(),
            Collections.singletonList(guiData.getPlugin().miniMessage().deserialize(guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("back").getKey("description").getValueAsString()))
        );
    }

    public void formatInventory(CommonInventory inventory, SortedItem[] sortedItems, boolean isRoot) {
        int size = inventory.getSize();

        List<SortedItem> sortedList = new ArrayList<>();
        for (SortedItem item : sortedItems) {
            sortedList.add(item);
        }

        sortedList.sort(Comparator.comparing(SortedItem::isFeatured).reversed()
            .thenComparingInt(SortedItem::getSorting));

        CommonItem[] newItems = new CommonItem[size];

        for (SortedItem sorted : sortedList) {
            int slot = findNextAvailableSlot(newItems, size);
            if (slot != -1) {
                newItems[slot] = sorted.getItem();
            }
        }

        CommonItem glassPane = new CommonItem(Component.text(" "), ConfigKeys.BUY_GUI_KEYS.CATEGORY_KEYS.ITEM.getValue(), Collections.emptyList());
        CommonItem air = new CommonItem(Component.text(" "), "AIR", Collections.emptyList());
        boolean backgroundEnabled = ConfigKeys.BUY_GUI_KEYS.CATEGORY_KEYS.ENABLED.getValue();
        CommonItem background = backgroundEnabled ? glassPane : air;

        for (int i = 0; i < size; i++) {
            if (newItems[i] == null) {
                newItems[i] = background;
            }
        }

        if (!isRoot && ConfigKeys.BUY_GUI_KEYS.BACK_KEYS.ENABLED.getValue()) {
            newItems[53] = backItem;
        }

        inventory.setItems(newItems);
    }

    private int findNextAvailableSlot(CommonItem[] items, int size) {
        for (int slot : AUTO_POSITIONS) {
            if (slot < size && items[slot] == null) {
                return slot;
            }
        }
        return -1;
    }

    public CommonItem getBackItem() {
        return backItem;
    }
}
