package me.chrommob.minestore.common.commands;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        CommonUser user = abstractUser.commonUser();
        user.sendMessage("Auto setup started!");
        user.sendMessage("Store URL: " + storeUrl);
        if (!storeUrl.endsWith("/")) {
            storeUrl += "/";
        }
        plugin.pluginConfig().getKey("store-url").setValue(storeUrl);
        plugin.pluginConfig().getKey("api").getKey("key").setValue(apiKey);
        plugin.pluginConfig().getKey("api").getKey("key-enabled").setValue(true);
        plugin.pluginConfig().getKey("weblistener").getKey("secret-key").setValue(secretKey);
        plugin.pluginConfig().getKey("weblistener").getKey("secret-enabled").setValue(true);
        plugin.pluginConfig().saveConfig();
        plugin.reload();
        if (user instanceof CommonConsoleUser) {
            return;
        }
        user.sendMessage(Component.text("Auto setup finished!").color(NamedTextColor.GREEN));
    }
}
