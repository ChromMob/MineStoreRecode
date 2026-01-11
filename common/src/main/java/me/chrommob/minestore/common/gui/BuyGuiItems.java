package me.chrommob.minestore.common.gui;

import me.chrommob.minestore.api.event.types.GuiClickEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.gui.data.GuiData;

import java.util.function.Consumer;

public class BuyGuiItems {
    private final GuiData guiData;

    public BuyGuiItems(GuiData guiData) {
        this.guiData = guiData;
    }

    public void attachHandlers(CommonInventory inventory, HandlerFactory factory) {
        for (int i = 0; i < inventory.size(); i++) {
            CommonItem item = inventory.getItem(i);
            if (item == null) continue;
            Consumer<GuiClickEvent> handler = factory.create(i, item);
            if (handler != null) {
                inventory.setItem(i, new CommonItem(item, handler));
            }
        }
    }

    public void attachBackHandler(CommonInventory inventory, CommonInventory parentInventory) {
        CommonItem backItem = guiData.getGuiInfo().getBackItem();
        attachHandlers(inventory, (slot, item) -> {
            if (item.equals(backItem)) {
                return event -> {
                    event.getUser().closeInventory();
                    event.getUser().openInventory(parentInventory);
                };
            }
            return null;
        });
    }

    public void attachConfirmationHandlers(CommonInventory inventory, Consumer<GuiClickEvent> confirmHandler, Consumer<GuiClickEvent> denyHandler) {
        attachHandlers(inventory, (slot, item) -> {
            CommonItem confirmItem = guiData.getConfirmationConfirmItem();
            CommonItem denyItem = guiData.getConfirmationDenyItem();
            if (item.equals(confirmItem)) {
                return confirmHandler;
            } else if (item.equals(denyItem)) {
                return denyHandler;
            }
            return null;
        });
    }

    @FunctionalInterface
    public interface HandlerFactory {
        Consumer<GuiClickEvent> create(int slot, CommonItem item);
    }
}
