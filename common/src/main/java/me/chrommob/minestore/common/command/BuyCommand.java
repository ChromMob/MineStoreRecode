package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
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
        CommonUser commonUser = user.user();
        if (commonUser instanceof CommonConsoleUser) {
            commonUser.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        plugin.runOnMainThread(() -> plugin.guiData().getGuiInfo().handleInventoryClick(commonUser, null));
    }
}
