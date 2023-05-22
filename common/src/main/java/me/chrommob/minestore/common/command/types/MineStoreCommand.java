package me.chrommob.minestore.common.command.types;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MineStoreCommand extends BaseCommand {
    @Override
    public void help(CommandIssuer issuer, String[] args) {
        AbstractUser abstractUser = new AbstractUser(issuer.getUniqueId());
        CommonUser user = abstractUser.user();
        if (!user.isOnline()) {
            user = new CommonConsoleUser();
        }
        user.sendMessage(Component.text("[MineStore] /minestore help").color(NamedTextColor.RED));
    }
}
