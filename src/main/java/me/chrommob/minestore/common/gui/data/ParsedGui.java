package me.chrommob.minestore.common.gui.data;

import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.parsed.ParsedCategory;

import java.util.List;

public class ParsedGui {
    public ParsedGui(List<Category> categories) {
        for (Category category : categories) {
            this.categories.add(new ParsedCategory(category));
        }
    }
    private List<ParsedCategory> categories;
}
