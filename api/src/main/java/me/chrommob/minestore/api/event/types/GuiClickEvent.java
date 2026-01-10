package me.chrommob.minestore.api.event.types;

import me.chrommob.minestore.api.event.MineStoreEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.user.CommonUser;

public class GuiClickEvent extends MineStoreEvent {
    private final CommonUser user;
    private final CommonItem item;
    private final CommonInventory inventory;
    private boolean cancelled = false;

    public GuiClickEvent(CommonUser user, CommonItem item, CommonInventory inventory) {
        this.user = user;
        this.item = item;
        this.inventory = inventory;
    }

    public CommonUser getUser() {
        return user;
    }

    public CommonItem getItem() {
        return item;
    }

    public CommonInventory getInventory() {
        return inventory;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
