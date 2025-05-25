package me.chrommob.minestore.common.commands;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@SuppressWarnings("unused")
public class BuyCommand {
    private final MineStoreCommon plugin;
    public BuyCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Permission("minestore.buy")
    @Command("buy")
    public void onBuy(AbstractUser user) {
        CommonUser commonUser = user.commonUser();
        if (commonUser instanceof CommonConsoleUser) {
            commonUser.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        plugin.runOnMainThread(() -> plugin.guiData().getGuiInfo().handleInventoryClick(commonUser, null));
    }
}
