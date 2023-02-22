package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.parsed.ParsedCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParsedGui {
    private final List<ParsedCategory> categories = new ArrayList<>();
    public ParsedGui(List<Category> categories) {
        for (Category category : categories) {
            this.categories.add(new ParsedCategory(category));
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

    public CommonInventory getInventory() {

    }
}
