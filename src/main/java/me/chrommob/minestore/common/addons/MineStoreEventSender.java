package me.chrommob.minestore.common.addons;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;

public class MineStoreEventSender {
    private final MineStoreCommon common;
    public MineStoreEventSender(MineStoreCommon common) {
        this.common = common;
    }
    public void onPurchase(ParsedResponse event) {
        common.getListeners().forEach(listener -> listener.onPurchase(event));
    }
}
