package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@SuppressWarnings("unused")
public class ReloadCommand {
    private final MineStoreCommon plugin;
    public ReloadCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Permission("minestore.reload")
    @Command("minestore|ms reload")
    public void onReload(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        plugin.reload();
        //Send pretty message to user using Component
        user.sendMessage(Component.text("Reloaded MineStore!").color(NamedTextColor.GREEN));
    }
}
