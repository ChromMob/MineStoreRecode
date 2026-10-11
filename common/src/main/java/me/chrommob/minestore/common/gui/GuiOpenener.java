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
    private static final int PREVIOUS_PAGE_SLOT = 48;
    private static final int NEXT_PAGE_SLOT = 50;

    public GuiOpenener(GuiData guiData) {
        this.guiData = guiData;
        this.backItem = new CommonItem(
            guiData.getPlugin().miniMessage().deserialize(guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("back").getKey("name").getValueAsString()),
            guiData.getPlugin().pluginConfig().getKey("buy-gui").getKey("back").getKey("item").getValueAsString(),
            Collections.singletonList(guiData.getPlugin().miniMessage().deserialize(guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("back").getKey("description").getValueAsString()))
        );
    }

    /**
     * Spreads the items over as many 54 slot inventories as needed (28 items each), linked with previous/next buttons.
     * The first page is the one to open.
     */
    public List<CommonInventory> createPages(Component title, SortedItem[] sortedItems, boolean isRoot) {
        List<SortedItem> sortedList = new ArrayList<>();
        Collections.addAll(sortedList, sortedItems);
        sortedList.sort(Comparator.comparing(SortedItem::isFeatured).reversed()
            .thenComparingInt(SortedItem::getSorting));

        int pageCount = Math.max(1, (sortedList.size() + AUTO_POSITIONS.length - 1) / AUTO_POSITIONS.length);
        List<CommonInventory> pages = new ArrayList<>();
        for (int page = 0; page < pageCount; page++) {
            List<SortedItem> pageItems = sortedList.subList(page * AUTO_POSITIONS.length, Math.min(sortedList.size(), (page + 1) * AUTO_POSITIONS.length));
            CommonInventory inventory = new CommonInventory(title, 54);
            formatInventory(inventory, pageItems.toArray(new SortedItem[0]), isRoot);
            pages.add(inventory);
        }
        for (int page = 0; page < pageCount; page++) {
            if (page > 0) {
                pages.get(page).setItem(PREVIOUS_PAGE_SLOT, pageButton("Previous page", pages.get(page - 1)));
            }
            if (page < pageCount - 1) {
                pages.get(page).setItem(NEXT_PAGE_SLOT, pageButton("Next page", pages.get(page + 1)));
            }
        }
        return pages;
    }

    private CommonItem pageButton(String name, CommonInventory target) {
        return new CommonItem(Component.text(name), "ARROW", Collections.emptyList(), event -> {
            event.getUser().closeInventory();
            event.getUser().openInventory(target);
        });
    }

    public void formatInventory(CommonInventory inventory, SortedItem[] sortedItems, boolean isRoot) {
        int size = inventory.size();

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

        for (int i = 0; i < newItems.length; i++) {
            inventory.setItem(i, newItems[i]);
        }
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
