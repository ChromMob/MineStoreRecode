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
    private List<ParsedPackage> packages;

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
        ConfigReader config = MineStoreCommon.getInstance().configReader();
        MiniMessage miniMessage = MineStoreCommon.getInstance().miniMessage();
        String configName = (String) config.get(ConfigKey.BUY_GUI_SUBCATEGORY_NAME);
        configName = configName.replace("%subcategory%", this.name);
        Component name = miniMessage.deserialize(configName);
        return new CommonItem(name, material, new ArrayList<>());
    }

    public CommonInventory getInventory() {
        return null;
    }
}
