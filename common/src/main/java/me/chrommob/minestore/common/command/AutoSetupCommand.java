package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

@SuppressWarnings("unused")
@CommandAlias("minestore|ms")
@CommandPermission("minestore.autosetup")
public class AutoSetupCommand extends BaseCommand {

    @Override
    public void help(CommandIssuer issuer, String[] args) {
        CommonUser user = MineStoreCommon.getInstance().userGetter().get(issuer.getUniqueId());
        user.sendMessage("[MineStore] /minestore autosetup <storeUrl> <apiKey> <secretKey>");
    }

    @Subcommand("autosetup")
    public void onAutoSetup(AbstractUser abstractUser, String storeUrl, String apiKey, String secretKey) {
        CommonUser user = abstractUser.user();
        user.sendMessage("Auto setup started!");
        user.sendMessage("Store URL: " + storeUrl);
        MineStoreCommon.getInstance().configReader().set(ConfigKey.STORE_URL, storeUrl);
        MineStoreCommon.getInstance().configReader().set(ConfigKey.API_ENABLED, true);
        MineStoreCommon.getInstance().configReader().set(ConfigKey.API_KEY, apiKey);
        MineStoreCommon.getInstance().configReader().set(ConfigKey.SECRET_ENABLED, true);
        MineStoreCommon.getInstance().configReader().set(ConfigKey.SECRET_KEY, secretKey);
        MineStoreCommon.getInstance().reload();
    }
}
