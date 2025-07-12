package me.chrommob.minestore.common.commands;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
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
        CommonUser user = abstractUser.commonUser();
        plugin.reload();
        //Send pretty message to user using Component
        if (user instanceof CommonConsoleUser) {
            return;
        }
        user.sendMessage(Component.text("Reloaded MineStore!").color(NamedTextColor.GREEN));
    }
}
