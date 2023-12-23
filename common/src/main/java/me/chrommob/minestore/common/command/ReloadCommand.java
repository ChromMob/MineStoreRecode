package me.chrommob.minestore.common.command;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@SuppressWarnings("unused")
public class ReloadCommand {
    @CommandPermission("minestore.reload")
    @CommandMethod("minestore|ms reload")
    public void onReload(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        MineStoreCommon.getInstance().reload();
        //Send pretty message to user using Component
        user.sendMessage(Component.text("Reloaded MineStore!").color(NamedTextColor.GREEN));
    }
}
