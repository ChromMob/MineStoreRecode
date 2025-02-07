package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ParsedSubCategory {
    private final MineStoreCommon plugin;
    private ParsedCategory root;
    private String name;
    private String url;
    private String material;
    private List<ParsedPackage> packages = new ArrayList<>();
    private final CommonItem item;
    private final CommonInventory inventory;

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
        this.item = this.getItem();
        this.inventory = this.getInventory();
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

    public CommonItem getItem() {
        if (item != null) {
            return item;
        }
        MiniMessage miniMessage = plugin.miniMessage();
        String configName = plugin.pluginConfig().getLang().getKey("buy-gui").getKey("subcategory").getKey("name").getAsString();
        configName = configName.replace("%subcategory%", this.name);
        Component name = miniMessage.deserialize(configName);
        return new CommonItem(name, material, new ArrayList<>());
    }

    public CommonInventory getInventory() {
        if (inventory != null) {
            return inventory;
        }
        List<CommonItem> items = new ArrayList<>();
        for (ParsedPackage pack : this.packages) {
            items.add(pack.getItem());
        }
        CommonInventory inventory = new CommonInventory(plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("buy-gui").getKey("package").getKey("title").getAsString().replace("%subcategory%", name)), 54, items);
        plugin.guiData().getGuiInfo().formatInventory(inventory, false);
        return inventory;
    }
}
