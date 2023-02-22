package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ParsedCategory {

    private int id;
    private String name;
    private String url;
    private String material;
    private List<ParsedSubCategory> subcategories;
    private List<ParsedPackage> packages;

    public ParsedCategory(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.url = category.getUrl();
        this.material = category.getGui_item_id();
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            for (SubCategory subCategory : category.getSubcategories()) {
                this.subcategories.add(new ParsedSubCategory(subCategory, category.getPackages(), this));
            }
        } else {
            if (category.getPackages() != null && !category.getPackages().isEmpty()) {
                for (Package pack : category.getPackages()) {
                    this.packages.add(new ParsedPackage(pack, this));
                }
            }
        }
    }

    public Object getByItem(CommonItem item) {
        if (subcategories != null && !subcategories.isEmpty()) {
            for (ParsedSubCategory subCategory : this.subcategories) {
                if (subCategory.getItem() != null && subCategory.getItem().equals(item)) {
                    return subCategory;
                }
            }
        }
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

    public CommonItem getItem() {
        ConfigReader config = MineStoreCommon.getInstance().configReader();
        MiniMessage miniMessage = MineStoreCommon.getInstance().miniMessage();
        String configName = (String) config.get(ConfigKey.BUY_GUI_CATEGORY_NAME);
        configName = configName.replace("%category%", this.name);
        Component name = miniMessage.deserialize(configName);
        return new CommonItem(name, material, new ArrayList<>());
    }

    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    public CommonInventory getInventory() {
        return null;
    }
}
