package me.chrommob.minestore.common.command;

import cloud.commandframework.annotations.Command;
import cloud.commandframework.annotations.Permission;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

@SuppressWarnings("unused")
public class BuyCommand {

    @Permission("minestore.buy")
    @Command("buy")
    public void onBuy(AbstractUser user) {
        CommonUser commonUser = user.user();
        if (commonUser instanceof CommonConsoleUser) {
            commonUser.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        MineStoreCommon.getInstance().guiData().getGuiInfo().handleInventoryClick(commonUser, null);
    }
}
