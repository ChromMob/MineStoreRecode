package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;

import java.util.ArrayList;
import java.util.List;

public class ParsedGui {
    private final List<ParsedCategory> categories = new ArrayList<>();
    private final CommonInventory inventory;
    public ParsedGui(List<Category> categories) {
        for (Category category : categories) {
            this.categories.add(new ParsedCategory(category));
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
        CommonItem[] items = new CommonItem[this.categories.size()];
        for (int i = 0; i < this.categories.size(); i++) {
            items[i] = this.categories.get(i).getItem();
        }
        return new CommonInventory(MineStoreCommon.getInstance().miniMessage().deserialize((String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_CATEGORY_TITLE)), 54, items);
    }
}
