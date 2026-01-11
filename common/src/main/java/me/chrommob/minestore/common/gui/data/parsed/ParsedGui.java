package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;

import java.util.ArrayList;
import java.util.List;

public class ParsedGui {
    private final MineStoreCommon plugin;
    private final List<ParsedCategory> categories = new ArrayList<>();

    public ParsedGui(List<?> categories, MineStoreCommon plugin) {
        this.plugin = plugin;
        if (categories.isEmpty()) {
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
    }

    public ParsedCategory getByItem(CommonItem item) {
        for (ParsedCategory category : this.categories) {
            if (category.getItem() != null && category.getItem().equals(item)) {
                return category;
            }
        }
        return null;
    }

    public List<ParsedCategory> getCategories() {
        return this.categories;
    }
}
