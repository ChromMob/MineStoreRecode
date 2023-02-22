package me.chrommob.minestore.common.gui;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.gui.data.GuiData;
import me.chrommob.minestore.common.gui.data.parsed.ParsedCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedGui;
import me.chrommob.minestore.common.gui.data.parsed.ParsedPackage;
import me.chrommob.minestore.common.gui.data.parsed.ParsedSubCategory;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiInfo {
    private final GuiData guiData;
    public GuiInfo(GuiData guiData) {
        this.guiData = guiData;
    }

    public enum MENU_TYPE {
        CATEGORIES,
        SUBCATEGORIES,
        PACKAGES
    }

    private CommonItem backItem;

    private Map<UUID, MENU_TYPE> menuType = new ConcurrentHashMap<>();
    private Map<UUID, Object> menuPage = new ConcurrentHashMap<>();

    public void handleInventoryClick(MineStoreCommon plugin, CommonUser user, CommonItem item) {
        if (item == null) {
            menuPage.put(user.getUUID(), guiData.getParsedGui());
            menuType.put(user.getUUID(), MENU_TYPE.CATEGORIES);
            openMenu(plugin, user);
            return;
        }
        if (item.equals(backItem)) {
            if (menuType.get(user.getUUID()) == MENU_TYPE.SUBCATEGORIES) {
                menuPage.put(user.getUUID(), guiData.getParsedGui());
                menuType.put(user.getUUID(), MENU_TYPE.CATEGORIES);
            } else if (menuType.get(user.getUUID()) == MENU_TYPE.PACKAGES) {
                ParsedPackage parsedPackage = (ParsedPackage) menuPage.get(user.getUUID());
                Object object = parsedPackage.getRoot();
                if (object instanceof ParsedSubCategory) {
                    menuPage.put(user.getUUID(), ((ParsedSubCategory) object).getRoot());
                    menuType.put(user.getUUID(), MENU_TYPE.SUBCATEGORIES);
                } else {
                    menuPage.put(user.getUUID(), guiData.getParsedGui());
                    menuType.put(user.getUUID(), MENU_TYPE.CATEGORIES);
                }
            }
            openMenu(plugin, user);
            return;
        }
        switch (menuType.get(user.getUUID())) {
            case CATEGORIES:
                if (item.equals(backItem)) {
                    return;
                }
                ParsedGui parsedGui = (ParsedGui) menuPage.get(user.getUUID());
                ParsedCategory parsedCategory = parsedGui.getByItem(item);
                if (parsedCategory.hasSubcategories()) {
                    menuPage.put(user.getUUID(), parsedCategory);
                    menuType.put(user.getUUID(), MENU_TYPE.SUBCATEGORIES);
                } else {
                    menuPage.put(user.getUUID(), parsedCategory);
                    menuType.put(user.getUUID(), MENU_TYPE.PACKAGES);
                }
                openMenu(plugin, user);
                break;
            case SUBCATEGORIES:
                ParsedSubCategory parsedSubCategory = (ParsedSubCategory) ((ParsedCategory) menuPage.get(user.getUUID())).getByItem(item);
                menuPage.put(user.getUUID(), parsedSubCategory);
                menuType.put(user.getUUID(), MENU_TYPE.PACKAGES);
                openMenu(plugin, user);
                break;
            case PACKAGES:
                if (item.equals(backItem)) {
                    return;
                }
                ParsedPackage parsedPackage = (ParsedPackage) ((ParsedSubCategory) menuPage.get(user.getUUID())).getByItem(item);
                String config = (String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_MESSAGE);
                String url = MineStoreCommon.getInstance().configReader().get(ConfigKey.STORE_URL) + "/category/";
                if (parsedPackage.getRoot() instanceof ParsedCategory) {
                    url += ((ParsedCategory) parsedPackage.getRoot()).getUrl();
                } else if (parsedPackage.getRoot() instanceof ParsedSubCategory) {
                    url += ((ParsedSubCategory) parsedPackage.getRoot()).getUrl();
                }
                config = config.replace("%package%", parsedPackage.getName()).replace("%buy_url%", url);
                user.sendMessage(config);
                break;
        }
    }

    private void openMenu(MineStoreCommon plugin, CommonUser user) {
        switch (menuType.get(user.getUUID())) {
            case CATEGORIES:
                ParsedGui parsedGui = (ParsedGui) menuPage.get(user.getUUID());
                user.openInventory(parsedGui.getInventory());
                break;
            case SUBCATEGORIES:
                ParsedCategory parsedCategory = (ParsedCategory) menuPage.get(user.getUUID());
                user.openInventory(parsedCategory.getInventory());
                break;
            case PACKAGES:
                ParsedSubCategory parsedSubCategory = (ParsedSubCategory) menuPage.get(user.getUUID());
                user.openInventory(parsedSubCategory.getInventory());
                break;
        }
    }

    private void formatInventory(CommonInventory inventory) {

    }

    public CommonItem getBackItem() {
        return backItem;
    }
}
