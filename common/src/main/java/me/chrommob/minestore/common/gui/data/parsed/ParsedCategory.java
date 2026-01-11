package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.api.event.types.GuiClickEvent;
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
    private final int id;
    private final String name;
    private final Component displayName;
    private final String url;
    private final String material;
    private final List<ParsedSubCategory> subcategories = new ArrayList<>();
    private final List<ParsedCategory> newCategories = new ArrayList<>();
    private final List<ParsedPackage> packages = new ArrayList<>();
    private final CommonItem item;

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
        this.item = createItem();
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
        this.item = createItem();
        this.root = root;
    }

    private CommonItem createItem() {
        MiniMessage miniMessage = plugin.miniMessage();
        String configName = plugin.pluginConfig().getLang().getKey("buy-gui").getKey("category").getKey("name").getValueAsString();
        configName = configName.replace("%category%", this.name);
        Component name = miniMessage.deserialize(configName);
        String itemMaterial = material != null ? material : "CHEST";
        return new CommonItem(name, itemMaterial, new ArrayList<>());
    }

    public String getUrl() {
        return url;
    }

    public CommonItem getItem() {
        return item;
    }

    public CommonItem getItem(Consumer<GuiClickEvent> handler) {
        return new CommonItem(item, handler);
    }

    public List<ParsedSubCategory> getSubCategories() {
        return subcategories;
    }

    public List<ParsedCategory> getNewCategories() {
        return newCategories;
    }

    public boolean hasSubcategories() {
        return !subcategories.isEmpty() || !newCategories.isEmpty();
    }

    public ParsedCategory getRoot() {
        return root;
    }

    public List<ParsedPackage> getPackages() {
        return packages;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public int getSorting() {
        return 0;
    }

    public boolean isFeatured() {
        return false;
    }
}
