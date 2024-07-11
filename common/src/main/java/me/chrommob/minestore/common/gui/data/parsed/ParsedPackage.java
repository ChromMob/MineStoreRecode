package me.chrommob.minestore.common.gui.data.parsed;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.config.ConfigReader;
import me.chrommob.minestore.common.gui.data.json.old.Package;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ParsedPackage {
    private final MineStoreCommon plugin;
    private Object root;
    private String name;
    private double price;
    private double discount;
    private int sorting;
    private String category_url;
    private int featured;
    private int active;
    private String material;
    private String item_lore;
    private final CommonItem item;

    public ParsedPackage(Package pack, Object root, MineStoreCommon plugin) {
        this.plugin = plugin;
        this.root = root;
        this.name = pack.getName();
        this.price = pack.getPrice();
        this.discount = pack.getDiscount();
        this.sorting = pack.getSorting();
        this.category_url = pack.getCategory_url();
        this.featured = pack.getFeatured();
        this.active = pack.getActive();
        this.material = pack.getItem_id();
        this.item_lore = pack.getItem_lore();
        this.item = this.getItem();
    }

    public CommonItem getItem() {
        if (this.item != null) {
            return this.item;
        }
        ConfigReader config = plugin.configReader();
        MiniMessage miniMessage = plugin.miniMessage();
        List<Component> lore = new ArrayList<>();
        if (this.item_lore != null && !this.item_lore.isEmpty()) {
            String item_lore = this.item_lore;
            String configLore = (String) config.get(ConfigKey.BUY_GUI_PACKAGE_LORE);
            configLore = configLore.replace("%description%", item_lore);
            lore.add(miniMessage.deserialize(configLore));
        }
        String configPrice = (String) config.get(ConfigKey.BUY_GUI_PACKAGE_PRICE);
        double price = (this.price - (this.price * (this.discount / 100)));
        price = Math.round(price * 100.0) / 100.0;
        configPrice = configPrice.replace("%price%", String.valueOf(price));
        lore.add(miniMessage.deserialize(configPrice));
        String configName = (String) config.get(ConfigKey.BUY_GUI_PACKAGE_NAME);
        configName = configName.replace("%package%", this.name);
        Component name = miniMessage.deserialize(configName);
        return new CommonItem(name, material, lore, featured == 1, sorting);
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return category_url;
    }

    public Object getRoot() {
        return root;
    }
}
