package me.chrommob.minestore.common.gui;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.gui.data.json.old.SubCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedPackage;
import me.chrommob.minestore.common.gui.data.parsed.ParsedSubCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiInfo {
    public enum MENU_TYPE {
        MAIN,
        SUBCATEGORY,
        PACKAGE
    }

    private Map<UUID, MENU_TYPE> menuType = new ConcurrentHashMap<>();
    private Map<UUID, Object> menuPage = new ConcurrentHashMap<>();

    private void openInventory(CommonUser user, CommonItem item, boolean back) {
        if (!menuType.containsKey(user.getUUID()) || item == null) {
            menuType.put(user.getUUID(), MENU_TYPE.MAIN);
        }
        if (back)
            switch (menuType.get(user.getUUID())) {
                case MAIN:
                case SUBCATEGORY:
                    //Shouldn't happen on MAIN
                    menuType.put(user.getUUID(), MENU_TYPE.MAIN);
                    break;
                case PACKAGE:
                    menuType.put(user.getUUID(), MENU_TYPE.SUBCATEGORY);
                    break;
            }
        else {
            switch (menuType.get(user.getUUID())) {
                case MAIN:
                    menuType.put(user.getUUID(), MENU_TYPE.SUBCATEGORY);
                    break;
                case SUBCATEGORY:
                case PACKAGE:
                    //Shouldn't happen on PACKAGE
                    menuType.put(user.getUUID(), MENU_TYPE.PACKAGE);
                    break;
            }
        }
        switch (menuType.get(user.getUUID())) {
            case MAIN:
                openInventory(user, MENU_TYPE.MAIN, back, item);
                break;
            case SUBCATEGORY:
                openInventory(user, MENU_TYPE.SUBCATEGORY, back, item);
                break;
            case PACKAGE:
                openInventory(user, MENU_TYPE.PACKAGE, back, item);
                break;
        }
    }

    public void openInventory(CommonUser user, MENU_TYPE menuType, boolean back, CommonItem item) {
        switch (menuType) {
            case MAIN:
                //TODO
                break;
            case SUBCATEGORY:
                //TODO
                break;
            case PACKAGE:
                ParsedPackage pack = null;
                ParsedSubCategory subCategory = null;
                if (menuPage.get(user.getUUID()) instanceof ParsedSubCategory) {
                    subCategory = (ParsedSubCategory) menuPage.get(user.getUUID());
                    pack = subCategory.getByItem(item);
                }
                ParsedCategory category = null;
                if (menuPage.get(user.getUUID()) instanceof ParsedCategory) {
                    category = (ParsedCategory) menuPage.get(user.getUUID());
                    pack = category.getByItem(item);
                }
                if (pack == null) {
                    MineStoreCommon.getInstance().log("Error while opening package gui for " + user.getName() + "!");
                    return;
                }
                pack = subCategory.getByItem(item);
                String config = (String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_MESSAGE);
                String url = MineStoreCommon.getInstance().configReader().get(ConfigKey.STORE_URL) + "/category/";
                if (pack.getRoot() instanceof ParsedCategory) {
                    url += category.getUrl();
                } else if (pack.getRoot() instanceof ParsedSubCategory) {
                    url += subCategory.getUrl();
                }
                config = config.replace("%package%", pack.getName()).replace("%buy_url%", url);
                user.sendMessage(config);
                break;
        }
    }
}
