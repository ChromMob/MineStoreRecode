package me.chrommob.minestore.common.gui.data.json.old;

import java.util.List;

public class Category {
    private int id;
    private String name;
    private String url;
    private String gui_item_id;
    private List<SubCategory> subcategories;
    private List<Package> packages;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getGui_item_id() {
        return gui_item_id;
    }

    public List<SubCategory> getSubcategories() {
        return subcategories;
    }

    public List<Package> getPackages() {
        return packages;
    }
}
