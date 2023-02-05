package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.authHolder.AuthUser;
import me.chrommob.minestore.common.command.types.AbstractUser;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.interfaces.CommonUser;

@CommandAlias("minestore|ms")
public class AuthCommand extends BaseCommand {
    @CommandPermission("minestore.auth|ms.auth")
    @CommandAlias("auth")
    @SuppressWarnings("unused")
    public void onAuth(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        if (user instanceof CommonConsoleUser) {
            user.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        AuthUser authUser = MineStoreCommon.getInstance().authHolder().getAuthUser(user.getName());
        if (authUser == null) {
            user.sendMessage("[MineStore] You don't have any pending authentication!");
            return;
        }
        authUser.confirmAuth();
        user.sendMessage("[MineStore] You are now authenticated!");
    }
}
