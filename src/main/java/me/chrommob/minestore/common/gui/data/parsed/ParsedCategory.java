package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;

import java.util.List;

public class ParsedCategory {

    private int id;
    private String name;
    private String url;
    private String gui_item_id;
    private List<ParsedSubCategory> subcategories;
    private List<ParsedPackage> packages;

    public ParsedCategory(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.url = category.getUrl();
        this.gui_item_id = category.getGui_item_id();
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            for (SubCategory subCategory : category.getSubcategories()) {
                this.subcategories.add(new ParsedSubCategory(subCategory, category.getPackages()));
            }
        } else {
            if (category.getPackages() != null && !category.getPackages().isEmpty()) {
                for (Package pack : category.getPackages()) {
                    this.packages.add(new ParsedPackage(pack, this));
                }
            }
        }
    }

    public ParsedPackage getByItem(CommonItem item) {
        for (ParsedPackage pack : this.packages) {
            if (pack.getItem().equals(item)) {
                return pack;
            }
        }
        return null;
    }

    public String getUrl() {
        return url;
    }
}
