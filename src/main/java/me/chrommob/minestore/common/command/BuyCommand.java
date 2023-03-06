package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

@CommandAlias("buy")
public class BuyCommand extends BaseCommand {
    @Default
    @SuppressWarnings("unused")
    public void onBuy(AbstractUser user) {
        CommonUser commonUser = user.user();
        MineStoreCommon.getInstance().guiData().getGuiInfo().handleInventoryClick(commonUser, null);
    }
}
