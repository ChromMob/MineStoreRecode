package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class StoreCommand  {
    @Command("store")
    @Permission("minestore.store")
    @SuppressWarnings("unused")
    public void onStore(final AbstractUser abstractUser) {
        String storeUrl = (String) MineStoreCommon.getInstance().configReader().get(ConfigKey.STORE_URL);
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        final Component message = (MineStoreCommon.getInstance().miniMessage()).deserialize(((String)MineStoreCommon.getInstance().configReader().get(ConfigKey.STORE_COMMAND_MESSAGE)).replaceAll("%store_url%", storeUrl));
        abstractUser.user().sendMessage(message);
    }
}
