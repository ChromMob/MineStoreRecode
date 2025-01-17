package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
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
        final Component message = (plugin.miniMessage()).deserialize(((String)plugin.configReader().get(ConfigKey.STORE_COMMAND_MESSAGE)).replaceAll("%store_url%", (String)plugin.configReader().get(ConfigKey.STORE_URL)));
        abstractUser.user().sendMessage(message);
    }
}
