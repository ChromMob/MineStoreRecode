package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ParsedSubCategory {
    private ParsedCategory root;
    private String name;
    private String url;
    private String material;
    private List<ParsedPackage> packages = new ArrayList<>();
    private final CommonItem item;
    private final CommonInventory inventory;

    public ParsedSubCategory(SubCategory subCategory, List<Package> packages, ParsedCategory root) {
        this.root = root;
        this.name = subCategory.getName();
        this.url = subCategory.getUrl();
        this.material = subCategory.getGui_item_id();
        if (packages != null && !packages.isEmpty()) {
            for (Package pack : packages) {
                this.packages.add(new ParsedPackage(pack, this));
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
        ConfigReader config = MineStoreCommon.getInstance().configReader();
        MiniMessage miniMessage = MineStoreCommon.getInstance().miniMessage();
        String configName = (String) config.get(ConfigKey.BUY_GUI_SUBCATEGORY_NAME);
        configName = configName.replace("%subcategory%", this.name);
        Component name = miniMessage.deserialize(configName);
        return new CommonItem(name, material, new ArrayList<>());
    }

    public CommonInventory getInventory() {
        if (inventory != null) {
            return inventory;
        }
        CommonItem[] items = new CommonItem[packages.size()];
        for (int i = 0; i < packages.size(); i++) {
            items[i] = packages.get(i).getItem();
        }
        CommonInventory inventory = new CommonInventory(MineStoreCommon.getInstance().miniMessage().deserialize(((String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_PACKAGE_TITLE)).replace("%subcategory%", name)), 54, items);
        return inventory;
    }
}
