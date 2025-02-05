package me.chrommob.minestore.common.gui.payment;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.gui.data.parsed.ParsedPackage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfirmationInv {
    private final CommonItem background;
    private final CommonItem confirmationItem = new CommonItem(Component.text("Confirm").color(NamedTextColor.GREEN), "GREEN_WOOL", Collections.emptyList(), false);
    private final CommonItem denyItem = new CommonItem(Component.text("Deny").color(NamedTextColor.RED), "BARRIER", Collections.emptyList(), false);

    private final ParsedPackage parsedPackage;

    public ConfirmationInv(ParsedPackage parsedPackage, MineStoreCommon plugin) {
        this.parsedPackage = parsedPackage;
        background = new CommonItem(Component.text(" "), (String) plugin.configReader().get(ConfigKey.BUY_GUI_BACKGROUND_ITEM), Collections.emptyList(), true);
    }

    public CommonInventory getInventory() {
        List<CommonItem> items = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            items.add(background);
        }
        items.set(4, parsedPackage.getItem());
        items.set(20, confirmationItem);
        items.set(24, denyItem);

        return new CommonInventory(Component.text("Confirmation").color(NamedTextColor.GREEN), 27, items);
    }

    public ParsedPackage getItem() {
        return parsedPackage;
    }

    public CommonItem getConfirmationItem() {
        return confirmationItem;
    }

    public CommonItem getDenyItem() {
        return denyItem;
    }
}
