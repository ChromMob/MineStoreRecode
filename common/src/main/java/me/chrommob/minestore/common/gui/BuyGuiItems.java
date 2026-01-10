package me.chrommob.minestore.common.gui;

import me.chrommob.minestore.api.event.types.GuiClickEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.common.gui.data.GuiData;
import me.chrommob.minestore.common.gui.data.parsed.ParsedCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedPackage;
import me.chrommob.minestore.common.gui.data.parsed.ParsedSubCategory;
import me.chrommob.minestore.common.gui.payment.ConfirmationInv;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BuyGuiItems {
    private final GuiData guiData;

    public BuyGuiItems(GuiData guiData) {
        this.guiData = guiData;
    }

    public void attachCategoryHandlers(CommonInventory inventory, ParsedCategory category) {
        attachCategoryHandlers(inventory, category, guiData.getParsedGui().getInventory());
    }

    public void attachCategoryHandlers(CommonInventory inventory, ParsedCategory category, CommonInventory parentInventory) {
        for (int i = 0; i < inventory.getItems().size(); i++) {
            CommonItem item = inventory.getItems().get(i);
            CommonItem categoryItem = category.getItem();
            if (categoryItem != null && item.equals(categoryItem)) {
                Consumer<GuiClickEvent> handler = event -> {
                    CommonUser user = event.getUser();
                    if (!category.getSubCategories().isEmpty()) {
                        openSubcategories(user, category);
                    } else if (!category.getNewCategories().isEmpty()) {
                        openNewCategory(user, category);
                    } else {
                        openPackages(user, category, parentInventory);
                    }
                };
                inventory.getItems().set(i, new CommonItem(item.getName(), item.getMaterial(), item.getLore(), item.getAmount(), handler));
            }
        }
    }

    public void attachSubcategoryHandlers(CommonInventory inventory, ParsedSubCategory sub) {
        attachSubcategoryHandlers(inventory, sub, guiData.getParsedGui().getInventory());
    }

    public void attachSubcategoryHandlers(CommonInventory inventory, ParsedSubCategory sub, CommonInventory parentInventory) {
        for (int i = 0; i < inventory.getItems().size(); i++) {
            CommonItem item = inventory.getItems().get(i);
            CommonItem subItem = sub.getItem();
            if (subItem != null && item.equals(subItem)) {
                Consumer<GuiClickEvent> handler = event -> {
                    CommonUser user = event.getUser();
                    openPackages(user, sub, parentInventory);
                };
                inventory.getItems().set(i, new CommonItem(item.getName(), item.getMaterial(), item.getLore(), item.getAmount(), handler));
            }
        }
    }

    public void attachPackageHandlers(CommonInventory inventory, ParsedCategory category, ParsedPackage pkg) {
        for (int i = 0; i < inventory.getItems().size(); i++) {
            CommonItem item = inventory.getItems().get(i);
            CommonItem pkgItem = pkg.getItem();
            if (pkgItem != null && item.equals(pkgItem)) {
                Consumer<GuiClickEvent> handler = event -> {
                    CommonUser user = event.getUser();
                    if (pkg.isVirtualCurrency()) {
                        openConfirmation(user, pkg);
                    } else {
                        openStoreUrl(user, pkg);
                    }
                };
                inventory.getItems().set(i, new CommonItem(item.getName(), item.getMaterial(), item.getLore(), item.getAmount(), handler));
            }
        }
    }

    public void attachPackageHandlers(CommonInventory inventory, ParsedSubCategory sub, ParsedPackage pkg) {
        for (int i = 0; i < inventory.getItems().size(); i++) {
            CommonItem item = inventory.getItems().get(i);
            CommonItem pkgItem = pkg.getItem();
            if (pkgItem != null && item.equals(pkgItem)) {
                Consumer<GuiClickEvent> handler = event -> {
                    CommonUser user = event.getUser();
                    if (pkg.isVirtualCurrency()) {
                        openConfirmation(user, pkg);
                    } else {
                        openStoreUrl(user, pkg);
                    }
                };
                inventory.getItems().set(i, new CommonItem(item.getName(), item.getMaterial(), item.getLore(), item.getAmount(), handler));
            }
        }
    }

    public void attachBackHandler(CommonInventory inventory) {
        CommonItem backItem = guiData.getGuiInfo().getBackItem();
        for (int i = 0; i < inventory.getItems().size(); i++) {
            CommonItem item = inventory.getItems().get(i);
            if (item.equals(backItem)) {
                Consumer<GuiClickEvent> handler = event -> {
                    CommonUser user = event.getUser();
                    user.closeInventory();
                    user.openInventory(guiData.getParsedGui().getInventory());
                };
                inventory.getItems().set(i, new CommonItem(item.getName(), item.getMaterial(), item.getLore(), item.getAmount(), handler));
            }
        }
    }

    private void attachBackHandler(CommonInventory inventory, CommonInventory parentInventory) {
        CommonItem backItem = guiData.getGuiInfo().getBackItem();
        for (int i = 0; i < inventory.getItems().size(); i++) {
            CommonItem item = inventory.getItems().get(i);
            if (item.equals(backItem)) {
                Consumer<GuiClickEvent> handler = event -> {
                    CommonUser user = event.getUser();
                    user.closeInventory();
                    user.openInventory(parentInventory);
                };
                inventory.getItems().set(i, new CommonItem(item.getName(), item.getMaterial(), item.getLore(), item.getAmount(), handler));
            }
        }
    }

    public void attachConfirmationHandlers(CommonInventory inventory, ConfirmationInv confirmation) {
        for (int i = 0; i < inventory.getItems().size(); i++) {
            CommonItem item = inventory.getItems().get(i);
            if (item.equals(confirmation.getConfirmationItem())) {
                Consumer<GuiClickEvent> handler = event -> {
                    CommonUser user = event.getUser();
                    user.closeInventory();
                    guiData.getPlugin().paymentHandler().createPayment(user.getName(), confirmation.getItem().getId()).thenAccept(success -> {
                        if (success) {
                            user.sendMessage(Component.text("Successfully created payment!").color(NamedTextColor.GREEN));
                        } else {
                            user.sendMessage(Component.text("Failed to create payment!").color(NamedTextColor.RED));
                        }
                    });
                };
                inventory.getItems().set(i, new CommonItem(item.getName(), item.getMaterial(), item.getLore(), item.getAmount(), handler));
            } else if (item.equals(confirmation.getDenyItem())) {
                Consumer<GuiClickEvent> handler = event -> {
                    CommonUser user = event.getUser();
                    user.closeInventory();
                    user.sendMessage(Component.text("Denied!"));
                };
                inventory.getItems().set(i, new CommonItem(item.getName(), item.getMaterial(), item.getLore(), item.getAmount(), handler));
            }
        }
    }

    private void openSubcategories(CommonUser user, ParsedCategory category) {
        CommonInventory parentInv = category.getInventory();
        for (ParsedSubCategory sub : category.getSubCategories()) {
            CommonInventory inv = sub.getInventory();
            attachSubcategoryHandlers(inv, sub, parentInv);
            attachBackHandler(inv, parentInv);
            for (ParsedPackage pkg : sub.getPackages()) {
                attachPackageHandlers(inv, sub, pkg);
            }
            user.openInventory(inv);
            break;
        }
    }

    private void openNewCategory(CommonUser user, ParsedCategory parentCategory) {
        if (parentCategory.getNewCategories().isEmpty()) {
            openPackages(user, parentCategory);
            return;
        }
        CommonInventory inv = parentCategory.getInventory();
        CommonInventory parentInv = guiData.getParsedGui().getInventory();
        for (ParsedCategory newCat : parentCategory.getNewCategories()) {
            attachCategoryHandlers(inv, newCat, parentCategory.getInventory());
        }
        attachBackHandler(inv, parentInv);
        user.openInventory(inv);
    }

    private void openPackages(CommonUser user, ParsedCategory category) {
        openPackages(user, category, guiData.getParsedGui().getInventory());
    }

    private void openPackages(CommonUser user, ParsedCategory category, CommonInventory parentInventory) {
        CommonInventory inv = category.getInventory();
        attachBackHandler(inv, parentInventory);
        for (ParsedPackage pkg : category.getPackages()) {
            attachPackageHandlers(inv, category, pkg);
        }
        user.openInventory(inv);
    }

    private void openPackages(CommonUser user, ParsedSubCategory sub) {
        openPackages(user, sub, guiData.getParsedGui().getInventory());
    }

    private void openPackages(CommonUser user, ParsedSubCategory sub, CommonInventory parentInventory) {
        CommonInventory inv = sub.getInventory();
        attachBackHandler(inv, parentInventory);
        for (ParsedPackage pkg : sub.getPackages()) {
            attachPackageHandlers(inv, sub, pkg);
        }
        user.openInventory(inv);
    }

    private void openConfirmation(CommonUser user, ParsedPackage pkg) {
        ConfirmationInv confirmation = new ConfirmationInv(pkg, guiData.getPlugin());
        CommonInventory inv = confirmation.getInventory();
        attachConfirmationHandlers(inv, confirmation);
        user.openInventory(inv);
    }

    private void openStoreUrl(CommonUser user, ParsedPackage pkg) {
        String config = guiData.getPlugin().pluginConfig().getLang().getKey("buy-gui").getKey("message").getValueAsString();
        String storeUrl = ConfigKeys.STORE_URL.getValue();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        String url;
        if (MineStoreCommon.version().requires(3, 0, 0)) {
            url = storeUrl + "/categories/";
        } else {
            url = storeUrl + "/category/";
        }
        if (pkg.getRoot() instanceof ParsedCategory) {
            url += ((ParsedCategory) pkg.getRoot()).getUrl();
        } else if (pkg.getRoot() instanceof ParsedSubCategory) {
            url += ((ParsedSubCategory) pkg.getRoot()).getUrl();
        }
        config = config.replace("%package%", pkg.getName()).replace("%buy_url%", url);
        Component component = guiData.getPlugin().miniMessage().deserialize(config);
        user.closeInventory();
        user.sendMessage(component);
    }

    private void goBack(CommonUser user) {
        user.closeInventory();
        user.openInventory(guiData.getParsedGui().getInventory());
    }
}
