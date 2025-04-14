package me.chrommob.minestore.common.command;

import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.MineStoreCommon;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class StoreCommand  {
    private final MineStoreCommon plugin;
    public StoreCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Command("store")
    @Permission("minestore.store")
    @SuppressWarnings("unused")
    public void onStore(final AbstractUser abstractUser) {
        String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        final Component message = plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("store-command").getKey("message").getAsString().replaceAll("%store_url%", storeUrl));
        abstractUser.user().sendMessage(message);
    }
}
