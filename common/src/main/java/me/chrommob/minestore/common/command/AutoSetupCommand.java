package me.chrommob.minestore.common.command;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

@SuppressWarnings("unused")
public class AutoSetupCommand {
    @CommandPermission("minestore.autosetup")
    @CommandMethod("minestore|ms autosetup <storeUrl> <apiKey> <secretKey>")
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
