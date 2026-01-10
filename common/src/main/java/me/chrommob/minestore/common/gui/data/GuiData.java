package me.chrommob.minestore.common.gui.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebContext;
import me.chrommob.minestore.api.web.WebRequest;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.BuyGuiItems;
import me.chrommob.minestore.common.gui.GuiOpenener;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedGui;
import me.chrommob.minestore.common.gui.data.parsed.ParsedPackage;
import me.chrommob.minestore.common.gui.data.parsed.ParsedSubCategory;

import java.util.List;

public class GuiData {
    private final MineStoreCommon plugin;
    public GuiData(MineStoreCommon plugin) {
        this.plugin = plugin;
        guiOpenener = new GuiOpenener(this);
    }
    private List<?> parsedResponse;
    private final Gson gson = new Gson();

    private final GuiOpenener guiOpenener;
    private ParsedGui parsedGui;
    private BuyGuiItems buyGuiItems;

    public void load() throws WebContext {
        if (MineStoreCommon.version().requires(3, 0, 0)) {
            TypeToken<List<NewCategory>> listType = new TypeToken<List<NewCategory>>() {
            };
            WebRequest<List<NewCategory>> request = new WebRequest.Builder<>(listType).path("gui/packages_new").requiresApiKey(true).type(WebRequest.Type.GET).build();
            Result<List<NewCategory>, WebContext> res = plugin.apiHandler().request(request);
            if (res.isError()) {
                throw res.context();
            }
            parsedResponse = res.value();
        } else {
            TypeToken<List<Category>> listType = new TypeToken<List<Category>>() {
            };
            WebRequest<List<Category>> request = new WebRequest.Builder<>(listType).path("gui/packages_new").requiresApiKey(true).type(WebRequest.Type.GET).build();
            Result<List<Category>, WebContext> res = plugin.apiHandler().request(request);
            if (res.isError()) {
                throw res.context();
            }
            parsedResponse = res.value();
        }
        parsedGui = new ParsedGui(parsedResponse, plugin);
        buyGuiItems = new BuyGuiItems(this);
        attachHandlers();
    }

    private void attachHandlers() {
        if (parsedGui == null || parsedGui.getInventory() == null) {
            return;
        }

        buyGuiItems.attachBackHandler(parsedGui.getInventory());

        for (ParsedCategory category : parsedGui.getCategories()) {
            buyGuiItems.attachCategoryHandlers(parsedGui.getInventory(), category);

            if (category.hasSubcategories()) {
                for (ParsedSubCategory sub : category.getSubCategories()) {
                    CommonInventory subInv = sub.getInventory();
                    buyGuiItems.attachSubcategoryHandlers(subInv, sub);
                    buyGuiItems.attachBackHandler(subInv);

                    for (ParsedPackage pkg : sub.getPackages()) {
                        buyGuiItems.attachPackageHandlers(subInv, sub, pkg);
                    }
                }
                for (ParsedCategory newCat : category.getNewCategories()) {
                    CommonInventory newCatInv = newCat.getInventory();
                    buyGuiItems.attachCategoryHandlers(newCatInv, newCat);
                    buyGuiItems.attachBackHandler(newCatInv);

                    for (ParsedPackage pkg : newCat.getPackages()) {
                        buyGuiItems.attachPackageHandlers(newCatInv, newCat, pkg);
                    }
                }
            } else {
                for (ParsedPackage pkg : category.getPackages()) {
                    buyGuiItems.attachPackageHandlers(category.getInventory(), category, pkg);
                }
            }
        }
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
}
