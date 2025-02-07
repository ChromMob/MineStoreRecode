package me.chrommob.minestore.common.gui;

import me.chrommob.minestore.api.generic.MineStoreVersion;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.payment.ConfirmationInv;
import me.chrommob.minestore.common.gui.data.GuiData;
import me.chrommob.minestore.common.gui.data.parsed.ParsedCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedGui;
import me.chrommob.minestore.common.gui.data.parsed.ParsedPackage;
import me.chrommob.minestore.common.gui.data.parsed.ParsedSubCategory;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class GuiOpenener {
    private final GuiData guiData;
    public GuiOpenener(GuiData guiData) {
        this.guiData = guiData;
        backItem = new CommonItem(guiData.getPlugin().miniMessage().deserialize(guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("back").getKey("name").getAsString()), guiData.getPlugin().pluginConfig().getKey("buy-gui").getKey("back").getKey("item").getAsString(), Collections.singletonList(guiData.getPlugin().miniMessage().deserialize(guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("back").getKey("description").getAsString())));
    }

    public enum MENU_TYPE {
        CATEGORIES,
        SUBCATEGORIES,
        PACKAGES,
        CONFIRMATION
    }

    private final CommonItem backItem;
    private final Map<UUID, MENU_TYPE> menuType = new ConcurrentHashMap<>();
    private final Map<UUID, Object> menuPage = new ConcurrentHashMap<>();

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
                } else if (object instanceof ParsedCategory && ((ParsedCategory) object).getRoot() != null) {
                    menuPage.put(user.getUUID(), ((ParsedCategory) object).getRoot());
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
                Object parsedSubCategory = ((ParsedCategory) menuPage.get(user.getUUID())).getByItem(item);
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
                if (parsedPackage.isVirtualCurrency()) {
                    menuPage.put(user.getUUID(), new ConfirmationInv(parsedPackage, guiData.getPlugin()));
                    menuType.put(user.getUUID(), MENU_TYPE.CONFIRMATION);
                    openMenu(user);
                    return;
                }
                String config = guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("message").getAsString();
                String storeUrl = guiData.getPlugin().pluginConfig().getKey("store-url").getAsString();
                if (storeUrl.endsWith("/")) {
                    storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
                }
                String url;
                if (MineStoreCommon.version().requires(new MineStoreVersion(3, 0, 0))) {
                    url = storeUrl + "/categories/";
                } else {
                    url = storeUrl + "/category/";
                }
                if (parsedPackage.getRoot() instanceof ParsedCategory) {
                    url += ((ParsedCategory) parsedPackage.getRoot()).getUrl();
                } else if (parsedPackage.getRoot() instanceof ParsedSubCategory) {
                    url += ((ParsedSubCategory) parsedPackage.getRoot()).getUrl();
                }
                config = config.replace("%package%", parsedPackage.getName()).replace("%buy_url%", url);
                Component component = guiData.getPlugin().miniMessage().deserialize(config);
                user.closeInventory();
                user.sendMessage(component);
                break;
            case CONFIRMATION:
                ConfirmationInv confirmationInv = (ConfirmationInv) menuPage.get(user.getUUID());
                if (item.equals(confirmationInv.getConfirmationItem())) {
                    user.closeInventory();
                    guiData.getPlugin().paymentHandler().createPayment(user.getName(), confirmationInv.getItem().getId()).thenAccept(success -> {
                        if (success) {
                            user.sendMessage(Component.text("Successfully created payment!").color(NamedTextColor.GREEN));
                        } else {
                            user.sendMessage(Component.text("Failed to create payment!").color(NamedTextColor.RED));
                        }
                    });
                    return;
                }
                if (item.equals(confirmationInv.getDenyItem())) {
                    user.closeInventory();
                    user.sendMessage(Component.text("Denied!"));
                    return;
                }
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
            case CONFIRMATION:
                ConfirmationInv confirmationInv = (ConfirmationInv) menuPage.get(user.getUUID());
                user.openInventory(confirmationInv.getInventory());
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
        addBackground(finalItems, guiData.getPlugin().pluginConfig().getKey("buy-gui").getKey("background").getKey("enabled").getAsBoolean());
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
    private void addBackground(List<CommonItem> finalItems, boolean enabled) {
        CommonItem glassPane = new CommonItem(Component.text(" "), guiData.getPlugin().pluginConfig().getKey("buy-gui").getKey("background").getKey("item").getAsString(), Collections.emptyList(), true);
        CommonItem air = new CommonItem(Component.text(" "), "AIR", Collections.emptyList(), true);
        for (int i = 0; i < 54; i++) {
            if (enabled) {
                finalItems.add(glassPane);
                continue;
            }
            finalItems.add(air);
        }
    }

    public Set<Component> getTitles() {
        Set<Component> titles = new HashSet<>();
        if (guiData == null || guiData.getParsedGui() == null || guiData.getParsedGui().getInventory() == null) {
            return titles;
        } else {
            guiData.getParsedGui().getInventory();
        }
        titles.add(guiData.getParsedGui().getInventory().getTitle());
        for (ParsedCategory parsedCategory : guiData.getParsedGui().getCategories()) {
            titles.add(parsedCategory.getInventory().getTitle());
            for (ParsedSubCategory parsedSubCategory : parsedCategory.getSubCategories()) {
                titles.add(parsedSubCategory.getInventory().getTitle());
            }
            for (ParsedCategory parsedCategory1 : parsedCategory.getNewCategories()) {
                titles.add(parsedCategory1.getInventory().getTitle());
            }
        }
        titles.add(Component.text("Confirmation").color(NamedTextColor.GREEN));
        return titles;
    }

    private Set<Component> customTitles = new CopyOnWriteArraySet<>();
    public void addCustomTitle(Component title) {
        if (customTitles.contains(title)) {
            return;
        }
        customTitles.add(title);
    }

    public Set<Component> getCustomTitles() {
        return customTitles;
    }
}
