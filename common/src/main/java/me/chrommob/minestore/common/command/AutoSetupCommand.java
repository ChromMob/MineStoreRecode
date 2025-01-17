package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import org.incendo.cloud.annotation.specifier.Quoted;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@SuppressWarnings("unused")
public class AutoSetupCommand {
    private final MineStoreCommon plugin;
    public AutoSetupCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }

    @Permission("minestore.autosetup")
    @Command("minestore|ms autosetup <storeUrl> <apiKey> <secretKey>")
    public void onAutoSetup(AbstractUser abstractUser, @Argument("storeUrl") @Quoted String storeUrl, @Argument("apiKey") String apiKey, @Argument("secretKey") String secretKey) {
        CommonUser user = abstractUser.user();
        user.sendMessage("Auto setup started!");
        user.sendMessage("Store URL: " + storeUrl);
        if (!storeUrl.endsWith("/")) {
            storeUrl += "/";
        }
        plugin.configReader().set(ConfigKey.STORE_URL, storeUrl);
        plugin.configReader().set(ConfigKey.API_ENABLED, true);
        plugin.configReader().set(ConfigKey.API_KEY, apiKey);
        plugin.configReader().set(ConfigKey.SECRET_ENABLED, true);
        plugin.configReader().set(ConfigKey.SECRET_KEY, secretKey);
        plugin.reload();
    }
}
