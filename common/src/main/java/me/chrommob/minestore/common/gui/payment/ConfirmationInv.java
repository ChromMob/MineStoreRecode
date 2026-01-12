package me.chrommob.minestore.common.gui.payment;

import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.gui.data.parsed.ParsedPackage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;

public class ConfirmationInv {
    private final CommonItem background;
    private final CommonItem confirmationItem = new CommonItem(Component.text("Confirm").color(NamedTextColor.GREEN), "GREEN_WOOL", Collections.emptyList());
    private final CommonItem denyItem = new CommonItem(Component.text("Deny").color(NamedTextColor.RED), "BARRIER", Collections.emptyList());

    private final ParsedPackage parsedPackage;

    public ConfirmationInv(ParsedPackage parsedPackage, MineStoreCommon plugin) {
        this.parsedPackage = parsedPackage;
        background = new CommonItem(Component.text(" "), "AIR", Collections.emptyList());
    }

    public CommonInventory getInventory() {
        CommonInventory inventory = new CommonInventory(Component.text("Confirmation").color(NamedTextColor.GREEN), 27);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, background);
        }
        inventory.setItem(4, parsedPackage.getItem());
        inventory.setItem(20, confirmationItem);
        inventory.setItem(24, denyItem);

        return inventory;
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
