package me.chrommob.minestore.api.event.types;

import me.chrommob.minestore.api.event.MineStoreEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.user.CommonUser;

public class GuiCloseEvent extends MineStoreEvent {
    private final CommonUser user;
    private final CommonInventory inventory;

    public GuiCloseEvent(CommonUser user, CommonInventory inventory) {
        this.user = user;
        this.inventory = inventory;
    }

    public CommonUser getUser() {
        return user;
    }

    public CommonInventory getInventory() {
        return inventory;
    }
}
