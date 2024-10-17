package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;
import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ParsedCategory {
    private final ParsedCategory root;
    private final MineStoreCommon plugin;
    private int id;
    private String name;
    private Component displayName;
    private String url;
    private String material;
    private final List<ParsedSubCategory> subcategories = new ArrayList<>();
    private final List<ParsedCategory> newCategories = new ArrayList<>();
    private final List<ParsedPackage> packages = new ArrayList<>();
    private final CommonItem item;
    private final CommonInventory inventory;

    public ParsedCategory(Category category, MineStoreCommon plugin) {
        this.plugin = plugin;
        this.id = category.getId();
        this.name = category.getName();
        this.url = category.getUrl();
        this.material = category.getGui_item_id();
        this.displayName = plugin.miniMessage().deserialize(((String) plugin.configReader().get(ConfigKey.BUY_GUI_CATEGORY_NAME)).replace("%category%", this.name));
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            for (SubCategory subCategory : category.getSubcategories()) {
                this.subcategories.add(new ParsedSubCategory(subCategory, category.getPackages(), this, plugin));
            }
        } else {
            if (category.getPackages() != null && !category.getPackages().isEmpty()) {
                for (Package pack : category.getPackages()) {
                    if (pack.getActive() == 0)
                        continue;
                    this.packages.add(new ParsedPackage(pack, this, plugin));
                }
            }
        }
        this.item = this.getItem();
        this.inventory = this.getInventory();
        this.root = null;
    }

    public ParsedCategory(NewCategory category, MineStoreCommon plugin) {
        this(null, category, plugin);
    }

    public ParsedCategory(ParsedCategory root, NewCategory category, MineStoreCommon plugin) {
        this.plugin = plugin;
        this.id = category.getId();
        this.name = category.getName();
        this.url = category.getUrl();
        this.material = category.getGui_item_id();
        this.displayName = plugin.miniMessage().deserialize(((String) plugin.configReader().get(ConfigKey.BUY_GUI_CATEGORY_NAME)).replace("%category%", this.name));
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            for (NewCategory subCategory : category.getSubcategories()) {
                this.newCategories.add(new ParsedCategory(this, subCategory, plugin));
            }
        } else {
            if (category.getPackages() != null && !category.getPackages().isEmpty()) {
                for (Package pack : category.getPackages()) {
                    if (pack.getActive() == 0)
                        continue;
                    this.packages.add(new ParsedPackage(pack, this, plugin));
                }
            }
        }
        this.item = this.getItem();
        this.inventory = this.getInventory();
        this.root = root;
    }

    public Object getByItem(CommonItem item) {
        if (!newCategories.isEmpty()) {
            for (ParsedCategory category : this.newCategories) {
                if (category.getItem() != null && category.getItem().equals(item)) {
                    return category;
                }
            }
        }
        if (!subcategories.isEmpty()) {
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
        if (this.item != null) {
            return this.item;
        }
        ConfigReader config = plugin.configReader();
        MiniMessage miniMessage = plugin.miniMessage();
        String configName = (String) config.get(ConfigKey.BUY_GUI_CATEGORY_NAME);
        configName = configName.replace("%category%", this.name);
        Component name = miniMessage.deserialize(configName);
        return new CommonItem(name, material, new ArrayList<>());
    }

    public boolean hasSubcategories() {
        return !subcategories.isEmpty() || !newCategories.isEmpty();
    }

    public CommonInventory getInventory() {
        if (this.inventory != null) {
            return this.inventory;
        }
        if (hasSubcategories()) {
            if (!subcategories.isEmpty()) {
                List<CommonItem> items = new ArrayList<>();
                for (ParsedSubCategory subcategory : subcategories) {
                    items.add(subcategory.getItem());
                }
                CommonInventory inventory = new CommonInventory(displayName, 54, items);
                plugin.guiData().getGuiInfo().formatInventory(inventory, false);
                return inventory;
            } else {
                List<CommonItem> items = new ArrayList<>();
                for (ParsedCategory subcategory : newCategories) {
                    items.add(subcategory.getItem());
                }
                CommonInventory inventory = new CommonInventory(displayName, 54, items);
                plugin.guiData().getGuiInfo().formatInventory(inventory, false);
                return inventory;
            }
        }
        List<CommonItem> items = new ArrayList<>();
        for (ParsedPackage pack : packages) {
            items.add(pack.getItem());
        }
        CommonInventory inventory = new CommonInventory(displayName, 54, items);
        plugin.guiData().getGuiInfo().formatInventory(inventory, false);
        return inventory;
    }

    public List<ParsedSubCategory> getSubCategories() {
        if (hasSubcategories()) {
            return subcategories;
        } else {
            return new ArrayList<>();
        }
    }
    public List<ParsedCategory> getNewCategories() {
        if (hasSubcategories()) {
            return newCategories;
        } else {
            return new ArrayList<>();
        }
    }

    public ParsedCategory getRoot() {
        return root;
    }
}
