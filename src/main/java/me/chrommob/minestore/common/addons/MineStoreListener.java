package me.chrommob.minestore.common.addons;

import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;

public class MineStoreListener {
    public void onPurchase(ParsedResponse event) {}

    public void onClick(CommonItem item, CommonUser commonUser, Component title) {}
}
