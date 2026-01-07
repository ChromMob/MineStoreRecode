package me.chrommob.minestore.common.gui.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebContext;
import me.chrommob.minestore.api.web.WebRequest;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.GuiOpenener;
import me.chrommob.minestore.common.gui.data.json.old.Category;
import me.chrommob.minestore.common.gui.data.json.old.NewCategory;
import me.chrommob.minestore.common.gui.data.parsed.ParsedGui;

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
