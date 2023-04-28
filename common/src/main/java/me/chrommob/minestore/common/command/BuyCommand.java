package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

@CommandAlias("buy")
public class BuyCommand extends BaseCommand {

    @Override
    public void help(CommandIssuer issuer, String[] args) {
        CommonUser user = MineStoreCommon.getInstance().userGetter().get(issuer.getUniqueId());
        if (!(user instanceof CommonConsoleUser)) {
            user.sendMessage("[MineStore] /buy");
        } else {
            user.sendMessage("[MineStore] You can't use this command from console!");
        }
    }

    @CommandPermission("minestore.buy")
    @Default
    @SuppressWarnings("unused")
    public void onBuy(AbstractUser user) {
        CommonUser commonUser = user.user();
        if (commonUser instanceof CommonConsoleUser) {
            commonUser.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        MineStoreCommon.getInstance().guiData().getGuiInfo().handleInventoryClick(commonUser, null);
    }
}
