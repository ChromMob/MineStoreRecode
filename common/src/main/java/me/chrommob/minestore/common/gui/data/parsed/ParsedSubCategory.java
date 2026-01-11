package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ParsedSubCategory {
    private final MineStoreCommon plugin;
    private final ParsedCategory root;
    private final String name;
    private final String url;
    private final String material;
    private final List<ParsedPackage> packages = new ArrayList<>();
    private final CommonItem item;

    public ParsedSubCategory(SubCategory subCategory, List<Package> packages, ParsedCategory root, MineStoreCommon plugin) {
        this.plugin = plugin;
        this.root = root;
        this.name = subCategory.getName();
        this.url = subCategory.getUrl();
        this.material = subCategory.getGui_item_id();
        if (packages != null && !packages.isEmpty()) {
            for (Package pack : packages) {
                if (pack.getCategory_url() == null || !pack.getCategory_url().equals(this.url))
                    continue;
                if (pack.getActive() == 0)
                    continue;
                this.packages.add(new ParsedPackage(pack, this, plugin));
            }
        }
        this.item = createItem();
    }

    private CommonItem createItem() {
        MiniMessage miniMessage = plugin.miniMessage();
        String configName = plugin.pluginConfig().getLang().getKey("buy-gui").getKey("subcategory").getKey("name").getValueAsString();
        configName = configName.replace("%subcategory%", this.name);
        Component name = miniMessage.deserialize(configName);
        String itemMaterial = material != null ? material : "CHEST";
        return new CommonItem(name, itemMaterial, new ArrayList<>());
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

    public ParsedCategory getRoot() {
        return root;
    }

    public ParsedCategory getParent() {
        return root;
    }

    public CommonItem getItem() {
        return item;
    }

    public List<ParsedPackage> getPackages() {
        return packages;
    }

    public int getSorting() {
        return 0;
    }

    public boolean isFeatured() {
        return false;
    }
}
