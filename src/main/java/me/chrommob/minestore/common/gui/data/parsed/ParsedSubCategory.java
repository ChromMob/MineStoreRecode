package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;

import java.util.List;

public class ParsedSubCategory {
    private String name;
    private String url;
    private String gui_item_id;
    private List<ParsedPackage> packages;

    public ParsedSubCategory(SubCategory subCategory, List<Package> packages) {
        this.name = subCategory.getName();
        this.url = subCategory.getUrl();
        this.gui_item_id = subCategory.getGui_item_id();
        if (packages != null && !packages.isEmpty()) {
            for (Package pack : packages) {
                this.packages.add(new ParsedPackage(pack, this));
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
