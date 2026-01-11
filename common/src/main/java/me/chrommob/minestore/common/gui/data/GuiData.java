package me.chrommob.minestore.common.gui.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.chrommob.minestore.api.event.types.GuiClickEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebContext;
import me.chrommob.minestore.api.web.WebRequest;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.common.gui.BuyGuiItems;
import me.chrommob.minestore.common.gui.GuiOpenener;
import me.chrommob.minestore.common.gui.SortedItem;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedGui;
import me.chrommob.minestore.common.gui.data.parsed.ParsedPackage;
import me.chrommob.minestore.common.gui.data.parsed.ParsedSubCategory;
import me.chrommob.minestore.common.gui.payment.ConfirmationInv;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GuiData {
    private final MineStoreCommon plugin;
    private final GuiOpenener guiOpenener;
    private final BuyGuiItems buyGuiItems;
    private final Gson gson = new Gson();

    private List<?> parsedResponse;
    private ParsedGui parsedGui;
    private final Map<String, CommonInventory> inventoryCache = new HashMap<>();

    public GuiData(MineStoreCommon plugin) {
        this.plugin = plugin;
        this.guiOpenener = new GuiOpenener(this);
        this.buyGuiItems = new BuyGuiItems(this);
    }

    public void load() throws WebContext {
        if (MineStoreCommon.version().requires(3, 0, 0)) {
            TypeToken<List<NewCategory>> listType = new TypeToken<List<NewCategory>>() {};
            WebRequest<List<NewCategory>> request = new WebRequest.Builder<>(listType).path("gui/packages_new").requiresApiKey(true).type(WebRequest.Type.GET).build();
            Result<List<NewCategory>, WebContext> res = plugin.apiHandler().request(request);
            if (res.isError()) {
                throw res.context();
            }
            parsedResponse = res.value();
        } else {
            TypeToken<List<Category>> listType = new TypeToken<List<Category>>() {};
            WebRequest<List<Category>> request = new WebRequest.Builder<>(listType).path("gui/packages_new").requiresApiKey(true).type(WebRequest.Type.GET).build();
            Result<List<Category>, WebContext> res = plugin.apiHandler().request(request);
            if (res.isError()) {
                throw res.context();
            }
            parsedResponse = res.value();
        }
        parsedGui = new ParsedGui(parsedResponse, plugin);
        inventoryCache.clear();
    }

    public CommonInventory getOrCreateRootInventory() {
        String cacheKey = "root";
        if (inventoryCache.containsKey(cacheKey)) {
            return inventoryCache.get(cacheKey);
        }

        CommonInventory inventory = createRootInventory();
        inventoryCache.put(cacheKey, inventory);
        return inventory;
    }

    private CommonInventory createRootInventory() {
        List<SortedItem> sortedItems = new ArrayList<>();
        for (ParsedCategory category : parsedGui.getCategories()) {
            CommonItem itemWithHandler = createItemWithHandler(
                category.getItem(),
                createCategoryClickHandler(category, null)
            );
            SortedItem sortedItem = new SortedItem(itemWithHandler, category.getSorting(), category.isFeatured());
            sortedItems.add(sortedItem);
        }
        SortedItem[] items = sortedItems.toArray(new SortedItem[0]);
        CommonInventory inventory = new CommonInventory(
            plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("buy-gui").getKey("category").getKey("title").getValueAsString()),
            54,
            new CommonItem[0]
        );
        guiOpenener.formatInventory(inventory, items, true);
        return inventory;
    }

    private CommonItem createItemWithHandler(CommonItem baseItem, Consumer<GuiClickEvent> handler) {
        return new CommonItem(
            baseItem.getName(),
            baseItem.getMaterial(),
            baseItem.getLore(),
            baseItem.getEnchantments(),
            baseItem.getAmount(),
            handler
        );
    }

    public CommonInventory getOrCreateCategoryInventory(ParsedCategory category) {
        String cacheKey = "category:" + category.getId();
        if (inventoryCache.containsKey(cacheKey)) {
            return inventoryCache.get(cacheKey);
        }

        CommonInventory inventory = createCategoryInventory(category, null);
        inventoryCache.put(cacheKey, inventory);
        return inventory;
    }

    private CommonInventory createCategoryInventory(ParsedCategory category, CommonInventory parentInventory) {
        List<SortedItem> sortedItems = new ArrayList<>();

        if (category.hasSubcategories()) {
            if (!category.getSubCategories().isEmpty()) {
                for (ParsedSubCategory sub : category.getSubCategories()) {
                    CommonItem itemWithHandler = createItemWithHandler(
                        sub.getItem(),
                        createSubCategoryClickHandler(sub)
                    );
                    sortedItems.add(new SortedItem(itemWithHandler, sub.getSorting(), sub.isFeatured()));
                }
            } else {
                for (ParsedCategory newCat : category.getNewCategories()) {
                    CommonItem itemWithHandler = createItemWithHandler(
                        newCat.getItem(),
                        createCategoryClickHandler(newCat, null)
                    );
                    sortedItems.add(new SortedItem(itemWithHandler, newCat.getSorting(), newCat.isFeatured()));
                }
            }
        } else {
            for (ParsedPackage pkg : category.getPackages()) {
                CommonItem itemWithHandler = createItemWithHandler(
                    pkg.getItem(),
                    createPackageClickHandler(pkg)
                );
                sortedItems.add(new SortedItem(itemWithHandler, pkg.getSorting(), pkg.isFeatured()));
            }
        }

        SortedItem[] items = sortedItems.toArray(new SortedItem[0]);
        CommonInventory inventory = new CommonInventory(category.getDisplayName(), 54, new CommonItem[0]);
        guiOpenener.formatInventory(inventory, items, false);

        return inventory;
    }

    public CommonInventory getOrCreateSubCategoryInventory(ParsedSubCategory sub) {
        String cacheKey = "subcategory:" + sub.getUrl();
        if (inventoryCache.containsKey(cacheKey)) {
            return inventoryCache.get(cacheKey);
        }

        CommonInventory inventory = createSubCategoryInventory(sub);
        inventoryCache.put(cacheKey, inventory);
        return inventory;
    }

    private CommonInventory createSubCategoryInventory(ParsedSubCategory sub) {
        List<SortedItem> sortedItems = new ArrayList<>();
        for (ParsedPackage pkg : sub.getPackages()) {
            CommonItem itemWithHandler = createItemWithHandler(
                pkg.getItem(),
                createPackageClickHandler(pkg)
            );
            sortedItems.add(new SortedItem(itemWithHandler, pkg.getSorting(), pkg.isFeatured()));
        }
        SortedItem[] items = sortedItems.toArray(new SortedItem[0]);
        CommonInventory inventory = new CommonInventory(
            plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("buy-gui").getKey("package").getKey("title").getValueAsString().replace("%subcategory%", sub.getUrl())),
            54,
            new CommonItem[0]
        );
        guiOpenener.formatInventory(inventory, items, false);

        return inventory;
    }

    private Consumer<GuiClickEvent> createCategoryClickHandler(ParsedCategory category, CommonInventory parentInventory) {
        return event -> {
            CommonUser user = event.getUser();
            if (!category.getSubCategories().isEmpty()) {
                openSubcategories(user, category);
            } else if (!category.getNewCategories().isEmpty()) {
                openNewCategory(user, category, event.getInventory());
            } else {
                openPackages(user, category, event.getInventory());
            }
        };
    }

    private void openNewCategory(CommonUser user, ParsedCategory category, CommonInventory parentInventory) {
        CommonInventory inv = getOrCreateCategoryInventory(category);
        if (category.getNewCategories().isEmpty()) {
            buyGuiItems.attachBackHandler(inv, parentInventory);
            user.openInventory(inv);
            return;
        }
        buyGuiItems.attachBackHandler(inv, parentInventory);
        user.openInventory(inv);
    }

    private Consumer<GuiClickEvent> createSubCategoryClickHandler(ParsedSubCategory sub) {
        return event -> {
            CommonUser user = event.getUser();
            ParsedCategory parent = sub.getParent();
            CommonInventory parentInv = getOrCreateCategoryInventory(parent);
            openPackages(user, sub, parentInv);
        };
    }

    private Consumer<GuiClickEvent> createPackageClickHandler(ParsedPackage pkg) {
        return event -> {
            CommonUser user = event.getUser();
            if (pkg.isVirtualCurrency()) {
                openConfirmation(user, pkg);
            } else {
                openStoreUrl(user, pkg);
            }
        };
    }

    private void openSubcategories(CommonUser user, ParsedCategory category) {
        CommonInventory inv = getOrCreateCategoryInventory(category);
        buyGuiItems.attachBackHandler(inv, getOrCreateRootInventory());
        user.openInventory(inv);
    }

    private void openPackages(CommonUser user, ParsedCategory category) {
        openPackages(user, category, getOrCreateRootInventory());
    }

    private void openPackages(CommonUser user, ParsedCategory category, CommonInventory parentInventory) {
        CommonInventory inv = getOrCreateCategoryInventory(category);
        buyGuiItems.attachBackHandler(inv, parentInventory);
        user.openInventory(inv);
    }

    private void openPackages(CommonUser user, ParsedSubCategory sub, CommonInventory parentInventory) {
        CommonInventory inv = getOrCreateSubCategoryInventory(sub);
        buyGuiItems.attachBackHandler(inv, parentInventory);
        user.openInventory(inv);
    }

    private void openConfirmation(CommonUser user, ParsedPackage pkg) {
        ConfirmationInv confirmation = new ConfirmationInv(pkg, plugin);
        CommonInventory inv = confirmation.getInventory();

        Consumer<GuiClickEvent> confirmHandler = event -> {
            user.closeInventory();
            plugin.paymentHandler().createPayment(user.getName(), pkg.getId()).thenAccept(success -> {
                if (success) {
                    user.sendMessage(Component.text("Successfully created payment!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
                } else {
                    user.sendMessage(Component.text("Failed to create payment!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                }
            });
        };

        Consumer<GuiClickEvent> denyHandler = event -> {
            user.closeInventory();
            user.sendMessage(Component.text("Denied!"));
        };

        buyGuiItems.attachConfirmationHandlers(inv, confirmHandler, denyHandler);
        user.openInventory(inv);
    }

    private void openStoreUrl(CommonUser user, ParsedPackage pkg) {
        String config = plugin.pluginConfig().getLang().getKey("buy-gui").getKey("message").getValueAsString();
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
        Component component = plugin.miniMessage().deserialize(config);
        user.closeInventory();
        user.sendMessage(component);
    }

    public final MineStoreScheduledTask mineStoreScheduledTask = new MineStoreScheduledTask("guiData", new Runnable() {
        @Override
        public void run() {
            try {
                load();
                plugin.notError();
            } catch (WebContext e) {
                plugin.debug(this.getClass(), "[GuiData] Error loading data!");
                plugin.handleError(e);
            }
        }
    }, 1000 * 60 * 5);

    public ParsedGui getParsedGui() {
        return parsedGui;
    }

    public GuiOpenener getGuiInfo() {
        return guiOpenener;
    }

    public MineStoreCommon getPlugin() {
        return plugin;
    }

    public CommonItem getConfirmationConfirmItem() {
        return new CommonItem(
            net.kyori.adventure.text.Component.text("Confirm").color(net.kyori.adventure.text.format.NamedTextColor.GREEN),
            "GREEN_WOOL",
            java.util.Collections.emptyList()
        );
    }

    public CommonItem getConfirmationDenyItem() {
        return new CommonItem(
            net.kyori.adventure.text.Component.text("Deny").color(net.kyori.adventure.text.format.NamedTextColor.RED),
            "BARRIER",
            java.util.Collections.emptyList()
        );
    }
}
