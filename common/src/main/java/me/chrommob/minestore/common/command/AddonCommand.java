package me.chrommob.minestore.common.command;

import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@SuppressWarnings("unused")
public class AddonCommand {
    private final MineStoreCommon plugin;
    public AddonCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Command("minestore|ms addons")
    @Permission("minestore.addons")
    public void onAddons(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        user.sendMessage("Loaded addons: " + plugin.getAddons());
    }
}
