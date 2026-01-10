package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.api.event.types.GuiClickEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;
import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        this.displayName = plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("buy-gui").getKey("category").getKey("name").getValueAsString().replace("%category%", this.name));
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
        this.displayName = plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("buy-gui").getKey("category").getKey("name").getValueAsString().replace("%category%", this.name));
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
        return getItem(null);
    }

    public CommonItem getItem(Consumer<GuiClickEvent> handler) {
        if (this.item != null && handler == null) {
            return this.item;
        }
        MiniMessage miniMessage = plugin.miniMessage();
        String configName = plugin.pluginConfig().getLang().getKey("buy-gui").getKey("category").getKey("name").getValueAsString();
        configName = configName.replace("%category%", this.name);
        Component name = miniMessage.deserialize(configName);
        CommonItem item = new CommonItem(name, material, new ArrayList<>(), handler);
        if (handler == null) {
            // Only cache if no handler provided
            return item;
        }
        return item;
    }

    public boolean hasSubcategories() {
        return !subcategories.isEmpty() || !newCategories.isEmpty();
    }

    public CommonInventory getInventory() {
        if (this.inventory != null) {
            return this.inventory;
        }
        List<CommonItem> items = new ArrayList<>();
        if (hasSubcategories()) {
            if (!subcategories.isEmpty()) {
                for (ParsedSubCategory subcategory : subcategories) {
                    items.add(subcategory.getItem());
                }
            } else {
                for (ParsedCategory subcategory : newCategories) {
                    items.add(subcategory.getItem());
                }
            }
        } else {
            for (ParsedPackage pack : packages) {
                items.add(pack.getItem());
            }
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

    public List<ParsedPackage> getPackages() {
        return packages;
    }
}
