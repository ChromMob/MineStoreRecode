package me.chrommob.minestore.common.addons;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;

public class MineStoreEventSender {
    private final MineStoreCommon common;
    public MineStoreEventSender(MineStoreCommon common) {
        this.common = common;
    }
    public void onPurchase(ParsedResponse event) {
        common.getListeners().forEach(listener -> listener.onPurchase(event));
    }

    public void onClick(CommonItem item, CommonUser commonUser, Component title) {
        common.getListeners().forEach(listener -> listener.onClick(item, commonUser, title));
    }
}
