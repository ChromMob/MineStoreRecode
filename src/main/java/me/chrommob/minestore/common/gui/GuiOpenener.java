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
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuiOpenener {
    private final GuiData guiData;
    public GuiOpenener(GuiData guiData) {
        this.guiData = guiData;
    }

    public enum MENU_TYPE {
        CATEGORIES,
        SUBCATEGORIES,
        PACKAGES
    }

    private CommonItem backItem = new CommonItem(MineStoreCommon.getInstance().miniMessage().deserialize((String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_BACK_ITEM_NAME)), (String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_BACK_ITEM), Collections.singletonList(MineStoreCommon.getInstance().miniMessage().deserialize((String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_BACK_ITEM_LORE))));

    private Map<UUID, MENU_TYPE> menuType = new ConcurrentHashMap<>();
    private Map<UUID, Object> menuPage = new ConcurrentHashMap<>();

    public void handleInventoryClick(CommonUser user, CommonItem item) {
        if (item == null) {
            menuPage.put(user.getUUID(), guiData.getParsedGui());
            menuType.put(user.getUUID(), MENU_TYPE.CATEGORIES);
            openMenu(user);
            return;
        }
        if (item.equals(backItem)) {
            if (menuType.get(user.getUUID()) == MENU_TYPE.SUBCATEGORIES) {
                menuPage.put(user.getUUID(), guiData.getParsedGui());
                menuType.put(user.getUUID(), MENU_TYPE.CATEGORIES);
            } else if (menuType.get(user.getUUID()) == MENU_TYPE.PACKAGES) {
                Object object = menuPage.get(user.getUUID());
                if (object instanceof ParsedSubCategory) {
                    menuPage.put(user.getUUID(), ((ParsedSubCategory) object).getRoot());
                    menuType.put(user.getUUID(), MENU_TYPE.SUBCATEGORIES);
                } else {
                    menuPage.put(user.getUUID(), guiData.getParsedGui());
                    menuType.put(user.getUUID(), MENU_TYPE.CATEGORIES);
                }
            }
            openMenu(user);
            return;
        }
        switch (menuType.get(user.getUUID())) {
            case CATEGORIES:
                if (item.equals(backItem)) {
                    return;
                }
                ParsedGui parsedGui = (ParsedGui) menuPage.get(user.getUUID());
                ParsedCategory parsedCategory = parsedGui.getByItem(item);
                if (parsedCategory == null) {
                    return;
                }
                if (parsedCategory.hasSubcategories()) {
                    menuPage.put(user.getUUID(), parsedCategory);
                    menuType.put(user.getUUID(), MENU_TYPE.SUBCATEGORIES);
                } else {
                    menuPage.put(user.getUUID(), parsedCategory);
                    menuType.put(user.getUUID(), MENU_TYPE.PACKAGES);
                }
                openMenu(user);
                break;
            case SUBCATEGORIES:
                ParsedSubCategory parsedSubCategory = (ParsedSubCategory) ((ParsedCategory) menuPage.get(user.getUUID())).getByItem(item);
                if (parsedSubCategory == null) {
                    return;
                }
                menuPage.put(user.getUUID(), parsedSubCategory);
                menuType.put(user.getUUID(), MENU_TYPE.PACKAGES);
                openMenu(user);
                break;
            case PACKAGES:
                if (item.equals(backItem)) {
                    return;
                }
                Class<?> clazz = menuPage.get(user.getUUID()).getClass();
                ParsedPackage parsedPackage = clazz == ParsedCategory.class ? (ParsedPackage) ((ParsedCategory) menuPage.get(user.getUUID())).getByItem(item) : ((ParsedSubCategory) menuPage.get(user.getUUID())).getByItem(item);
                if (parsedPackage == null) {
                    return;
                }
                String config = (String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_MESSAGE);
                String url = MineStoreCommon.getInstance().configReader().get(ConfigKey.STORE_URL) + "/category/";
                if (parsedPackage.getRoot() instanceof ParsedCategory) {
                    url += ((ParsedCategory) parsedPackage.getRoot()).getUrl();
                } else if (parsedPackage.getRoot() instanceof ParsedSubCategory) {
                    url += ((ParsedSubCategory) parsedPackage.getRoot()).getUrl();
                }
                config = config.replace("%package%", parsedPackage.getName()).replace("%buy_url%", url);
                Component component = MineStoreCommon.getInstance().miniMessage().deserialize(config);
                user.closeInventory();
                user.sendMessage(component);
                break;
        }
    }

    private void openMenu(CommonUser user) {
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
                Class<?> clazz = menuPage.get(user.getUUID()).getClass();
                CommonInventory inventory = clazz == ParsedCategory.class ? ((ParsedCategory) menuPage.get(user.getUUID())).getInventory() : ((ParsedSubCategory) menuPage.get(user.getUUID())).getInventory();
                user.openInventory(inventory);
                break;
        }
    }

    public void formatInventory(CommonInventory inventory, boolean isRoot) {
        List<CommonItem> items = inventory.getItems();
        if (inventory.hasSorting()) {
            items.sort(Comparator.comparingInt(CommonItem::getSorting));
        }
        if (inventory.hasFeatured()) {
            items.sort(Comparator.comparing(CommonItem::isFeatured).reversed());
        }
        List<CommonItem> finalItems = new ArrayList<>();
        if ((boolean) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_BACKGROUND_ENABLED)) {
            addBackground(finalItems);
        }
        int index = 0;
        for (int i = 10; i < 17; i++) {
            if (index >= items.size()) {
                break;
            }
            finalItems.set(i, items.get(index));
            index++;
        }
        if (index < items.size()) {
            for (int i = 19; i < 26; i++) {
                if (index >= items.size()) {
                    break;
                }
                finalItems.set(i, items.get(index));
                index++;
            }
        }
        if (index < items.size()) {
            for (int i = 28; i < 35; i++) {
                if (index >= items.size()) {
                    break;
                }
                finalItems.set(i, items.get(index));
                index++;
            }
        }
        if (index < items.size()) {
            for (int i = 37; i < 44; i++) {
                if (index >= items.size()) {
                    break;
                }
                finalItems.set(i, items.get(index));
                index++;
            }
        }
        if (!isRoot) {
            finalItems.set(53, backItem);
        }
        inventory.setItems(finalItems);
    }
    
    private List<CommonItem> addBackground(List<CommonItem> finalItems) {
        CommonItem glassPane = new CommonItem(Component.text(" "), (String) MineStoreCommon.getInstance().configReader().get(ConfigKey.BUY_GUI_BACKGROUND_ITEM), Collections.emptyList(), true);
        for (int i = 0; i < 54; i++) {
            finalItems.add(glassPane);
        }
        return finalItems;
    }

    public Set<Component> getTitles() {
        Set<Component> titles = new HashSet<>();
        titles.add(guiData.getParsedGui().getInventory().getTitle());
        for (ParsedCategory parsedCategory : guiData.getParsedGui().getCategories()) {
            titles.add(parsedCategory.getInventory().getTitle());
            for (ParsedSubCategory parsedSubCategory : parsedCategory.getSubCategories()) {
                titles.add(parsedSubCategory.getInventory().getTitle());
            }
        }
        return titles;
    }
}
