package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;

import java.util.ArrayList;
import java.util.List;

public class ParsedGui {
    private final MineStoreCommon plugin;
    private final List<ParsedCategory> categories = new ArrayList<>();
    private final CommonInventory inventory;
    public ParsedGui(List categories, MineStoreCommon plugin) {
        this.plugin = plugin;
        if (categories.isEmpty()) {
            this.inventory = this.getInventory();
            return;
        }
        Object first = categories.get(0);
        if (first instanceof Category) {
            for (Object category : categories) {
                this.categories.add(new ParsedCategory((Category) category, plugin));
            }
        } else {
            for (Object category : categories) {
                this.categories.add(new ParsedCategory((NewCategory) category, plugin));
            }
        }
        this.inventory = this.getInventory();
    }

    public ParsedCategory getByItem(CommonItem item) {
        for (ParsedCategory category : this.categories) {
            if (category.getItem() != null && category.getItem().equals(item)) {
                return category;
            }
        }
        return null;
    }

    public CommonInventory getInventory() {
        if (this.inventory != null) {
            return this.inventory;
        }
        List<CommonItem> items = new ArrayList<>();
        for (ParsedCategory category : this.categories) {
            items.add(category.getItem());
        }
        CommonInventory inventory = new CommonInventory(plugin.miniMessage().deserialize((String) plugin.configReader().get(ConfigKey.BUY_GUI_CATEGORY_TITLE)), 54, items);
        plugin.guiData().getGuiInfo().formatInventory(inventory, true);
        return inventory;
    }

    public List<ParsedCategory> getCategories() {
        return this.categories;
    }
}
