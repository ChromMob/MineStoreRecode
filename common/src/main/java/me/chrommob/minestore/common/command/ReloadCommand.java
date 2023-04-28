package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@CommandAlias("minestore|ms")
public class ReloadCommand extends BaseCommand {
    @Override
    public void help(CommandIssuer issuer, String[] args) {
        CommonUser user = MineStoreCommon.getInstance().userGetter().get(issuer.getUniqueId());
        user.sendMessage(Component.text("[MineStore] /minestore reload").color(NamedTextColor.RED));
    }
    @CommandPermission("minestore.reload")
    @CommandAlias("reload")
    @SuppressWarnings("unused")
    public void onReload(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        MineStoreCommon.getInstance().reload();
        //Send pretty message to user using Component
        user.sendMessage(Component.text("Reloaded MineStore!").color(NamedTextColor.GREEN));
    }
}
